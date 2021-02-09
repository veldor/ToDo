package net.veldor.todo.view_models;

import androidx.lifecycle.ViewModel;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import net.veldor.todo.App;
import net.veldor.todo.workers.UpdateOutgoingTaskListWorker;

import static net.veldor.todo.workers.UpdateOutgoingTaskListWorker.ACTION;

public class MainViewModel extends ViewModel {

    public void updateTaskList() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest loginWork = new OneTimeWorkRequest.Builder(UpdateOutgoingTaskListWorker.class).addTag(ACTION).setConstraints(constraints).build();
        WorkManager.getInstance(App.getInstance()).enqueueUniqueWork(ACTION, ExistingWorkPolicy.REPLACE, loginWork);
    }
}
