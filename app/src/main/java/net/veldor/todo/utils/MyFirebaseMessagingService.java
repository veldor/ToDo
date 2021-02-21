package net.veldor.todo.utils;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import net.veldor.todo.App;

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
                String action = data.get("action");
                if(action != null){
                    switch (action){
                        case "task_created":
                            App.getInstance().updateOutgoingTaskList();
                            App.getInstance().updateIncomingTaskList();
                            if(Preferences.getInstance().isShowTaskCreated() && !Preferences.getInstance().isNotDisturb()){
                                MyNotify.getInstance().notifyTaskCreated(data.get("task_id"), data.get("initiator"), data.get("task_header"));
                            }
                            break;
                        case "task_accepted":
                            App.getInstance().updateOutgoingTaskList();
                            if(Preferences.getInstance().isShowTaskAccepted() && !Preferences.getInstance().isNotDisturb()){
                                MyNotify.getInstance().notifyTaskAccepted(data.get("task_id"), data.get("executor"), data.get("task_header"));
                            }
                            break;
                        case "task_finished":
                            App.getInstance().updateOutgoingTaskList();
                            if(Preferences.getInstance().isShowTaskFinished() && !Preferences.getInstance().isNotDisturb()){
                                MyNotify.getInstance().notifyTaskFinished(data.get("task_id"), data.get("task_header"));
                            }
                            break;
                        case "task_cancelled":
                            App.getInstance().updateOutgoingTaskList();
                            App.getInstance().updateIncomingTaskList();
                            if(Preferences.getInstance().isShowTaskCancelled() && !Preferences.getInstance().isNotDisturb()){
                                MyNotify.getInstance().notifyTaskCancelled(data.get("task_id"), data.get("task_header"));
                            }
                            break;
                        case "task_dismissed":
                            App.getInstance().updateOutgoingTaskList();
                            if(Preferences.getInstance().isShowTaskDismissed() && !Preferences.getInstance().isNotDisturb()){
                                MyNotify.getInstance().notifyTaskDismissed(data.get("task_id"), data.get("task_header"), data.get("reason"));
                            }
                            break;
                        case "task_accepted_by_executor":
                            App.getInstance().updateIncomingTaskList();
                            App.getInstance().mExecutorAcceptedTask.postValue(data.get("task_id"));

                    }
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
