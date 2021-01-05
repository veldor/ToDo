package net.veldor.todo.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

import net.veldor.todo.App;
import net.veldor.todo.R;
import net.veldor.todo.ui.IncomingTaskActivity;

public class MyNotify {
    private static final String MAIN_CHANNEL_ID = "main";
    private static final String PRIORITY_CHANNEL_ID = "priority";
    private static MyNotify instance;
    private final NotificationManager mNotificationManager;
    private int mLastNotificationId = 100;

    private MyNotify(){
        mNotificationManager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        // создам каналы уведомлений
        createChannels();
    }

    private void createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mNotificationManager != null) {
                // создам канал уведомлений о срабатывании защиты
                NotificationChannel nc = new NotificationChannel(MAIN_CHANNEL_ID, App.getInstance().getString(R.string.main_channel_description), NotificationManager.IMPORTANCE_HIGH);
                nc.setDescription(App.getInstance().getString(R.string.main_channel_description));
                nc.enableLights(true);
                nc.setLightColor(Color.RED);
                nc.enableVibration(true);
                mNotificationManager.createNotificationChannel(nc);
                // создам канал уведомлений о важных событиях
                nc = new NotificationChannel(PRIORITY_CHANNEL_ID, App.getInstance().getString(R.string.main_channel_description), NotificationManager.IMPORTANCE_HIGH);
                nc.setDescription(App.getInstance().getString(R.string.main_channel_description));
                nc.enableLights(true);
                nc.setLightColor(Color.RED);
                nc.enableVibration(true);
                mNotificationManager.createNotificationChannel(nc);
            }
        }
    }

    public static MyNotify getInstance() {
        if(instance == null){
            instance = new MyNotify();
        }
        return instance;
    }

    public void showTestNotification() {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(App.getInstance(), MAIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_settings_24)
                .setContentTitle("Тут заголовок оповещения")
                .setContentText("Тут текст оповещения")
                .setPriority(Notification.PRIORITY_HIGH)
                .setColor(Color.RED)
                .setAutoCancel(true);
        Notification notification = notificationBuilder.build();
        mNotificationManager.notify(mLastNotificationId, notification);
        mLastNotificationId++;
    }

    public void notifyTaskCreated(String task_id) {
        Intent fullScreenIntent = new Intent(App.getInstance(), IncomingTaskActivity.class);
        fullScreenIntent.putExtra(IncomingTaskActivity.TASK_ID, task_id);
        fullScreenIntent.putExtra(IncomingTaskActivity.NOTIFICATION_ID, mLastNotificationId);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(App.getInstance(), 0,
                fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(App.getInstance(), PRIORITY_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_baseline_check_24)
                        .setContentTitle("Новая задача")
                        .setContentText("Поступила новая задача")
                        .setColor(Color.RED)
                        .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                        .setSound(Settings.System.DEFAULT_ALARM_ALERT_URI)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                        .setContentIntent(fullScreenPendingIntent)
                        // Use a full-screen intent only for the highest-priority alerts where you
                        // have an associated activity that you would like to launch after the user
                        // interacts with the notification. Also, if your app targets Android 10
                        // or higher, you need to request the USE_FULL_SCREEN_INTENT permission in
                        // order for the platform to invoke this notification.
                        .setFullScreenIntent(fullScreenPendingIntent, true);

        Notification incomingCallNotification = notificationBuilder.build();
        mNotificationManager.notify(mLastNotificationId, incomingCallNotification);
        mLastNotificationId++;
    }
}
