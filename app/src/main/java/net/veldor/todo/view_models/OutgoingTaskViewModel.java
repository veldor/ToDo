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
import net.veldor.todo.workers.CancelTaskWorker;
import net.veldor.todo.workers.GetTaskInfoWorker;

public class OutgoingTaskViewModel extends ViewModel {

    public LiveData<WorkInfo> getTaskInfo(String task_id) {
        Data inputData = new Data.Builder()
                .putString(GetTaskInfoWorker.TASK_ID, task_id)
                .build();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(GetTaskInfoWorker.class).addTag(GetTaskInfoWorker.ACTION).setInputData(inputData).setConstraints(constraints).build();
        WorkManager.getInstance(App.getInstance()).enqueueUniqueWork(GetTaskInfoWorker.ACTION, ExistingWorkPolicy.REPLACE, work);
        return WorkManager.getInstance(App.getInstance()).getWorkInfoByIdLiveData(work.getId());
    }

    public LiveData<WorkInfo> cancelTask(TaskItem mData) {
        Data inputData = new Data.Builder()
                .putString(CancelTaskWorker.TASK_ID, mData.id)
                .build();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(CancelTaskWorker.class).addTag(CancelTaskWorker.ACTION).setInputData(inputData).setConstraints(constraints).build();
        WorkManager.getInstance(App.getInstance()).enqueueUniqueWork(CancelTaskWorker.ACTION, ExistingWorkPolicy.REPLACE, work);
        return WorkManager.getInstance(App.getInstance()).getWorkInfoByIdLiveData(work.getId());
    }

}
