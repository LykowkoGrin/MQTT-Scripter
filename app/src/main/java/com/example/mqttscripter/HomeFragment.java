package com.example.mqttscripter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.HashSet;
import java.util.Set;

public class HomeFragment extends Fragment {

    LinearLayout items;
    LayoutInflater inflater;
    private Set<MQTTPanel> panels = new HashSet<>();

    public HomeFragment(){
        super(R.layout.all_panels);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.all_panels, container, false);

        this.inflater = inflater;
        items = view.findViewById(R.id.linearLayoutItems);


        for(MQTTPanel mqttPanel : panels){
            View panelItemView = inflater.inflate(R.layout.panel_item, null, false);
            TextView panelNameText = panelItemView.findViewById(R.id.item_name);
            panelNameText.setText(mqttPanel.getPanelName());

            panelItemView.setOnClickListener((View v) ->{
                //MQTTPanelSettings.setPanel(mqttPanel);

                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.myFragmentContainer, mqttPanel);
                transaction.commit();
            });

            ImageButton statusBtn = panelItemView.findViewById(R.id.item_image);
            mqttPanel.setConnectStatusButton(statusBtn);
            statusBtn.setOnClickListener((View v) ->{
                if(mqttPanel.getMQTTManager().isConnected()) mqttPanel.disconnect();
                else mqttPanel.connect();
            });

            items.addView(panelItemView);
        }

        View addButton = view.findViewById(R.id.create_panel);
        addButton.setOnClickListener((View v) ->{

            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.myFragmentContainer, new MQTTPanelSettings(new MQTTPanel(this),this));
            transaction.commit();
        });

        return view;
    }

    public void removePanel(MQTTPanel panel) {
        panels.remove(panel);
    }
    public void addPanel(MQTTPanel panel){
        panels.add(panel);
    }
}
