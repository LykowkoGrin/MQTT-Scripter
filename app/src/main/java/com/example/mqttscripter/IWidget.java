package com.example.mqttscripter;

import android.view.View;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface IWidget {
    public String getWidgetName();

    public Map<String,String> getSavedData();
    public void setupFromSavedData(Map<String,String> data);

    public int getQoS();
    public void messageArrived(String topic, MqttMessage message);
    public void openSettings();


    public View getView();
}
