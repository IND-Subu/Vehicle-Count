package com.embp.vehiclecount;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UpdateChecker {
    @SuppressLint("StaticFieldLeak")
    private static Activity activity;
    private static String downloadUrl = null;
    private static final String TAG = "UpdateChecker";
    private static final String GITHUB_API_BASE_URL = "https://api.github.com/repos/IND-Subu/Vehicle-Count/";
    private static final String APK_FILE_NAME = "vehicleCount.apk";
    public static void checkForUpdate(final Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GITHUB_API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GitHubService service = retrofit.create(GitHubService.class);

        Call<Release> call = service.getLatestRelease();

        call.enqueue(new Callback<Release>() {
            @Override
            public void onResponse(@NonNull Call<Release> call, @NonNull Response<Release> response) {

                if (response.isSuccessful()) {
                    Release latestRelease = response.body();
                    if (latestRelease != null && latestRelease.getAssets() != null && !latestRelease.getAssets().isEmpty()) {
                        Asset firstAsset = latestRelease.getAssets().get(0);
                        String browserDownloadUrl = firstAsset.getBrowserDownloadUrl();
                        if (browserDownloadUrl != null && !browserDownloadUrl.isEmpty()) {
                            downloadUrl = browserDownloadUrl;
                            String latestVersion = latestRelease.getTagName();
                            double latestVer = Double.parseDouble(latestVersion);
                            double currentVersion = Double.parseDouble(BuildConfig.VERSION_NAME);
                            if (currentVersion < latestVer) {
                                showUpdateDialog(context);
                            }
                        } else {
                            Log.e("UpdateChecker", "Browser download URL not found in assets");
                            Toast.makeText(activity.getApplicationContext(), "Error Code: 0x00055", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("UpdateChecker", "No assets found in the release");
                        Toast.makeText(activity.getApplicationContext(), "Error Code: 0x00059", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Release> call, @NonNull Throwable t) {
                // Handle failure
                Log.e("UpdateChecker", "Error fetching release: " + t.getMessage());
                Toast.makeText(activity.getApplicationContext(), "Error Code: 0x00068", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void showUpdateDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Update Available")
                .setMessage("A new version of the app is available. Do you want to update?")
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Check if permission is granted
                        if (PermissionHelper.checkReadExternalStoragePermission((Activity) context)) {
                            downloadApk(context);
                        } else {
                            // Permission not granted, request it
                            PermissionHelper.requestReadExternalStoragePermission((Activity) context);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User chose not to update
                        Toast.makeText(context, "Update Canceled.", Toast.LENGTH_SHORT).show();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static void downloadApk(Context context) {
        File apkFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), APK_FILE_NAME);
        if (apkFile.exists()){
            DownloadReceiver.installApk(context, apkFile);
            return;
        }
        if (downloadUrl == null || downloadUrl.isEmpty()){
            Log.d(TAG,"Download Url is Empty");
            Toast.makeText(context, "Error Code: 0x000113", Toast.LENGTH_SHORT).show();
            return;
        }

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        Uri downloadUri = Uri.parse(downloadUrl);
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setDestinationUri(Uri.fromFile(apkFile));
        request.setMimeType("application/vnd.android.package-archive");
        
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle("Downloading Update");
        request.setDescription("Downloading the latest version");
        downloadManager.enqueue(request);
    }
}
