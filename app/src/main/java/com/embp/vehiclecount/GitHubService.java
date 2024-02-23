package com.embp.vehiclecount;


import retrofit2.Call;
import retrofit2.http.GET;

public interface GitHubService {
    @GET("releases/latest")
    Call<Release> getLatestRelease();
}