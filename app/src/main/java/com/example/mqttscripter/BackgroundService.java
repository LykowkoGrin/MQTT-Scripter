package com.example.mqttscripter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

public class BackgroundService extends Service {

    private static Context mContext;
    private static final String CHANNEL_ID = "background_service_channel";

    public static HomeFragment home = null;

    // Статический метод для получения контекста сервиса
    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Используем application context, чтобы избежать утечек Activity context
        mContext = getApplicationContext();
        // Для API 26+ запускаем сервис как foreground service с уведомлением
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("MQTT Scripter")
                    .setContentText("MQTT Scripter is running in background")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .build();
            startForeground(1, notification);
        }
    }

    // Создание канала уведомлений (требуется для Android O и выше)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Background Service";
            String description = "Channel for background service";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Биндинг не требуется, возвращаем null
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // START_STICKY позволяет системе перезапускать сервис в случае его завершения
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Очищаем статическую ссылку для предотвращения утечек памяти
        mContext = null;
    }
}
