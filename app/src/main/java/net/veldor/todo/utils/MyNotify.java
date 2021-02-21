package net.veldor.todo.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import net.veldor.todo.App;
import net.veldor.todo.BuildConfig;
import net.veldor.todo.MainActivity;
import net.veldor.todo.R;
import net.veldor.todo.ui.IncomingTaskDetailsActivity;
import net.veldor.todo.ui.OutgoingTaskDetailsActivity;

public class MyNotify {
    private static final String MAIN_CHANNEL_ID = "main";
    private static final String PRIORITY_CHANNEL_ID = "priority";
    public static final int NEW_TASKS_NOTIFICATION = 5;
    private static final String SILENT_CHANNEL_ID = "silent";
    private static MyNotify instance;
    private final NotificationManager mNotificationManager;
    private int mLastNotificationId = 100;

    private MyNotify() {
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
                nc.setLightColor(Color.GREEN);
                nc.enableVibration(true);
                mNotificationManager.createNotificationChannel(nc);


                // создам канал уведомлений о важных событиях
                NotificationChannel nc1 = new NotificationChannel(PRIORITY_CHANNEL_ID, App.getInstance().getString(R.string.high_priority_channel_description), NotificationManager.IMPORTANCE_HIGH);
                nc1.setDescription(App.getInstance().getString(R.string.high_priority_channel_description));
                nc1.enableVibration(true);
                nc1.enableLights(true);
                nc1.setLightColor(Color.RED);
                nc1.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                mNotificationManager.createNotificationChannel(nc1);

                // создам бесшумный канал
                Uri sound = Uri.parse(
                        "android.resource://" +
                                App.getInstance().getPackageName() +
                                "/" + R.raw.silence);
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build();
                NotificationChannel nc2 = new NotificationChannel(SILENT_CHANNEL_ID, App.getInstance().getString(R.string.silent_channel_description), NotificationManager.IMPORTANCE_HIGH);
                nc2.setDescription(App.getInstance().getString(R.string.silent_channel_description));
                nc2.setSound(sound, audioAttributes);
                nc2.enableVibration(false);
                nc2.enableLights(true);
                nc2.setLightColor(Color.BLUE);
                nc2.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                mNotificationManager.createNotificationChannel(nc2);
            }
        }
    }

    public static MyNotify getInstance() {
        if (instance == null) {
            instance = new MyNotify();
        }
        return instance;
    }

    public void notifyTaskCreated(String taskId, String initiator, String header) {
        NotificationCompat.Builder notificationBuilder;
        if (Preferences.getInstance().isCreateNewTaskWindow()) {
            Intent fullScreenIntent = new Intent(App.getInstance(), IncomingTaskDetailsActivity.class);
            fullScreenIntent.putExtra(IncomingTaskDetailsActivity.TASK_ID, taskId);
            fullScreenIntent.putExtra(IncomingTaskDetailsActivity.NOTIFICATION_ID, mLastNotificationId);
            PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(App.getInstance(), 0,
                    fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            notificationBuilder =
                    new NotificationCompat.Builder(App.getInstance(), PRIORITY_CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_baseline_check_24)
                            .setContentTitle("Новая задача")
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("От: " + initiator + " \n Тема: " + header))
                            .setColor(Color.RED)
                            .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                            .setSound(Settings.System.DEFAULT_ALARM_ALERT_URI)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setCategory(NotificationCompat.CATEGORY_CALL)
                            .setContentIntent(fullScreenPendingIntent)
                            .setFullScreenIntent(fullScreenPendingIntent, true);

        } else {
            notificationBuilder =
                    new NotificationCompat.Builder(App.getInstance(), PRIORITY_CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_baseline_check_24)
                            .setContentTitle("Новая задача")
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("От: " + initiator + " \n Тема: " + header))
                            .setColor(Color.RED)
                            .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                            .setSound(Settings.System.DEFAULT_ALARM_ALERT_URI)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setCategory(NotificationCompat.CATEGORY_CALL);
        }
        Notification incomingCallNotification = notificationBuilder.build();
        mNotificationManager.notify(mLastNotificationId, incomingCallNotification);
        mLastNotificationId++;
    }

    public void hideMessage(int callingMessageId) {
        mNotificationManager.cancel(callingMessageId);
    }

    public void notifyTaskAccepted(String task_id, String executor, String task_header) {
        Intent contentIntent = new Intent(App.getInstance(), OutgoingTaskDetailsActivity.class);
        contentIntent.putExtra(OutgoingTaskDetailsActivity.TASK_ID, task_id);
        contentIntent.putExtra(OutgoingTaskDetailsActivity.NOTIFICATION_ID, mLastNotificationId);
        PendingIntent pendingIntent = PendingIntent.getActivity(App.getInstance(), 0,
                contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(App.getInstance(), PRIORITY_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_baseline_check_24)
                        .setContentTitle("Задаче назначен исполнитель")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Задачу " + task_header + " \n решает " + executor + ". Нажмите на уведомление, чтобы увидеть подробности"))
                        .setColor(Color.GREEN)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        Notification incomingCallNotification = notificationBuilder.build();
        mNotificationManager.notify(mLastNotificationId, incomingCallNotification);
        mLastNotificationId++;
    }

    public void notifyTaskCancelled(String task_id, String task_header) {
        Intent contentIntent = new Intent(App.getInstance(), IncomingTaskDetailsActivity.class);
        contentIntent.putExtra(IncomingTaskDetailsActivity.TASK_ID, task_id);
        contentIntent.putExtra(IncomingTaskDetailsActivity.NOTIFICATION_ID, mLastNotificationId);
        PendingIntent pendingIntent = PendingIntent.getActivity(App.getInstance(), 0,
                contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(App.getInstance(), PRIORITY_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_baseline_check_24)
                        .setContentTitle("Задача отменена")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Задача " + task_header + " отменена пользователем и больше неактуальна. Нажмите на уведомоелние, чтобы увидеть подробности"))
                        .setColor(Color.YELLOW)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        Notification incomingCallNotification = notificationBuilder.build();
        mNotificationManager.notify(mLastNotificationId, incomingCallNotification);
        mLastNotificationId++;
    }

    public void showHasNewTasks(int size) {
        Log.d("surprise", "MyNotify showHasNewTasks 149: show has new tasks1");
        Intent contentIntent = new Intent(App.getInstance(), MainActivity.class);
        contentIntent.putExtra(MainActivity.START_FRAGMENT, MainActivity.INCOMING_FRAGMENT);
        PendingIntent pendingIntent = PendingIntent.getActivity(App.getInstance(), 0,
                contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder;
        if (Preferences.getInstance().isActiveTime() && !Preferences.getInstance().isSilent()) {
            notificationBuilder =
                    new NotificationCompat.Builder(App.getInstance(), PRIORITY_CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_baseline_check_24)
                            .setContentTitle("Имеются непринятые заявки")
                            .setNumber(size)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setContentText("Нажмите, чтобы увидеть подробности")
                            .setLights(Color.YELLOW, 500, 500)
                            .setChannelId(PRIORITY_CHANNEL_ID)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent);
        } else {
            notificationBuilder =
                    new NotificationCompat.Builder(App.getInstance(), SILENT_CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_baseline_check_24)
                            .setContentTitle("Имеются непринятые заявки")
                            .setNumber(size)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setContentText("Нажмите, чтобы увидеть подробности")
                            .setLights(Color.YELLOW, 500, 500)
                            .setChannelId(SILENT_CHANNEL_ID)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                            .setVibrate(new long[]{0L})
                            .setContentIntent(pendingIntent);
        }

        Notification incomingCallNotification = notificationBuilder.build();
        mNotificationManager.notify(NEW_TASKS_NOTIFICATION, incomingCallNotification);
    }

    public void notifyTaskFinished(String task_id, String task_header) {
        Intent contentIntent = new Intent(App.getInstance(), OutgoingTaskDetailsActivity.class);
        contentIntent.putExtra(OutgoingTaskDetailsActivity.TASK_ID, task_id);
        contentIntent.putExtra(OutgoingTaskDetailsActivity.NOTIFICATION_ID, mLastNotificationId);
        PendingIntent pendingIntent = PendingIntent.getActivity(App.getInstance(), 0,
                contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(App.getInstance(), PRIORITY_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_baseline_check_24)
                        .setContentTitle("Задача выполнена")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Задача " + task_header + " решена. Нажмите на уведомление, чтобы увидеть подробности."))
                        .setColor(Color.GREEN)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        Notification incomingCallNotification = notificationBuilder.build();
        mNotificationManager.notify(mLastNotificationId, incomingCallNotification);
        mLastNotificationId++;
    }

    public void notifyTaskDismissed(String task_id, String task_header, String reason) {
        Intent contentIntent = new Intent(App.getInstance(), OutgoingTaskDetailsActivity.class);
        contentIntent.putExtra(OutgoingTaskDetailsActivity.TASK_ID, task_id);
        contentIntent.putExtra(OutgoingTaskDetailsActivity.NOTIFICATION_ID, mLastNotificationId);
        PendingIntent pendingIntent = PendingIntent.getActivity(App.getInstance(), 0,
                contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(App.getInstance(), PRIORITY_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_baseline_check_24)
                        .setContentTitle("Задача отменена исполнителем")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Задачу " + task_header + " отменил исполнитель. Причина: " + reason))
                        .setColor(Color.RED)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        Notification incomingCallNotification = notificationBuilder.build();
        mNotificationManager.notify(mLastNotificationId, incomingCallNotification);
        mLastNotificationId++;
    }
}
