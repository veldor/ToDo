package net.veldor.todo.utils;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import net.veldor.todo.App;

public class Preferences {
    private static final String AUTH_TOKEN = "auth token";
    private static final String PREFERENCE_FIREBASE_TOKEN = "firebase token";
    private static final String ROLE = "role";
    private static Preferences instance;
    public final SharedPreferences mSharedPreferences;

    public static Preferences getInstance(){
        if(instance == null){
            instance = new  Preferences();
        }
        return instance;
    }

    private Preferences(){
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
    public String getFirebaseToken(){
        return mSharedPreferences.getString(PREFERENCE_FIREBASE_TOKEN, null);
    }
}
