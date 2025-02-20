package com.example.mqttscripter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MQTTPanelSettings extends Fragment {

    private LayoutInflater inflater;
    private MQTTManager mqtt;
    private boolean isNewPanel = false;

    public MQTTPanelSettings(){
        super(R.layout.panel_settings);

        isNewPanel = panel.getMQTTManager() == null;

        if(isNewPanel){
            this.mqtt = new MQTTManager();
        }
        else{
            this.mqtt = new MQTTManager(panel.getMQTTManager());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.panel_settings, container, false);

        this.inflater = inflater;

        View cancelButton = view.findViewById(R.id.button_cancel);
        View acceptButton = view.findViewById(R.id.button_accept);
        View deleteButton = view.findViewById(R.id.button_delete);
        Spinner protocolSpinner = view.findViewById(R.id.spinner_protocol);


        cancelButton.setOnClickListener((View v) -> {
            goBack(false);
        });
        acceptButton.setOnClickListener((View v) ->{
            boolean allOk = true;
            allOk &= handlePanelName(view);
            allOk &= handleURL(view);
            allOk &= handleProtocol(view);
            allOk &= handlePort(view);
            handleUsername(view);
            handlePassword(view);
            handleClientID(view);

            if(allOk){
                panel.setMQTTManager(mqtt);

                if(isNewPanel) MQTTPanel.panels.add(panel);

                goBack(false);
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.protocol_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        protocolSpinner.setAdapter(adapter);

        if(isNewPanel){
            deleteButton.setActivated(false);
        }
        else{
            deleteButton.setOnClickListener((View v) ->{

                MQTTPanel.panels.remove(panel);

                goBack(true);
            });
        }

        switch(mqtt.getProtocol()){
            case "tcp":
                protocolSpinner.setSelection(0);
                break;
            case "ssl":
                protocolSpinner.setSelection(1);
                break;
            case "ws":
                protocolSpinner.setSelection(2);
                break;
            case "wss":
                protocolSpinner.setSelection(3);
                break;
        }

        ((TextInputEditText)view.findViewById(R.id.edit_panel_name)).setText(panel.getPanelName());
        ((TextInputEditText)view.findViewById(R.id.edit_url)).setText(mqtt.getURL());
        ((TextInputEditText)view.findViewById(R.id.edit_port)).setText(String.valueOf(mqtt.getPort()));
        ((TextInputEditText)view.findViewById(R.id.edit_username)).setText(mqtt.getUsername());
        ((TextInputEditText)view.findViewById(R.id.edit_password)).setText(mqtt.getPassword());
        ((TextInputEditText)view.findViewById(R.id.edit_client_id)).setText(mqtt.getClientId());


        return view;
    }

    private boolean handlePanelName(View view){
        TextInputLayout layoutPanelName = view.findViewById(R.id.layout_panel_name);
        TextInputEditText editPanelName = view.findViewById(R.id.edit_panel_name);

        String text = editPanelName.getText().toString().trim();

        if(text.isEmpty()){
            layoutPanelName.setError("Ошибка: поле не может быть пустым!"); // Покажет ошибку
            layoutPanelName.setErrorEnabled(true); // Включит отображение ошибки
            return false;
        }

        panel.setPanelName(text);
        layoutPanelName.setError(null);

        return true;

    }

    private boolean handleURL(View view){
        TextInputLayout layoutPanelName = view.findViewById(R.id.layout_url);
        TextInputEditText editPanelName = view.findViewById(R.id.edit_url);

        String text = editPanelName.getText().toString().trim();

        if(text.isEmpty()){
            layoutPanelName.setError("Ошибка: поле не может быть пустым!"); // Покажет ошибку
            layoutPanelName.setErrorEnabled(true); // Включит отображение ошибки
            return false;
        }

        layoutPanelName.setError(null);

        mqtt.setURL(text);

        return true;
    }

    private boolean handleProtocol(View view){
        Spinner spinner = view.findViewById(R.id.spinner_protocol);

        int itemId = (int)spinner.getSelectedItemId();

        switch (itemId){
            case 0:
                mqtt.setProtocol("tcp");
                break;
            case 1:
                mqtt.setProtocol("ssl");
                break;
            case 2:
                mqtt.setProtocol("ws");
                break;
            case 3:
                mqtt.setProtocol("wss");
                break;
        }

        return true;
    }

    private boolean handlePort(View view){
        TextInputLayout layoutPanelName = view.findViewById(R.id.layout_port);
        TextInputEditText editPanelName = view.findViewById(R.id.edit_port);

        String text = editPanelName.getText().toString().trim();

        if(text.isEmpty()){
            layoutPanelName.setError("Ошибка: поле не может быть пустым!"); // Покажет ошибку
            layoutPanelName.setErrorEnabled(true); // Включит отображение ошибки
            return false;
        }

        try{
            mqtt.setPort(Integer.parseInt(text));
        }
        catch (Exception ex){
            layoutPanelName.setError("Ошибка: неверное значение порта!"); // Покажет ошибку
            layoutPanelName.setErrorEnabled(true); // Включит отображение ошибки
            return false;
        }


        layoutPanelName.setError(null);
        return true;
    }

    private boolean handleUsername(View view){
        TextInputLayout layoutPanelName = view.findViewById(R.id.layout_username);
        TextInputEditText editPanelName = view.findViewById(R.id.edit_username);

        String text = editPanelName.getText().toString().trim();

        if(text.isEmpty()){
            //layoutPanelName.setError("Ошибка: поле не может быть пустым!"); // Покажет ошибку
            //layoutPanelName.setErrorEnabled(true); // Включит отображение ошибки
            return false;
        }

        //layoutPanelName.setError(null);

        mqtt.setUsername(text);

        return true;
    }

    private boolean handlePassword(View view){
        TextInputLayout layoutPanelName = view.findViewById(R.id.layout_password);
        TextInputEditText editPanelName = view.findViewById(R.id.edit_password);

        String text = editPanelName.getText().toString().trim();

        if(text.isEmpty()){
            //layoutPanelName.setError("Ошибка: поле не может быть пустым!"); // Покажет ошибку
            //layoutPanelName.setErrorEnabled(true); // Включит отображение ошибки
            return false;
        }

        //layoutPanelName.setError(null);

        mqtt.setPassword(text);

        return true;
    }

    private boolean handleClientID(View view){
        TextInputLayout layoutPanelName = view.findViewById(R.id.layout_client_id);
        TextInputEditText editPanelName = view.findViewById(R.id.edit_client_id);

        String text = editPanelName.getText().toString().trim();

        if(text.isEmpty()){
            //layoutPanelName.setError("Ошибка: поле не может быть пустым!"); // Покажет ошибку
            //layoutPanelName.setErrorEnabled(true); // Включит отображение ошибки
            return false;
        }

        //layoutPanelName.setError(null);

        mqtt.setClientId(text);

        return true;
    }

    private void goBack(boolean isDeleted){
        if(isNewPanel || isDeleted){
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.myFragmentContainer, new HomeFragment());
            transaction.commit();
        }
        else{
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.myFragmentContainer, panel);
            transaction.commit();
        }
    }




    public static void setPanel(MQTTPanel panel){
        MQTTPanelSettings.panel = panel;
    }

    private static MQTTPanel panel;

}
