package net.veldor.todo.workers;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.veldor.todo.App;
import net.veldor.todo.selections.LoginResponse;
import net.veldor.todo.selections.RefreshDataResponse;
import net.veldor.todo.selections.TaskItem;
import net.veldor.todo.utils.Preferences;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UpdateTaskListWorker extends Worker {


    public static final String ACTION = "update task list";

    public UpdateTaskListWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            URL url = new URL("https://rdcnn.ru/personal-api");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            String jsonInputString = "{\"cmd\":\"getTaskList\", \"token\":\"" + Preferences.getInstance().getToken() + "\"}";
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                if (response != null) {
                    GsonBuilder builder = new GsonBuilder();
                    Gson responseGson = builder.create();
                    RefreshDataResponse resp = responseGson.fromJson(response.toString(), RefreshDataResponse.class);
                    if (resp.list != null && resp.list.size() > 0) {
                        for (TaskItem i :
                                resp.list
                        ) {
                            switch (i.target){
                                case "office":
                                    i.target = "Офис";
                                    break;
                                case "it":
                                    i.target = "IT";
                                    break;
                                case "engeneer":
                                    i.target = "Инженерная служба";
                                    break;
                            }

                            switch (i.task_status){
                                case "created":
                                    i.task_status = "Ожидает подтвержения";
                                    i.sideColor = Color.parseColor("#FFC107");
                                    break;
                                case "accepted":
                                    i.task_status = "В работе";
                                    i.sideColor = Color.parseColor("#03A9F4");
                                    break;
                                case "finished":
                                    i.task_status = "Завершено";
                                    i.sideColor = Color.parseColor("#8BC34A");
                                    break;
                                case "cancelled":
                                    i.task_status = "Отменено";
                                    i.sideColor = Color.parseColor("#FF5722");
                                    break;
                            }
                        }
                    }
                    App.getInstance().mCurrentList.postValue(resp);
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Result.failure();
    }
}
