package net.veldor.todo.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import net.veldor.todo.App;
import net.veldor.todo.utils.FilesHandler;
import net.veldor.todo.utils.Preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendNewTaskWorker extends Worker {


    public static final String ACTION = "send new task";
    public static final String TITLE = "title";
    public static final String BODY = "body";
    public static final String TARGET = "target";
    public static File image;
    public static DocumentFile zip;

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
            URL url = new URL(App.API_ADDRESS);
            OkHttpClient client = new OkHttpClient();
            MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("cmd", "newTask")
                    .addFormDataPart("token", Preferences.getInstance().getToken())
                    .addFormDataPart("title", title)
                    .addFormDataPart("text", text)
                    .addFormDataPart("target", target);
            if (image != null && image.isFile() && image.length() > 0) {
                requestBodyBuilder.addFormDataPart("task_image", image.getName(), RequestBody.create(image, MediaType.parse("image/jpeg")));
            }
            if (zip != null && zip.isFile() && zip.length() > 0) {
                InputStream is = App.getInstance().getContentResolver().openInputStream(zip.getUri());
                requestBodyBuilder.addFormDataPart("task_document", zip.getName(), RequestBody.create(FilesHandler.isToBytes(is), MediaType.parse("application/zip")));
                Log.d("surprise", "doWork:73 appended zip");
            }
            RequestBody requestBody = requestBodyBuilder.build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            Log.d("surprise", "SendNewTaskWorker doWork 79: send add task request " + request.body().toString());
            Response response = client.newCall(request).execute();
            if (response.body() != null) {
                Log.d("surprise", "doWork:98 have answer " + response.body().string());
            }
            return Result.success();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("surprise", "doWork:101 error request ");
            e.printStackTrace();
        }

        return Result.failure();
    }
}
