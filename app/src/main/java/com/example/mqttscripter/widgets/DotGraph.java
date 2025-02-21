package com.example.mqttscripter.widgets;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;

import com.example.mqttscripter.IWidget;
import com.example.mqttscripter.MQTTManager;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DotGraph implements IWidget {

    MQTTManager mqtt;
    Context context;
    private LineChart lineChart;
    private int maxEntrys = 10;

    public DotGraph(Context context){
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


        //addEntry(0,1);
        //addEntry(10,20);
        //addEntry(100,500);
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

            // Ограничиваем количество точек, используя maxDots
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
        return "dot_graph";
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

    @Override
    public void setMQTTManager(@NotNull MQTTManager mqtt){
        this.mqtt = mqtt;
    }

    public void setMaxEntrys(int maxEntrys){
        this.maxEntrys = maxEntrys;
    }

}
