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
import net.veldor.todo.selections.LoginResponse;
import net.veldor.todo.utils.Preferences;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginWorker extends Worker {
    public static final String USER_LOGIN = "user login";
    public static final String USER_PASSWORD = "user password";
    public static final String LOGIN_ACTION = "login";

    public LoginWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data data = getInputData();
        String login = data.getString(USER_LOGIN);
        String password = data.getString(USER_PASSWORD);
        try {
            URL url = new URL(App.API_ADDRESS);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            String jsonInputString = "{\"cmd\":\"login\"," +
                    " \"login\":\"" + login + "\"," +
                    " \"pass\":\"" + password + "\"," +
                    " \"is_ios\":\"0\"," +
                    " \"firebase_token\":\"" + Preferences.getInstance().getFirebaseToken() + "\"}";
            try(OutputStream os = con.getOutputStream()) {
                @SuppressWarnings("CharsetObjectCanBeUsed") byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            //noinspection CharsetObjectCanBeUsed
            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                GsonBuilder builder = new GsonBuilder();
                Gson responseGson = builder.create();
                LoginResponse resp = responseGson.fromJson(response.toString(), LoginResponse.class);
                Log.d("surprise", "LoginWorker doWork 62: " + resp.status);
                if(resp.token != null){
                    Preferences.getInstance().saveToken(resp.token);
                    Preferences.getInstance().saveRole(resp.role);
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
