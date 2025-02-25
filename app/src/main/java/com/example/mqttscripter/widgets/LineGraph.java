package com.example.mqttscripter.widgets;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.fragment.app.FragmentTransaction;

import com.example.mqttscripter.IWidget;
import com.example.mqttscripter.LuaScript;
import com.example.mqttscripter.MQTTManager;
import com.example.mqttscripter.MQTTPanel;
import com.example.mqttscripter.MQTTPanelSettings;
import com.example.mqttscripter.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LineGraph implements IWidget {

    MQTTPanel panel;
    Context context;
    private LineChart lineChart;
    private int maxEntrys = 10;
    private LuaScript luaScript = null;

    String topic = "";
    String graphName = "";
    String dataLabel = "";
    int QoS = 0;

    public LineGraph(Context context, @NotNull MQTTPanel panel){
        this.panel = panel;
        this.context = context;
        lineChart = new LineChart(context);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                600
        );
        lineChart.setLayoutParams(params);

        LineDataSet dataSet = new LineDataSet(new ArrayList<>(), "Пример данных");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.RED);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        lineChart.setNoDataText("");

        Description description = new Description();
        description.setText("Имя графика");
        lineChart.setDescription(description);

        lineChart.invalidate();

        lineChart.setOnClickListener((View v) -> {
            FragmentTransaction transaction = panel.requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.myFragmentContainer, new LineGraphSettings(this,panel));
            transaction.commit();
        });
    }

    private void addEntry(float x, float y) {
        // Получаем данные графика
        LineData data = lineChart.getData();
        if (data != null) {
            // Пытаемся получить первый набор данных
            ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {
                // Если набора данных ещё нет, создаём новый
                set = createSet();
                data.addDataSet(set);
            }
            // Добавляем новую точку в набор данных
            data.addEntry(new Entry(x, y), 0);

            // Ограничиваем количество точек, используя maxEntrys
            if (set.getEntryCount() > maxEntrys) {
                // Получаем первую (самую старую) точку
                Entry removedEntry = set.getEntryForIndex(0);
                // Удаляем её из набора данных
                data.removeEntry(removedEntry, 0);
            }

            // Уведомляем данные о том, что они изменились
            data.notifyDataChanged();
            // Обновляем график
            lineChart.notifyDataSetChanged();
            lineChart.invalidate();
        }
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Данные");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(context.getResources().getColor(android.R.color.holo_blue_dark));
        set.setCircleColor(context.getResources().getColor(android.R.color.holo_blue_light));
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setValueTextSize(10f);
        set.setDrawValues(false);
        return set;
    }

    @Override
    public String getWidgetName(){
        return "line_graph";
    }

    @Override
    public Map<String, String> getSavedData() {
        return Collections.emptyMap();
    }

    @Override
    public void setupFromSavedData(Map<String, String> data) {

    }

    @Override
    public View getView() {
        return (View)lineChart;
    }



    public void setMaxEntrys(int maxEntrys){
        this.maxEntrys = maxEntrys;
    }

    public void setGraphName(String name) {
        graphName = name;

        Description description = lineChart.getDescription();
        if (description == null) {
            description = new Description();
        }
        description.setText(name);
        lineChart.setDescription(description);
        lineChart.invalidate();
    }

    public void setDataLabel(String label) {
        dataLabel = label;

        LineData data = lineChart.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            if (set instanceof LineDataSet) {
                ((LineDataSet)set).setLabel(label);
                lineChart.invalidate();
            }
        }
    }

    public void setTopic(String topic){
        this.topic = topic;
    }

    public void setQoS(int QoS){
        this.QoS = QoS;
    }


    @Override
    public int getQoS(){
        return QoS;
    }

    public String getGraphName(){
        return graphName;
    }

    public int getMaxEntrys(){
        return maxEntrys;
    }

    public String getTopic(){
        return topic;
    }

    public String getDataLabel(){
        return dataLabel;
    }

    public void setLuaScript(@NotNull LuaScript luaScript){
        this.luaScript = luaScript;
        this.luaScript.setMqttConsole(panel.getConsole());
    }

    public LuaScript getLuaScript(){
        return luaScript;
    }



    @Override
    public void messageArrived(String topic, MqttMessage message){
        Log.d("LineGraph", Arrays.toString(message.getPayload()));

        if(luaScript == null) return;


        TwoArgFunction addEntryLua = new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue arg1, LuaValue arg2) {
                float x = (float) arg1.checkdouble();
                float y = (float) arg2.checkdouble();
                // Вызываем наш приватный метод
                addEntry(x, y);
                return LuaValue.NIL;
            }
        };

        String messageText = Arrays.toString(message.getPayload());

        Map<String, LuaValue> globalValues = new HashMap<>();
        globalValues.put("addDot",addEntryLua);
        globalValues.put("message",LuaValue.valueOf(messageText));



        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    luaScript.execute(globalValues);
                }
                catch (LuaError e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
/*
    @Override
    public void connect(@NotNull MQTTManager mqtt){
        if(topic.isEmpty()) return;

        mqtt.subscribe(topic, QoS, new IMqttMessageListener() {
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("LineGraph", Arrays.toString(message.getPayload()));
            }
        });
    }*/
}
