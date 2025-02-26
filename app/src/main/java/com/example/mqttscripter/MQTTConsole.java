package com.example.mqttscripter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MQTTConsole extends Fragment {

    private TextView tvConsole = null;
    private ScrollView scrollView = null;
    private final MQTTPanel panel;

    private StringBuilder logBuffer = new StringBuilder();

    public MQTTConsole(MQTTPanel panel) {
        super(R.layout.console);

        this.panel = panel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.console, container, false);
        tvConsole = view.findViewById(R.id.tvConsole);
        scrollView = view.findViewById(R.id.scrollView);
        View closeButton = view.findViewById(R.id.close_console);

        tvConsole.setText(logBuffer.toString());

        closeButton.setOnClickListener((View v)->{
            tvConsole = null;
            scrollView = null;

            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.myFragmentContainer, panel);
            transaction.commit();
        });

        return view;
    }

    /**
     * Метод для вывода нового сообщения в консоль.
     *
     * @param text текст сообщения
     */
    public void print(String text) {
        logBuffer.append(text).append("\n");

        try{
            if (tvConsole != null) {
                requireActivity().runOnUiThread(() -> {
                    if (tvConsole != null) {
                        tvConsole.append(text + "\n");
                        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
                    }
                });
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
