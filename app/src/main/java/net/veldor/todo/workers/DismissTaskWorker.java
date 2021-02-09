package net.veldor.todo.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import net.veldor.todo.App;

import java.util.Map;

import static java.util.Map.entry;

public class DismissTaskWorker extends ConnectWorker {


    public static final String ACTION = "dismiss task";
    public static final String TASK_ID = "task id";
    public static final String REASON = "reason";

    public DismissTaskWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Data data = getInputData();
            String taskId = data.getString(TASK_ID);
            String reason = data.getString(REASON);
            Map<String, String> args = Map.ofEntries(
                    entry("taskId", taskId),
                    entry("reason", reason)
            );
            String answer = handleRequest("dismissTask", args);
            if (answer != null) {
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
