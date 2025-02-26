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

import com.example.mqttscripter.widgets.LineGraph;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MQTTPanel extends Fragment {
    LayoutInflater inflater;

    private String panelName = "";
    private MQTTManager mqtt;
    private MQTTConsole mqttConsole;
    private ImageButton statusButton;

    private Context context;

    private LinearLayout widgetsLayout;

    private Set<IWidget> widgets = new HashSet<>();
    private Map<String,IWidget> topics = new HashMap<>();
    private HomeFragment home;

    public MQTTPanel(Context context, HomeFragment home){
        super(R.layout.panel);
        this.context = context;
        this.home = home;

        mqtt = new MQTTManager(BackgroundService.getContext());
        mqttConsole = new MQTTConsole(this);

        mqtt.setMQTTCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if(statusButton != null){
                    statusButton.setImageResource(R.drawable.baseline_signal_wifi_4_bar_50);
                }
                for(Map.Entry<String, IWidget> entry : topics.entrySet()){
                    if(entry.getKey().isEmpty()) continue;
                    connectWidgetToTopic(entry.getValue(),entry.getKey(),entry.getValue().getQoS());
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.panel, container, false);

        this.inflater = inflater;

        View backButton = view.findViewById(R.id.back);
        View settingsButton = view.findViewById(R.id.settings_button);
        View connButton = view.findViewById(R.id.conn_status);
        View addWidgetButton = view.findViewById(R.id.create_widget);
        View consoleButton = view.findViewById(R.id.open_console);

        connButton.setOnClickListener((View v)->{
            if(mqtt.isConnected()) disconnect();
            else connect();
        });

        consoleButton.setOnClickListener((View v)->{
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.myFragmentContainer, mqttConsole);
            transaction.commit();
        });

        backButton.setOnClickListener((View v) ->{
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.myFragmentContainer, home);
            transaction.commit();
        });

        settingsButton.setOnClickListener((View v) ->{
            //MQTTPanelSettings.setPanel(this);

            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.myFragmentContainer, new MQTTPanelSettings(context,this, home));
            transaction.commit();
        });

        addWidgetButton.setOnClickListener((View v)->{
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.myFragmentContainer, new WidgetAdder(context,this));
            transaction.commit();
        });

        setConnectStatusButton((ImageButton) connButton);

        TextView viewPanelName = view.findViewById(R.id.panel_name);
        viewPanelName.setText(panelName);


        widgetsLayout = view.findViewById(R.id.widgets_layout);

        for(IWidget widget : widgets){
            if (widget.getView().getParent() != null)
                ((ViewGroup) widget.getView().getParent()).removeView(widget.getView());

            widgetsLayout.addView(widget.getView());
        }

        return view;
    }

    public String getPanelName(){
        return panelName;
    }

    public void setPanelName(String panelName){
        this.panelName = panelName;
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
    public void deletePanel(){

        mqtt.close();
        widgets.clear();
        topics.clear();

        widgetsLayout = null;
        mqtt = null;
        statusButton = null;
        context = null;
        mqttConsole = null;
    }

    public void setConnectStatusButton(ImageButton statusButton){
        this.statusButton = statusButton;

        if(statusButton != null && mqtt != null){
            if(mqtt.isConnected()) statusButton.setImageResource(R.drawable.baseline_signal_wifi_4_bar_50);
            else statusButton.setImageResource(R.drawable.baseline_signal_wifi_off_50);
        }
    }

    public void addWidget(IWidget widget){
        widgets.add(widget);

        widgetsLayout.addView(widget.getView());

    }
    public void removeWidget(IWidget widget){
        widgets.remove(widget);
        disconnectWidgetFromTopics(widget);
    }

    public void disconnectWidgetFromTopics(@NotNull IWidget widget){
        for(Map.Entry<String, IWidget> entry : topics.entrySet()){
            if(entry.getValue() != widget) continue;

            topics.remove(entry.getKey());
            mqtt.unsubscribe(entry.getKey());
        }
    }

    public void connectWidgetToTopic(@NotNull IWidget widget, @NotNull String topic, int QoS){

        if(topic.isEmpty()) return;

        topics.put(topic,widget);
        mqtt.subscribe(topic, QoS, new IMqttMessageListener() {
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                widget.messageArrived(topic,message);
            }
        });
    }

    public MQTTConsole getConsole(){
        return mqttConsole;
    }

    private IWidget getWidgetByName(String name){
        switch (name){
            case "line_graph":
                return new LineGraph(context, this);

        }
        return null;
    }

}
