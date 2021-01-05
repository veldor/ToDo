package net.veldor.todo.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.veldor.todo.App;
import net.veldor.todo.selections.RefreshDataResponse;
import net.veldor.todo.utils.Preferences;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SendNewTaskWorker extends Worker {


    public static final String ACTION = "send new task";
    public static final String TITLE = "title";
    public static final String BODY = "body";
    public static final String TARGET = "target";

    public SendNewTaskWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {


        Data data = getInputData();
        String title = data.getString(TITLE);
        String text = data.getString(BODY);
        String target = data.getString(TARGET);

        try {
            URL url = new URL("https://rdcnn.ru/personal-api");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            String jsonInputString = "{\"cmd\":\"newTask\", \"title\":\"" + title + "\", \"text\":\"" + text + "\", \"target\":\"" + target + "\", \"token\":\"" + Preferences.getInstance().getToken() + "\"}";
            Log.d("surprise", "SendNewTaskWorker doWork 56: request is " + jsonInputString);
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
                    return Result.success();
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
