package net.veldor.todo.view_models;

import androidx.documentfile.provider.DocumentFile;
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
import net.veldor.todo.workers.SendNewTaskWorker;

import java.io.File;

public class NewTaskViewModel extends ViewModel {

    public LiveData<WorkInfo> sendTask(String title, String text, String target, File photoFile, DocumentFile zipFile) {
        Data inputData = new Data.Builder()
                .putString(SendNewTaskWorker.TITLE, title)
                .putString(SendNewTaskWorker.BODY, text)
                .putString(SendNewTaskWorker.TARGET, target)
                .build();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        SendNewTaskWorker.image = photoFile;
        SendNewTaskWorker.zip = zipFile;

        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(SendNewTaskWorker.class).addTag(SendNewTaskWorker.ACTION).setInputData(inputData).setConstraints(constraints).build();
        WorkManager.getInstance(App.getInstance()).enqueueUniqueWork(SendNewTaskWorker.ACTION, ExistingWorkPolicy.REPLACE, work);
        return WorkManager.getInstance(App.getInstance()).getWorkInfoByIdLiveData(work.getId());
    }
}
