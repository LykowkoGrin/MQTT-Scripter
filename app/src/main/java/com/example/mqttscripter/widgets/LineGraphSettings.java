package com.example.mqttscripter.widgets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.mqttscripter.R;

public class LineGraphSettings extends Fragment {

    LineGraph graph;

    LineGraphSettings(LineGraph graph){
        super(R.layout.graph_settings);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.graph_settings, container, false);



        return view;
    }
}
