package net.veldor.todo.ui.incoming;

import androidx.lifecycle.ViewModel;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import net.veldor.todo.App;
import net.veldor.todo.workers.UpdateIncomingTaskListWorker;

public class IncomingViewModel extends ViewModel {
    public void updateTaskList() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(UpdateIncomingTaskListWorker.class).addTag(UpdateIncomingTaskListWorker.ACTION).setConstraints(constraints).build();
        WorkManager.getInstance(App.getInstance()).enqueueUniqueWork(UpdateIncomingTaskListWorker.ACTION, ExistingWorkPolicy.REPLACE, work);
    }
}