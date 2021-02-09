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
import net.veldor.todo.workers.DismissTaskWorker;

public class DismissTaskViewModel extends ViewModel {

    public LiveData<WorkInfo> dismissTask(String taskId, String reason) {
        Data inputData = new Data.Builder()
                .putString(DismissTaskWorker.TASK_ID, taskId)
                .putString(DismissTaskWorker.REASON, reason)
                .build();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(DismissTaskWorker.class).addTag(DismissTaskWorker.ACTION).setInputData(inputData).setConstraints(constraints).build();
        WorkManager.getInstance(App.getInstance()).enqueueUniqueWork(DismissTaskWorker.ACTION, ExistingWorkPolicy.REPLACE, work);
        return WorkManager.getInstance(App.getInstance()).getWorkInfoByIdLiveData(work.getId());
    }
}
