package net.veldor.todo.utils;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

public class FirebaseHandler {
    public void getToken() {
        // проверю наличие токена Firebase
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.d("surprise", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    // Get new FCM registration token
                    String token = task.getResult();
                    if (token != null) {
                        Log.d("surprise", "FirebaseHandler getToken 24: save token " + token);
                        Preferences.getInstance().setFirebaseToken(token);
                    }
                });
    }
}
