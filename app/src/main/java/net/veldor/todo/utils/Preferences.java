package net.veldor.todo.utils;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import net.veldor.todo.App;

import java.util.Calendar;

public class Preferences {
    private static final String AUTH_TOKEN = "auth token";
    private static final String PREFERENCE_FIREBASE_TOKEN = "firebase token";
    public static final String KEY_DO_NOT_DISTURB = "do not disturb";


    public static final int DEFAULT_START_HOUR = 9;
    public static final int DEFAULT_FINISH_HOUR = 23;
    // ===============

    public static final String KEY_SILENT = "silent";
    public static final String KEY_SHOW_NEW_TASK_WINDOW = "show new task window";
    public static final String KEY_SHOW_WAITING_ACCEPT = "show waiting accept";
    public static final String KEY_SHOW_TASK_ACCEPTED = "show task accepted";
    public static final String KEY_SHOW_TASK_CREATED = "show task created";
    public static final String KEY_SHOW_TASK_CANCELLED = "show task cancelled";
    public static final String KEY_SHOW_TASK_DISMISSED = "show task dismissed";
    public static final String KEY_SHOW_TASK_FINISHED = "show task finished";
    public static final String KEY_START_ACTIVE_TIME = "start check time";
    public static final String KEY_FINISH_ACTIVE_TIME = "finish check time";


    private static final String ROLE = "role";
    private static Preferences instance;
    public final SharedPreferences mSharedPreferences;

    public static Preferences getInstance() {
        if (instance == null) {
            instance = new Preferences();
        }
        return instance;
    }

    private Preferences() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
    }

    public boolean isUserUnknown() {
        return getToken() == null || getRole() == 0;
    }

    public void saveToken(String token) {
        mSharedPreferences.edit().putString(AUTH_TOKEN, token).apply();
    }

    public void saveRole(int role) {
        mSharedPreferences.edit().putInt(ROLE, role).apply();
    }

    public String getToken() {
        return mSharedPreferences.getString(AUTH_TOKEN, null);
    }

    public int getRole() {
        return mSharedPreferences.getInt(ROLE, 0);
    }

    public void setFirebaseToken(String token) {
        mSharedPreferences.edit().putString(PREFERENCE_FIREBASE_TOKEN, token).apply();
    }

    public String getFirebaseToken() {
        return mSharedPreferences.getString(PREFERENCE_FIREBASE_TOKEN, null);
    }

    public void removeToken() {
        mSharedPreferences.edit().putString(AUTH_TOKEN, null).apply();
    }

    public boolean isNotDisturb() {
        return mSharedPreferences.getBoolean(KEY_DO_NOT_DISTURB, false);
    }
    public boolean isSilent() {
        return mSharedPreferences.getBoolean(KEY_SILENT, false);
    }

    public boolean isCreateNewTaskWindow() {
        return mSharedPreferences.getBoolean(KEY_SHOW_NEW_TASK_WINDOW, true);
    }

    public boolean isShowWaitingAccept() {
        return mSharedPreferences.getBoolean(KEY_SHOW_WAITING_ACCEPT, true);
    }

    public boolean isShowTaskCreated() {
        return mSharedPreferences.getBoolean(KEY_SHOW_TASK_CREATED, true);
    }

    public boolean isShowTaskAccepted() {
        return mSharedPreferences.getBoolean(KEY_SHOW_TASK_ACCEPTED, true);
    }

    public boolean isShowTaskCancelled() {
        return mSharedPreferences.getBoolean(KEY_SHOW_TASK_CANCELLED, true);
    }

    public boolean isShowTaskDismissed() {
        return mSharedPreferences.getBoolean(KEY_SHOW_TASK_DISMISSED, true);
    }

    public boolean isShowTaskFinished() {
        return mSharedPreferences.getBoolean(KEY_SHOW_TASK_FINISHED, true);
    }

    public void setSilence(boolean value) {
        mSharedPreferences.edit().putBoolean(KEY_DO_NOT_DISTURB, value).apply();
    }

    public int getActiveStartTime() {
        return mSharedPreferences.getInt(KEY_START_ACTIVE_TIME, DEFAULT_START_HOUR * 60);
    }

    public int getActiveFinishTime() {
        return mSharedPreferences.getInt(KEY_FINISH_ACTIVE_TIME, DEFAULT_FINISH_HOUR * 60);
    }

    public boolean isActiveTime() {
        // проверю, можно ли шуметь
        int activeTimeStart = getActiveStartTime();
        int activeTimeFinish = getActiveFinishTime();
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour > activeTimeStart / 60) {
            if (hour < activeTimeFinish / 60) {
                return true;
            } else if (hour == activeTimeFinish / 60) {
                int minutes = calendar.get(Calendar.MINUTE);
                return minutes <= activeTimeFinish % 60;
            }
        } else if (hour == activeTimeStart / 60) {
            int minutes = calendar.get(Calendar.MINUTE);
            return minutes >= activeTimeStart % 60;
        }
        return false;
    }
}
