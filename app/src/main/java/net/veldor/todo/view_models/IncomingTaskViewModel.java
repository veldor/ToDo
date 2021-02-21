package net.veldor.todo.view_models;

import android.util.Log;

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
import net.veldor.todo.workers.ConfirmTaskWorker;
import net.veldor.todo.workers.FinishTaskWorker;
import net.veldor.todo.workers.GetAttachmentWorker;
import net.veldor.todo.workers.GetImageWorker;
import net.veldor.todo.workers.GetTaskInfoWorker;

public class IncomingTaskViewModel extends MainViewModel {

    public LiveData<WorkInfo> getTaskInfo(String task_id) {
        Log.d("surprise", "IncomingTaskViewModel getTaskInfo 25: getting task info");
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

    public LiveData<WorkInfo> confirmTask(TaskItem mData, int plannedTime) {
        Data inputData = new Data.Builder()
                .putString(ConfirmTaskWorker.TASK_ID, mData.id)
                .putInt(ConfirmTaskWorker.PLANNED_TIME, plannedTime)
                .build();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(ConfirmTaskWorker.class).addTag(ConfirmTaskWorker.ACTION).setInputData(inputData).setConstraints(constraints).build();
        WorkManager.getInstance(App.getInstance()).enqueueUniqueWork(ConfirmTaskWorker.ACTION, ExistingWorkPolicy.REPLACE, work);
        return WorkManager.getInstance(App.getInstance()).getWorkInfoByIdLiveData(work.getId());
    }

    public LiveData<WorkInfo> finishTask(TaskItem mData) {
        Data inputData = new Data.Builder()
                .putString(FinishTaskWorker.TASK_ID, mData.id)
                .build();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(FinishTaskWorker.class).addTag(FinishTaskWorker.ACTION).setInputData(inputData).setConstraints(constraints).build();
        WorkManager.getInstance(App.getInstance()).enqueueUniqueWork(FinishTaskWorker.ACTION, ExistingWorkPolicy.REPLACE, work);
        return WorkManager.getInstance(App.getInstance()).getWorkInfoByIdLiveData(work.getId());
    }

    public LiveData<WorkInfo> downloadPhoto(String id) {
        // запущу рабочего, который загрузит картинку
        Data inputData = new Data.Builder()
                .putString(CancelTaskWorker.TASK_ID, id)
                .build();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(GetImageWorker.class).addTag(GetImageWorker.ACTION).setInputData(inputData).setConstraints(constraints).build();
        WorkManager.getInstance(App.getInstance()).enqueueUniqueWork(GetImageWorker.ACTION, ExistingWorkPolicy.REPLACE, work);
        return WorkManager.getInstance(App.getInstance()).getWorkInfoByIdLiveData(work.getId());
    }

    public LiveData<WorkInfo> downloadZip(String id) {
        // запущу рабочего, который загрузит картинку
        Data inputData = new Data.Builder()
                .putString(CancelTaskWorker.TASK_ID, id)
                .build();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(GetAttachmentWorker.class).addTag(GetAttachmentWorker.ACTION).setInputData(inputData).setConstraints(constraints).build();
        WorkManager.getInstance(App.getInstance()).enqueueUniqueWork(GetAttachmentWorker.ACTION, ExistingWorkPolicy.REPLACE, work);
        return WorkManager.getInstance(App.getInstance()).getWorkInfoByIdLiveData(work.getId());
    }
}
