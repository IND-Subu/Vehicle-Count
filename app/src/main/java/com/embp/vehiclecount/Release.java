package com.embp.vehiclecount;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Release {
    @SerializedName("tag_name")
    private String tagName;

    @SerializedName("assets")
    private List<Asset> assets;


    public String getTagName() {
        return tagName;
    }

    public List<Asset> getAssets() {
        return assets;
    }
}

