package com.example.mqttscripter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mqttscripter.widgets.DotGraph;

public class WidgetAdder extends Fragment {

    MQTTPanel panel;
    Context context;

    public WidgetAdder(Context context, MQTTPanel panel){
        super(R.layout.create_widget);

        this.context = context;
        this.panel = panel;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.create_widget, container, false);

        ImageButton backButton = view.findViewById(R.id.back);
        backButton.setOnClickListener((View v)-> goBack());


        setupWidgetsList(view);

        return view;
    }

    private void setupWidgetsList(View view){
        LinearLayout panel1 = view.findViewById(R.id.panel1);
        ImageView icon1 = panel1.findViewById(R.id.item_image);
        TextView text1 = panel1.findViewById(R.id.item_name);

        icon1.setImageResource(R.drawable.baseline_auto_graph_50);
        text1.setText("График");

        panel1.setOnClickListener((View v)->{
            panel.addWidget(new DotGraph(context));
            goBack();
        });
    }

    private void goBack(){
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.myFragmentContainer, panel);
        transaction.commit();
    }

}
