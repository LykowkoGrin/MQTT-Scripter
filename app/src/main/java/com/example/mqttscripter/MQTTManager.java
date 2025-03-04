package com.example.mqttscripter;

import android.content.Context;
import android.util.Log;

import cloud.deepblue.mqttfix.mqtt.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.UUID;

import javax.net.ssl.SSLSocketFactory;

public class MQTTManager {

    private static final String TAG = "MQTTManager";

    // Настройки подключения
    private String url;         // адрес сервера (без протокола и порта), например "broker.hivemq.com"
    private String protocol = "tcp"; // протокол по умолчанию
    private int port = 1883;          // порт по умолчанию
    private String username;
    private String password;
    private String clientId;

    // Клиент MQTT
    private MqttAndroidClient mqttClient;
    private Context context;


    public MQTTManager(Context context){
        this.context = context;
    }

    public MQTTManager(MQTTManager mqtt){
        this.url = mqtt.getURL();
        this.port = mqtt.getPort();
        this.protocol = mqtt.getProtocol();
        this.username = mqtt.getUsername();
        this.password = mqtt.getPassword();
        this.clientId = mqtt.getClientId();

        this.context = mqtt.context;
    }

    // --------------------- Методы для настройки ---------------------

    /**
     * Устанавливает URL для подключения.
     * Если строка содержит схему (например, "tcp://") и порт (например, ":1883"),
     * то они будут распознаны и установлены.
     *
     * Примеры:
     *   "tcp://broker.hivemq.com:1883" → protocol = "tcp", url = "broker.hivemq.com", port = 1883
     *   "broker.hivemq.com" → url = "broker.hivemq.com"
     *
     * @param url строка с адресом сервера (возможно, с протоколом и портом)
     */
    public void setURL(String url) {
        if (url == null || url.isEmpty()) {
            return;
        }
        // Если присутствует схема (например, "tcp://")
        if (url.contains("://")) {
            String[] parts = url.split("://", 2);
            if (parts.length == 2) {
                this.protocol = parts[0];
                String remaining = parts[1];
                // Если указан порт (наличие ":")
                if (remaining.contains(":")) {
                    int colonIndex = remaining.lastIndexOf(':');
                    this.url = remaining.substring(0, colonIndex);
                    try {
                        this.port = Integer.parseInt(remaining.substring(colonIndex + 1));
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Некорректный номер порта в URL, используется текущий порт: " + this.port, e);
                    }
                } else {
                    this.url = remaining;
                }
            } else {
                this.url = url;
            }
        } else {
            this.url = url;
        }
    }

    public String getURL() {
        return this.url;
    }

    public void setProtocol(String protocol) {
        if (protocol != null && !protocol.isEmpty()) {
            this.protocol = protocol;
        }
    }

    public String getProtocol() {
        return this.protocol;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return this.port;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return this.clientId;
    }

    public void setMQTTCallback(MqttCallbackExtended callback){
        mqttCallbackExtended = callback;
    }

    public boolean isConnected(){
        if(mqttClient == null) return false;
        return mqttClient.isConnected();
    }


    /**
     * Асинхронно публикует данные по заданному топику.
     * Если клиент не подключён, вызывается onPublishFailure() слушателя.
     *
     * @param topic    топик для публикации
     * @param message     сообщение для публикации
     * @param listener случшатель умпешной и нет публикации
     */
    public void publish(final String topic, MqttMessage message, IMqttActionListener listener) {
        if (mqttClient == null || !mqttClient.isConnected()) {
            Log.e(TAG, "Клиент не подключён. Публикация невозможна.");
            listener.onFailure(null,new Exception("No server connection"));
            return;
        }

        try {
            mqttClient.publish(topic,message,context,listener);
        }
        catch (Exception e){
            Log.e("publish",e.toString());
        }
    }

    public void subscribe(String topic, int qos, IMqttMessageListener messageListener) {
        if (mqttClient == null || !mqttClient.isConnected()) {
            Log.e(TAG, "Клиент не подключён. Подписка невозможна.");
            return;
        }

        try{
            mqttClient.subscribe(topic,qos,messageListener);
        }
        catch (Exception e){
            Log.e("subscribe",e.toString());
        }
    }

    public void unsubscribe(String topic) {
        if (mqttClient == null) {
            Log.e(TAG, "Клиент == null. Отписка невозможна.");
            return;
        }

        try {
            mqttClient.unsubscribe(topic);
        } catch (Exception e) {
            Log.e("unsubscribe", e.toString());
        }
    }


    // --------------------- Методы подключения ---------------------

    /**
     * Подключается к MQTT-брокеру с использованием ранее заданных настроек.
     * При успешном подключении вызывается метод onConnectSuccess() у переданного слушателя.
     *
     * @param listener слушатель успешного подключения (может быть null)
     */
    public void connect(IMqttActionListener listener) {
        if (url == null || url.isEmpty()) {
            Log.e(TAG, "URL не установлен.");
            return;
        }

        if (clientId == null || clientId.isEmpty()) {
            clientId = "client-" + UUID.randomUUID().toString();
            Log.d(TAG, "Сгенерирован новый clientId: " + clientId);
        }

        // Формируем полный URI подключения, например "tcp://broker.hivemq.com:1883"
        String fullUri = protocol + "://" + url + ":" + port;
        Log.d(TAG, "Подключение к: " + fullUri);

        // Создаём экземпляр клиента MQTT
        mqttClient = new MqttAndroidClient(context, fullUri, clientId);

        mqttClient.setCallback(mqttCallbackExtended);

        MqttConnectOptions options = new MqttConnectOptions();

        if(protocol.equals("wss") || protocol.equals("ssl")){
            options.setSocketFactory(SSLSocketFactory.getDefault());
        }

        options.setAutomaticReconnect(true);  // Включаем автореконнект
        options.setCleanSession(true);
        options.setKeepAliveInterval(60);

        if (username != null && !username.isEmpty()) {
            options.setUserName(username);
        }
        if (password != null && !password.isEmpty()) {
            options.setPassword(password.toCharArray());
        }

        try {
            mqttClient.connect(options, null, listener);
        } catch (MqttException e) {
            Log.e(TAG, "Исключение при попытке подключения", e);
        }
    }

    /**
     * Отключается от MQTT-брокера.
     */
    public void disconnect() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try{
                mqttClient.disconnect();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void close() {
        if (mqttClient != null) {
            try {
                mqttClient.close();
                mqttClient = null;
            } catch (Exception e) {
                System.err.println("Error closing MQTT client: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    private MqttCallbackExtended mqttCallbackExtended;
}