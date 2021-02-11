package net.veldor.todo.utils;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import androidx.core.content.FileProvider;

import net.veldor.todo.App;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class FilesHandler {
    public static byte[] isToBytes(InputStream fos) throws IOException {
        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = fos.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }

    public static void requestOpenImageFile(File imageFile) {
        Uri mImageCaptureUri = FileProvider.getUriForFile(
                App.getInstance(),
                App.getInstance().getApplicationContext()
                        .getPackageName() + ".provider", imageFile);

        Intent view = new Intent();
        view.setAction(Intent.ACTION_VIEW);
        view.setData(mImageCaptureUri);
        List<ResolveInfo> resInfoList =
                App.getInstance().getPackageManager()
                        .queryIntentActivities(view, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo: resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            App.getInstance().grantUriPermission(packageName, mImageCaptureUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        view.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        Intent intent = new Intent();
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(mImageCaptureUri, "image/*");
        App.getInstance().startActivity(intent);
    }
    public static void requestOpenZipFile(File imageFile) {
        Uri mImageCaptureUri = FileProvider.getUriForFile(
                App.getInstance(),
                App.getInstance().getApplicationContext()
                        .getPackageName() + ".provider", imageFile);

        Intent view = new Intent();
        view.setAction(Intent.ACTION_VIEW);
        view.setData(mImageCaptureUri);
        List<ResolveInfo> resInfoList =
                App.getInstance().getPackageManager()
                        .queryIntentActivities(view, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo: resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            App.getInstance().grantUriPermission(packageName, mImageCaptureUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        view.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        Intent intent = new Intent();
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(mImageCaptureUri, "application/zip");
        App.getInstance().startActivity(intent);
    }
}
