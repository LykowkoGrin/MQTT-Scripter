package com.example.mqttscripter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class HomeFragment extends Fragment {

    LinearLayout items;
    LayoutInflater inflater;

    public HomeFragment(){
        super(R.layout.all_panels);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.all_panels, container, false);

        this.inflater = inflater;
        items = view.findViewById(R.id.linearLayoutItems);
        //LinearLayout items = view.findViewById(R.id.linearLayoutItems);


        //View panelItemView = inflater.inflate(R.layout.panel_item, null, false);

        //items.addView(panelItemView);

        View addButton = view.findViewById(R.id.create_panel);
        addButton.setOnClickListener((View v) ->{
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, new MQTTPanelSettings());
            transaction.commit();
        });

        return view;
    }
/*
    public void createNewPanel(View v){


        View panelItemView = inflater.inflate(R.layout.panel_item, null, false);

        items.addView(panelItemView);
    }
*/


}
