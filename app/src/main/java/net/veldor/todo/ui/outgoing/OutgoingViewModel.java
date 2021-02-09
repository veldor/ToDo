package net.veldor.todo.ui.outgoing;

import androidx.lifecycle.ViewModel;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import net.veldor.todo.App;
import net.veldor.todo.selections.TaskItem;
import net.veldor.todo.workers.CancelTaskWorker;
import net.veldor.todo.workers.UpdateOutgoingTaskListWorker;

public class OutgoingViewModel extends ViewModel {

    public void updateTaskList() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest loginWork = new OneTimeWorkRequest.Builder(UpdateOutgoingTaskListWorker.class).addTag(UpdateOutgoingTaskListWorker.ACTION).setConstraints(constraints).build();
        WorkManager.getInstance(App.getInstance()).enqueueUniqueWork(UpdateOutgoingTaskListWorker.ACTION, ExistingWorkPolicy.REPLACE, loginWork);
    }

    public void cancelTask(TaskItem taskItem) {
        Data inputData = new Data.Builder()
                .putString(CancelTaskWorker.TASK_ID, taskItem.id)
                .build();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(CancelTaskWorker.class).addTag(CancelTaskWorker.ACTION).setInputData(inputData).setConstraints(constraints).build();
        WorkManager.getInstance(App.getInstance()).enqueueUniqueWork(CancelTaskWorker.ACTION, ExistingWorkPolicy.REPLACE, work);
    }
}