package ar.com.wolox.wolmo.networking.test_utils.service;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface RetrofitTestService {

    @GET("/api/get/")
    Call<String> retrofitGetMethodString();

    @POST("/api/post/")
    Call<String> retrofitPostMethodString();
}
