package net.veldor.todo.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.veldor.todo.selections.NewTaskResponse;
import net.veldor.todo.utils.MyNotify;
import net.veldor.todo.utils.Preferences;

public class CheckStatusWorker extends  ConnectWorker{

    public static final String ACTION = "check status";

    public CheckStatusWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // если пользователь не обычный пользователь и время проверки не слишком раннее или позднее-
        // проверю, нет ли новых непринятых задач
        if( Preferences.getInstance().getRole() > 1){
            // проверю новые данные
            try {
                String answer = handleRequest("getNewTasks", null);
                Log.d("surprise", "CheckStatusWorker doWork 35: " + answer);
                if(answer != null){
                    GsonBuilder builder = new GsonBuilder();
                    Gson responseGson = builder.create();
                    NewTaskResponse resp = responseGson.fromJson(answer, NewTaskResponse.class);
                    if(resp != null &&  resp.new_tasks_count > 0){
                        MyNotify.getInstance().showHasNewTasks(resp.new_tasks_count);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Result.success();
    }
}
