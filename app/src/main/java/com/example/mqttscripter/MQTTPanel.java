package com.example.mqttscripter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MQTTPanel extends Fragment {
    LayoutInflater inflater;

    private String panelName = "";
    private MQTTManager mqtt;
    private ImageButton statusButton;

    public MQTTPanel(){
        super(R.layout.panel);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.panel, container, false);

        this.inflater = inflater;

        View backButton = view.findViewById(R.id.back);
        View settingsButton = view.findViewById(R.id.settings_button);
        View connButton = view.findViewById(R.id.conn_status);

        connButton.setOnClickListener((View v)->{
            if(mqtt.isConnected()) disconnect();
            else connect();
        });

        backButton.setOnClickListener((View v) ->{
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.myFragmentContainer, new HomeFragment());
            transaction.commit();
        });

        settingsButton.setOnClickListener((View v) ->{
            MQTTPanelSettings.setPanel(this);

            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.myFragmentContainer, new MQTTPanelSettings());
            transaction.commit();
        });

        setConnectStatusButton((ImageButton) connButton);

        TextView viewPanelName = view.findViewById(R.id.panel_name);
        viewPanelName.setText(panelName);

        return view;
    }

    public String getPanelName(){
        return panelName;
    }

    public void setPanelName(String panelName){
        this.panelName = panelName;
    }

    public void setMQTTManager(MQTTManager mqtt){
        this.mqtt = mqtt;

        if(mqtt != null){
            mqtt.setMQTTCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    if(statusButton != null){
                        statusButton.setImageResource(R.drawable.baseline_signal_wifi_4_bar_50);
                    }
                }

                @Override
                public void connectionLost(Throwable cause) {
                    if(statusButton != null){
                        statusButton.setImageResource(R.drawable.baseline_signal_wifi_off_50);
                    }
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
        }
    }

    public MQTTManager getMQTTManager(){
        return mqtt;
    }

    public void connect(){
        if(statusButton != null){
            statusButton.setImageResource(R.drawable.baseline_watch_later_50);
        }
        mqtt.connect(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {

            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                if(statusButton != null){
                    statusButton.setImageResource(R.drawable.baseline_signal_wifi_off_50);
                }
            }
        });
    }
    public void disconnect(){
        if(statusButton != null){
            statusButton.setImageResource(R.drawable.baseline_watch_later_50);
        }
        mqtt.disconnect();
    }

    public void setConnectStatusButton(ImageButton statusButton){
        this.statusButton = statusButton;

        if(statusButton != null && mqtt != null){
            if(mqtt.isConnected()) statusButton.setImageResource(R.drawable.baseline_signal_wifi_4_bar_50);
            else statusButton.setImageResource(R.drawable.baseline_signal_wifi_off_50);
        }
    }

    static public Set<MQTTPanel> panels = new HashSet<>();
}
