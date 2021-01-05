package net.veldor.todo.view_models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import net.veldor.todo.App;
import net.veldor.todo.R;
import net.veldor.todo.workers.LoginWorker;
import net.veldor.todo.workers.UpdateTaskListWorker;

import static net.veldor.todo.workers.LoginWorker.LOGIN_ACTION;
import static net.veldor.todo.workers.LoginWorker.USER_LOGIN;
import static net.veldor.todo.workers.LoginWorker.USER_PASSWORD;
import static net.veldor.todo.workers.UpdateTaskListWorker.ACTION;

public class MainViewModel extends ViewModel {

    public void updateTaskList() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest loginWork = new OneTimeWorkRequest.Builder(UpdateTaskListWorker.class).addTag(ACTION).setConstraints(constraints).build();
        WorkManager.getInstance(App.getInstance()).enqueueUniqueWork(ACTION, ExistingWorkPolicy.REPLACE, loginWork);
    }
}
