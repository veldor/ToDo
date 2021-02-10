package net.veldor.todo.utils;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "surprise";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            // проверю, что произошло. Если поступила информация о новой задаче- выведу сообщение
            if(data.containsKey("action")){
                switch (data.get("action")){
                    case "task_created":
                        MyNotify.getInstance().notifyTaskCreated(data.get("task_id"), data.get("initiator"), data.get("task_header"));
                        break;
                    case "task_accepted":
                        MyNotify.getInstance().notifyTaskAccepted(data.get("task_id"), data.get("executor"), data.get("task_header"));
                        break;
                    case "task_finished":
                        MyNotify.getInstance().notifyTaskFinished(data.get("task_id"), data.get("task_header"));
                        break;
                    case "task_cancelled":
                        MyNotify.getInstance().notifyTaskCancelled(data.get("task_id"), data.get("task_header"));
                        break;
                    case "task_dismissed":
                        MyNotify.getInstance().notifyTaskDismissed(data.get("task_id"), data.get("task_header"), data.get("reason"));
                        break;

                }
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    @Override
    public void onNewToken(@NotNull String token) {
        Preferences.getInstance().setFirebaseToken(token);
    }
    // [END on_new_token]
}
