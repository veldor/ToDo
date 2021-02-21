package net.veldor.todo.ui.incoming;

import androidx.lifecycle.ViewModel;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import net.veldor.todo.App;
import net.veldor.todo.utils.Grammar;
import net.veldor.todo.workers.UpdateIncomingTaskListWorker;
import net.veldor.todo.workers.UpdateOutgoingTaskListWorker;

public class IncomingViewModel extends ViewModel {
    public void updateTaskList(boolean[] filter, int sortingOption, int sortingReverse, int limit, int page) {
        String filterOptions = Grammar.filterToSting(filter);
        Data inputData = new Data.Builder()
                .putString(UpdateOutgoingTaskListWorker.FILTER_DATA, filterOptions)
                .putInt(UpdateOutgoingTaskListWorker.SORT_DATA, sortingOption)
                .putInt(UpdateOutgoingTaskListWorker.LIMIT_DATA, limit)
                .putInt(UpdateOutgoingTaskListWorker.PAGE_DATA, page)
                .putInt(UpdateOutgoingTaskListWorker.SORT_REVERSE_DATA, sortingReverse)
                .build();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(UpdateIncomingTaskListWorker.class).setInputData(inputData).addTag(UpdateIncomingTaskListWorker.ACTION).setConstraints(constraints).build();
        WorkManager.getInstance(App.getInstance()).enqueueUniqueWork(UpdateIncomingTaskListWorker.ACTION, ExistingWorkPolicy.REPLACE, work);
    }
}