package com.embp.vehiclecount;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.Objects;

public class DownloadReceiver extends BroadcastReceiver {



    private final Context mContent;
    @SuppressLint("StaticFieldLeak")
    private static Activity activity;

    public DownloadReceiver(Context context){
        mContent = context;
    }

    /**
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("logs", "received intent");
        String action = intent.getAction();
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadId != -1) {
                // Get the Download Manager
                DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                Cursor cursor = downloadManager.query(query);
                Log.d("logs", "No cursor found");

                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    int status = cursor.getInt(columnIndex);
                    Log.d("logs", "before executed");
                    if (status == DownloadManager.STATUS_SUCCESSFUL){
                        Log.d("logs", "after executed");

                        @SuppressLint("Range") String uriString = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        Log.d("DownloadReceiver", "URI for downloaded file: " + uriString);

                        if (uriString != null){
                            File file = new File(Objects.requireNonNull(Uri.parse(uriString).getPath()));
                            if (file.exists()){
                                if (PermissionHelper.checkReadExternalStoragePermission(activity)) {
                                    installApk(context, file);
                                }
                                else {
                                    PermissionHelper.requestReadExternalStoragePermission(activity);
                                }
                            }
                            else {
                                Toast.makeText(context, "Downloaded file not found", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            Toast.makeText(context, "URI for downloaded file is null", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show();
                    }
                    cursor.close();
                }
            }
        }
    }


    public static void installApk(Context context, File file) {
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", file);
        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        else {
            apkUri = Uri.fromFile(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS));
        }
        installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        context.startActivity(installIntent);
    }

}