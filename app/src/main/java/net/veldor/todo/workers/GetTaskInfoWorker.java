package net.veldor.todo.workers;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.veldor.todo.App;
import net.veldor.todo.R;
import net.veldor.todo.selections.GetTaskInfoResponse;
import net.veldor.todo.utils.TimeHandler;

import java.util.Map;

import static java.util.Map.entry;

public class GetTaskInfoWorker extends ConnectWorker {


    public static final String ACTION = "getTaskInfo";
    public static final String TASK_ID = "task id";

    public GetTaskInfoWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data data = getInputData();
        String taskId = data.getString(TASK_ID);
        Map<String, String> args = Map.ofEntries(
                entry("taskId", taskId)
        );
        try {
            String answer = handleRequest("getTaskInfo", args);
            Log.d("surprise", "GetTaskInfoWorker doWork 42: ANSWER info is " + answer);
            if (answer != null) {
                GsonBuilder builder = new GsonBuilder();
                Gson responseGson = builder.create();
                GetTaskInfoResponse resp = responseGson.fromJson(answer, GetTaskInfoResponse.class);
                if(resp.task_info.task_creation_time > 0){
                    resp.task_info.task_creation_time_formatted = TimeHandler.formatTime(resp.task_info.task_creation_time);
                }
                else{
                    resp.task_info.task_creation_time_formatted = "Ещё не назначено";
                }
                if(resp.task_info.task_accept_time > 0){
                    resp.task_info.task_accept_time_formatted = TimeHandler.formatTime(resp.task_info.task_accept_time);
                }
                else{
                    resp.task_info.task_accept_time_formatted = "Ещё не назначено";
                }
                if(resp.task_info.task_planned_finish_time > 0){
                    resp.task_info.task_planned_finish_time_formatted = TimeHandler.formatTime(resp.task_info.task_planned_finish_time);
                }
                else{
                    resp.task_info.task_planned_finish_time_formatted = "Ещё не назначено";
                }
                if(resp.task_info.task_finish_time > 0){
                    resp.task_info.task_finish_time_formatted = TimeHandler.formatTime(resp.task_info.task_finish_time);
                }
                else{
                    resp.task_info.task_finish_time_formatted = "Ещё не назначено";
                }
                if(resp.task_info.executor == null || resp.task_info.executor.isEmpty()){
                    resp.task_info.executor = App.getInstance().getString(R.string.executor_not_set_message);
                }

                switch (resp.task_info.task_status) {
                    case "created":
                        resp.task_info.task_status = "Ожидает подтвержения";
                        resp.task_info.task_status_code = 1;
                        resp.task_info.sideColor = Color.parseColor("#FFC107");
                        break;
                    case "accepted":
                        resp.task_info.task_status = "В работе";
                        resp.task_info.task_status_code = 2;
                        resp.task_info.sideColor = Color.parseColor("#03A9F4");
                        break;
                    case "finished":
                        resp.task_info.task_status = "Завершено";
                        resp.task_info.task_status_code = 3;
                        resp.task_info.sideColor = Color.parseColor("#8BC34A");
                        break;
                    case "cancelled":
                        resp.task_info.task_status = "Отменено";
                        resp.task_info.task_status_code = 4;
                        resp.task_info.sideColor = Color.parseColor("#FF5722");
                        break;
                }
                App.getInstance().mTaskInfo.postValue(resp);
                return Result.success();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.failure();
    }
}
