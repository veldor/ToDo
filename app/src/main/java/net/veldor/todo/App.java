package net.veldor.todo;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import net.veldor.todo.selections.RefreshDataResponse;
import net.veldor.todo.utils.FirebaseHandler;
import net.veldor.todo.utils.Preferences;

public class App extends Application {
    private static App instance;

    public final MutableLiveData<String> mRequestStatus = new MutableLiveData<>();
    public String mLoginError;
    public MutableLiveData<RefreshDataResponse> mCurrentList = new MutableLiveData<>();
    public MutableLiveData<RefreshDataResponse> mCurrentIncomingList = new MutableLiveData<>();

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        if(Preferences.getInstance().getFirebaseToken() == null){
            (new FirebaseHandler()).getToken();
        }
    }
}
