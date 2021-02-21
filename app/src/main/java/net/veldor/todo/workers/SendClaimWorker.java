package net.veldor.todo.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import net.veldor.todo.App;

import java.util.Map;

import static java.util.Map.entry;

public class SendClaimWorker extends ConnectWorker {


    public static final String ACTION = "send claim";
    public static final String TASK_ID = "task id";
    public static final String CLAIM_TEXT = "claim text";

    public SendClaimWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Data data = getInputData();
            String taskId = data.getString(TASK_ID);
            String claimText = data.getString(CLAIM_TEXT);
            Map<String, String> args = Map.ofEntries(
                    entry("taskId", taskId),
                    entry("claimText", claimText)
            );
            String answer = handleRequest("createClaim", args);
            if (answer != null) {
                return Result.success();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.failure();
    }
}
