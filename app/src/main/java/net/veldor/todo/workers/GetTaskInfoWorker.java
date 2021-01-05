package net.veldor.todo.workers;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.veldor.todo.App;
import net.veldor.todo.selections.GetTaskInfoResponse;
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

public class GetTaskInfoWorker extends Worker {


    public static final String ACTION = "getTaskInfo";
    public static final String TASK_ID = "task id";

    public GetTaskInfoWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Data data = getInputData();
            String taskId = data.getString(TASK_ID);
            URL url = new URL("https://rdcnn.ru/personal-api");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            String jsonInputString = "{\"cmd\":\"getTaskInfo\", \"token\":\"" + Preferences.getInstance().getToken() + "\", \"taskId\":\"" + taskId + "\"}";
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
                    GetTaskInfoResponse resp = responseGson.fromJson(response.toString(), GetTaskInfoResponse.class);
                    App.getInstance().mTaskInfo.postValue(resp);
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
