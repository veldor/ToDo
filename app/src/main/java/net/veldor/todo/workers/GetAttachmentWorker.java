package net.veldor.todo.workers;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import net.veldor.todo.utils.FilesHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static java.util.Map.entry;

public class GetAttachmentWorker extends ConnectWorker {


    public static final String ACTION = "get attachment";
    public static final String TASK_ID = "task id";

    public GetAttachmentWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Data data = getInputData();
            String taskId = data.getString(TASK_ID);
            Map<String, String> args = Map.ofEntries(
                    entry("taskId", taskId)
            );
            HttpURLConnection con = getFileConnection();
            String request = getRequest("getAttachment", args);
            Log.d("surprise", "GetImageWorker doWork 44: request is " + request);
            try (OutputStream os = con.getOutputStream()) {
                byte[] input;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    input = request.getBytes(StandardCharsets.UTF_8);
                }
                else{
                    //noinspection CharsetObjectCanBeUsed
                    input =request.getBytes("utf-8");
                }
                os.write(input, 0, input.length);
            }
            if(con.getResponseCode() == 200 && con.getContentLength() > 0){
               // определю тип
                String mime = ".zip";
                // вроде бы найдена картинка, сохраню в загрузки
                InputStream is = con.getInputStream();
                File zipFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "file_task_" + taskId + mime);
                try (FileOutputStream out = new FileOutputStream(zipFile)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    is.close();
                    // вызову окно просмотра и верну успешный результат
                    FilesHandler.requestOpenZipFile(zipFile);
                    return Result.success();
                } catch (Exception e) {
                    // TODO: handle exception
                    Log.d("surprise", "GetImageWorker doWork 73: error when saving");
                    e.printStackTrace();
                    is.close();
                }
                Log.d("surprise", "GetImageWorker doWork 73: image saved");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.failure();
    }
}
