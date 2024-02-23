package com.embp.vehiclecount;

import com.google.gson.annotations.SerializedName;

public class Asset {

    @SerializedName("browser_download_url")
    private String browserDownloadUrl;

    public String getBrowserDownloadUrl() {
        return browserDownloadUrl;
    }
}
