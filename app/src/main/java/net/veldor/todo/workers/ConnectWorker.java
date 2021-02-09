package net.veldor.todo.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import net.veldor.todo.utils.Preferences;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ConnectWorker extends Worker {


    public static final String ACTION = "confirmTask";

    public ConnectWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        return Result.failure();
    }

    String handleRequest(String command, Map<String, String> args) throws Exception {
        HttpURLConnection con = getConnection();
        String request = getRequest(command, args);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = new byte[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                input = request.getBytes(StandardCharsets.UTF_8);
            }
            else{
                //noinspection CharsetObjectCanBeUsed
                input =request.getBytes("utf-8");
            }
            os.write(input, 0, input.length);
        }
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        return response.toString();
    }

    private String getRequest(String command, Map<String, String> args) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"cmd\":\"").append(command).append("\"");
        sb.append(",\"token\":\"").append(Preferences.getInstance().getToken()).append("\"");
        if(args != null){
            for(Map.Entry<String, String> entry : args.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                sb.append(",\"").append(key).append("\":\"").append(value).append("\"");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private HttpURLConnection getConnection() throws Exception {
        URL url = new URL("https://rdc-scheluler.ru/api");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        return con;
    }
}
