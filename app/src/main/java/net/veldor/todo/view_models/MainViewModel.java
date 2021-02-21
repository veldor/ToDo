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
import net.veldor.todo.selections.TaskItem;
import net.veldor.todo.workers.GetTaskInfoWorker;
import net.veldor.todo.workers.SendClaimWorker;

public class MainViewModel extends ViewModel {

    public LiveData<WorkInfo> sendClaim(String claimText, TaskItem taskItem) {
        Data inputData = new Data.Builder()
                .putString(SendClaimWorker.TASK_ID, taskItem.id)
                .putString(SendClaimWorker.CLAIM_TEXT, claimText)
                .build();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(SendClaimWorker.class).addTag(SendClaimWorker.ACTION).setInputData(inputData).setConstraints(constraints).build();
        WorkManager.getInstance(App.getInstance()).enqueueUniqueWork(SendClaimWorker.ACTION, ExistingWorkPolicy.REPLACE, work);
        return WorkManager.getInstance(App.getInstance()).getWorkInfoByIdLiveData(work.getId());
    }
}
