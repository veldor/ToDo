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

public class ConfirmTaskWorker extends ConnectWorker {


    public static final String ACTION = "confirmTask";
    public static final String TASK_ID = "task id";
    public static final String PLANNED_TIME = "planned time";

    public ConfirmTaskWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Data data = getInputData();
            String taskId = data.getString(TASK_ID);
            String plannedTime = String.valueOf(data.getInt(PLANNED_TIME, 0));
            Map<String, String> args = Map.ofEntries(
                    entry("taskId", taskId),
                    entry("plannedTime", plannedTime)
            );
            String answer = handleRequest("confirmTask", args);
            if (answer != null) {
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
