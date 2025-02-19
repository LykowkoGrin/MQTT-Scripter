package com.example.mqttscripter;

import android.content.Context;
import android.util.Log;

import cloud.deepblue.mqttfix.mqtt.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.UUID;

public class MQTTManager {

    private static final String TAG = "MQTTManager";

    private Context context;

    // Настройки подключения
    private String url;         // адрес сервера (без протокола и порта), например "broker.hivemq.com"
    private String protocol = "tcp"; // протокол по умолчанию
    private int port = 1883;          // порт по умолчанию
    private String username;
    private String password;
    private String clientId;

    // Клиент MQTT
    private MqttAndroidClient mqttClient;

    /**
     * Интерфейс слушателя успешного подключения.
     */
    public interface OnConnectSuccessListener {
        void onConnectSuccess();
    }

    /**
     * Интерфейс слушателя результата публикации сообщения.
     */
    public interface OnPublishListener {
        void onPublishSuccess();
        void onPublishFailure(Throwable exception);
    }

    // Конструктор принимает Context (необходим для создания MqttAndroidClient)
    public MQTTManager(Context context) {
        this.context = context;
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


    /**
     * Асинхронно публикует данные по заданному топику.
     * Если клиент не подключён, вызывается onPublishFailure() слушателя.
     *
     * @param topic    топик для публикации
     * @param data     строка с данными для публикации
     * @param listener слушатель результата публикации (может быть null)
     */
    public void publish(final String topic, final String data, final OnPublishListener listener) {
        if (mqttClient == null || !mqttClient.isConnected()) {
            Log.e(TAG, "Клиент не подключён. Публикация невозможна.");
            if (listener != null) {
                listener.onPublishFailure(new IllegalStateException("MQTT client is not connected"));
            }
            return;
        }

        try {
            // Публикуем сообщение. Параметры: topic, payload, qos, retained, userContext, callback.
            mqttClient.publish(topic, data.getBytes(), 1, false, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Публикация прошла успешно для топика: " + topic);
                    if (listener != null) {
                        listener.onPublishSuccess();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "Ошибка публикации для топика " + topic + ": " + exception.getMessage());
                    if (listener != null) {
                        listener.onPublishFailure(exception);
                    }
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, "Исключение при публикации сообщения", e);
            if (listener != null) {
                listener.onPublishFailure(e);
            }
        }
    }




    // --------------------- Методы подключения ---------------------

    /**
     * Подключается к MQTT-брокеру с использованием ранее заданных настроек.
     * При успешном подключении вызывается метод onConnectSuccess() у переданного слушателя.
     *
     * @param listener слушатель успешного подключения (может быть null)
     */
    public void connect(final OnConnectSuccessListener listener) {
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

        MqttConnectOptions options = new MqttConnectOptions();

        options.setAutomaticReconnect(true);  // Включаем автореконнект
        options.setCleanSession(false);       // Сохраняем сессию для повторных подключений
        options.setKeepAliveInterval(60);

        if (username != null) {
            options.setUserName(username);
        }
        if (password != null) {
            options.setPassword(password.toCharArray());
        }

        try {
            mqttClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Подключение прошло успешно");
                    if (listener != null) {
                        listener.onConnectSuccess();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "Ошибка подключения: " + exception.getMessage());
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, "Исключение при попытке подключения", e);
        }
    }

    /**
     * Отключается от MQTT-брокера.
     */
    public void disconnect() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                Log.d(TAG, "Отключение прошло успешно");
            } catch (MqttException e) {
                Log.e(TAG, "Ошибка при отключении: " + e.getMessage());
            }
        }
    }
}