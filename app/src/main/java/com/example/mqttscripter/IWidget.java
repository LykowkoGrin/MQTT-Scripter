package com.example.mqttscripter;

import android.view.View;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface IWidget {
    public String getWidgetName();

    public Map<String,String> getSavedData();
    public void setupFromSavedData(Map<String,String> data);
    public void setMQTTManager(@NotNull MQTTManager mqtt);

    public View getView();
}
