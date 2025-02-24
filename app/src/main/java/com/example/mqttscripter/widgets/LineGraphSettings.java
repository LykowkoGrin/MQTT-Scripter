package com.example.mqttscripter.widgets;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.mqttscripter.HomeFragment;
import com.example.mqttscripter.LuaScript;
import com.example.mqttscripter.LuaScriptGrabber;
import com.example.mqttscripter.MQTTPanel;
import com.example.mqttscripter.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LineGraphSettings extends Fragment {

    private LineGraph graph;
    private MQTTPanel panel;
    private LuaScriptGrabber luaGrabber;
    private LuaScript luaScript;

    private String graphName = "";
    private String topic = "";
    private int qos = 0;
    private int maxDots = 10;
    private String valueName = "Value Name";



    LineGraphSettings(LineGraph graph, MQTTPanel panel){
        super(R.layout.graph_settings);

        this.graph = graph;
        this.panel = panel;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.graph_settings, container, false);

        luaGrabber = new LuaScriptGrabber(this);
        luaScript = graph.getLuaScript();

        View cancelButton = view.findViewById(R.id.button_cancel);
        View acceptButton = view.findViewById(R.id.button_accept);
        View deleteButton = view.findViewById(R.id.button_delete);
        View scriptButton = view.findViewById(R.id.choose_lua);
        TextView loadedFileText = view.findViewById(R.id.loaded_file_name);
        Spinner qosSpinner = view.findViewById(R.id.spinner_QoS);

        if(luaScript != null){
            loadedFileText.setText(luaScript.getFileName());
            loadedFileText.setTextColor(Color.GREEN);
        }

        graphName = graph.getGraphName();
        topic = graph.getTopic();
        qos = graph.getQoS();
        maxDots = graph.getMaxEntrys();
        valueName = graph.getDataLabel();

        cancelButton.setOnClickListener((View v) -> goBack());

        acceptButton.setOnClickListener((View v) ->{

            boolean allOk = true;
            allOk &= handleGraphName(view);
            allOk &= handleTopic(view);
            allOk &= handleQos(view);
            allOk &= handleMaxDots(view);
            allOk &= handleValueName(view);

            if(allOk){

                graph.setQoS(qos);
                graph.setGraphName(graphName);
                graph.setTopic(topic);
                graph.setDataLabel(valueName);
                graph.setMaxEntrys(maxDots);

                panel.disconnectWidgetFromTopics(graph);
                panel.connectWidgetToTopic(graph,graph.getTopic(),graph.getQoS());

                goBack();
            }

        });

        deleteButton.setOnClickListener((View v) ->{
            panel.removeWidget(graph);

            goBack();
        });

        scriptButton.setOnClickListener((View v)->{

            luaGrabber.startChoose(new LuaScriptGrabber.LuaScriptResultCallback() {
                @Override
                public void onScriptLoaded(LuaScript script) {
                    luaScript = script;
                    loadedFileText.setText(script.getFileName());
                    loadedFileText.setTextColor(Color.GREEN);
                    graph.setLuaScript(luaScript);
                }

                @Override
                public void onError(String message) {
                    loadedFileText.setText("Только .lua и .txt!");
                    loadedFileText.setTextColor(Color.RED);
                }
            });


        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.QoS_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        qosSpinner.setAdapter(adapter);

        qosSpinner.setSelection(qos);

        ((TextInputEditText)view.findViewById(R.id.edit_graph_name)).setText(graphName);
        ((TextInputEditText)view.findViewById(R.id.edit_topic)).setText(topic);
        ((TextInputEditText)view.findViewById(R.id.edit_max_dots)).setText(String.valueOf(maxDots));
        ((TextInputEditText)view.findViewById(R.id.edit_value_name)).setText(valueName);

        return view;
    }

    private void goBack(){

        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.myFragmentContainer, panel);
        transaction.commit();

    }

    private boolean handleGraphName(View view) {
        TextInputLayout layoutGraphName = view.findViewById(R.id.layout_graph_name);
        TextInputEditText editGraphName = view.findViewById(R.id.edit_graph_name);

        String text = editGraphName.getText().toString().trim();

        if(text.isEmpty()){
            layoutGraphName.setError("Ошибка: поле не может быть пустым!"); // Покажет ошибку
            layoutGraphName.setErrorEnabled(true); // Включит отображение ошибки
            return false;
        }

        graphName = text;
        layoutGraphName.setError(null);

        return true;
    }

    private boolean handleTopic(View view) {
        TextInputLayout layoutTopic = view.findViewById(R.id.layout_topic);
        TextInputEditText editTopic = view.findViewById(R.id.edit_topic);

        String text = editTopic.getText().toString().trim();

        if(text.isEmpty()){
            layoutTopic.setError("Ошибка: поле не может быть пустым!");
            layoutTopic.setErrorEnabled(true);
            return false;
        }

        topic = text;
        layoutTopic.setError(null);

        return true;
    }

    private boolean handleQos(View view) {

        return true;
    }

    private boolean handleMaxDots(View view) {
        TextInputLayout layoutMaxDots = view.findViewById(R.id.layout_max_dots);
        TextInputEditText editMaxDots = view.findViewById(R.id.edit_max_dots);

        String text = editMaxDots.getText().toString().trim();

        if(text.isEmpty()){
            layoutMaxDots.setError("Ошибка: поле не может быть пустым!");
            layoutMaxDots.setErrorEnabled(true);
            return false;
        }

        try {
            maxDots = Integer.parseInt(text);
        } catch(NumberFormatException e) {
            layoutMaxDots.setError("Ошибка: введите корректное число!");
            layoutMaxDots.setErrorEnabled(true);
            return false;
        }

        layoutMaxDots.setError(null);
        return true;
    }

    private boolean handleValueName(View view) {
        TextInputLayout layoutValueName = view.findViewById(R.id.layout_value_name);
        TextInputEditText editValueName = view.findViewById(R.id.edit_value_name);

        String text = editValueName.getText().toString().trim();

        if(text.isEmpty()){
            layoutValueName.setError("Ошибка: поле не может быть пустым!");
            layoutValueName.setErrorEnabled(true);
            return false;
        }

        valueName = text;
        layoutValueName.setError(null);
        return true;
    }

}
