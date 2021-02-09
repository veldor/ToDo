package net.veldor.todo.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.veldor.todo.App;
import net.veldor.todo.selections.GetTaskInfoResponse;

import java.util.Map;

import static java.util.Map.entry;

public class CancelTaskWorker extends ConnectWorker {


    public static final String ACTION = "cancelTask";
    public static final String TASK_ID = "task id";

    public CancelTaskWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Data data = getInputData();
            String taskId = data.getString(TASK_ID);
            Map<String, String> args = Map.ofEntries(
                    entry("taskId", taskId)
            );
            String answer = handleRequest("cancelTask", args);
            if (answer != null) {
                Log.d("surprise", "doWork:40 answer cancel task is " + answer);
                GsonBuilder builder = new GsonBuilder();
                Gson responseGson = builder.create();
                GetTaskInfoResponse resp = responseGson.fromJson(answer, GetTaskInfoResponse.class);
                App.getInstance().mTaskInfo.postValue(resp);
                App.getInstance().updateIncomingTaskList();
                App.getInstance().updateOutgoingTaskList();
                return Result.success();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.failure();
    }
}
