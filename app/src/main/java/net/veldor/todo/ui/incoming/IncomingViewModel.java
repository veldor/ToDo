package net.veldor.todo.ui.incoming;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import net.veldor.todo.App;
import net.veldor.todo.workers.UpdateTaskListWorker;

import static net.veldor.todo.workers.UpdateTaskListWorker.ACTION;

public class IncomingViewModel extends ViewModel {
    public void updateTaskList() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest loginWork = new OneTimeWorkRequest.Builder(UpdateTaskListWorker.class).addTag(ACTION).setConstraints(constraints).build();
        WorkManager.getInstance(App.getInstance()).enqueueUniqueWork(ACTION, ExistingWorkPolicy.REPLACE, loginWork);
    }
}