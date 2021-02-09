package net.veldor.todo;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import net.veldor.todo.selections.GetTaskInfoResponse;
import net.veldor.todo.selections.RefreshDataResponse;
import net.veldor.todo.utils.FirebaseHandler;
import net.veldor.todo.utils.Preferences;
import net.veldor.todo.workers.CheckStatusWorker;
import net.veldor.todo.workers.ConnectPostWorker;
import net.veldor.todo.workers.UpdateIncomingTaskListWorker;
import net.veldor.todo.workers.UpdateOutgoingTaskListWorker;

import java.util.concurrent.TimeUnit;

public class App extends Application {
    private static App instance;

    public static final String API_ADDRESS = "https://rdc-scheluler.ru/api";

    public final MutableLiveData<String> mRequestStatus = new MutableLiveData<>();
    public String mLoginError;
    public final MutableLiveData<RefreshDataResponse> mCurrentList = new MutableLiveData<>();
    public final MutableLiveData<RefreshDataResponse> mCurrentIncomingList = new MutableLiveData<>();
    public final MutableLiveData<GetTaskInfoResponse> mTaskInfo = new MutableLiveData<>();

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        startMainWorker();
        if (Preferences.getInstance().getFirebaseToken() == null) {
            (new FirebaseHandler()).getToken();
        }
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(ConnectPostWorker.class).addTag(ConnectPostWorker.ACTION).build();
        WorkManager.getInstance(this).enqueueUniqueWork(ConnectPostWorker.ACTION, ExistingWorkPolicy.REPLACE, work);


    }

    public void startMainWorker() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        // запущу рабочего, который периодически будет обновлять данные
        PeriodicWorkRequest periodicTask = new PeriodicWorkRequest.Builder(CheckStatusWorker.class, 15, TimeUnit.MINUTES).setConstraints(constraints).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(CheckStatusWorker.ACTION, ExistingPeriodicWorkPolicy.REPLACE, periodicTask);
    }

    public void updateOutgoingTaskList() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(UpdateOutgoingTaskListWorker.class).addTag(UpdateOutgoingTaskListWorker.ACTION).setConstraints(constraints).build();
        WorkManager.getInstance(this).enqueueUniqueWork(UpdateOutgoingTaskListWorker.ACTION, ExistingWorkPolicy.REPLACE, work);
    }
    public void updateIncomingTaskList() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(UpdateIncomingTaskListWorker.class).addTag(UpdateOutgoingTaskListWorker.ACTION).setConstraints(constraints).build();
        WorkManager.getInstance(this).enqueueUniqueWork(UpdateIncomingTaskListWorker.ACTION, ExistingWorkPolicy.REPLACE, work);
    }
}
