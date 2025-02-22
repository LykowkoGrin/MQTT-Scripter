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

import java.util.HashMap;
import java.util.Map;

public class MQTTPanelSettings extends Fragment {

    String panelName = "";
    String url = "";
    String protocol = "";
    String username = "";
    String password = "";
    String clientID = "";
    int port = 2000;

    boolean isNewPanel = false;
    Context context;


    public MQTTPanelSettings(Context context, MQTTPanel panel){
        super(R.layout.panel_settings);

        this.panel = panel;
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.panel_settings, container, false);

        isNewPanel = panel.getPanelName().isEmpty();


        View cancelButton = view.findViewById(R.id.button_cancel);
        View acceptButton = view.findViewById(R.id.button_accept);
        View deleteButton = view.findViewById(R.id.button_delete);
        Spinner protocolSpinner = view.findViewById(R.id.spinner_protocol);

        panelName = panel.getPanelName();
        url = panel.getMQTTManager().getURL();
        protocol = panel.getMQTTManager().getProtocol();
        username = panel.getMQTTManager().getUsername();
        password = panel.getMQTTManager().getPassword();
        clientID = panel.getMQTTManager().getClientId();
        port = panel.getMQTTManager().getPort();


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
                MQTTManager mqtt = panel.getMQTTManager();
                panel.setPanelName(panelName);

                mqtt.setPassword(password);
                mqtt.setUsername(username);
                mqtt.setPort(port);
                mqtt.setURL(url);
                mqtt.setClientId(clientID);
                mqtt.setProtocol(protocol);

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

        switch(protocol){
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

        ((TextInputEditText)view.findViewById(R.id.edit_panel_name)).setText(panelName);
        ((TextInputEditText)view.findViewById(R.id.edit_url)).setText(url);
        ((TextInputEditText)view.findViewById(R.id.edit_port)).setText(String.valueOf(port));
        ((TextInputEditText)view.findViewById(R.id.edit_username)).setText(username);
        ((TextInputEditText)view.findViewById(R.id.edit_password)).setText(password);
        ((TextInputEditText)view.findViewById(R.id.edit_client_id)).setText(clientID);


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

        panelName = text;
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

        url = text;

        return true;
    }

    private boolean handleProtocol(View view){
        Spinner spinner = view.findViewById(R.id.spinner_protocol);

        int itemId = (int)spinner.getSelectedItemId();


        switch (itemId){
            case 0:
                protocol = "tcp";
                break;
            case 1:
                protocol = "ssl";
                break;
            case 2:
                protocol = "ws";
                break;
            case 3:
                protocol = "wss";
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
            port = Integer.parseInt(text);
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

        username = text;

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

        password = text;

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

        clientID = text;

        return true;
    }

    private void goBack(boolean isDeleted){
        if(isNewPanel || isDeleted){
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.myFragmentContainer, new HomeFragment(context));
            transaction.commit();
        }
        else{
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.myFragmentContainer, panel);
            transaction.commit();
        }
    }

    private MQTTPanel panel;

}
