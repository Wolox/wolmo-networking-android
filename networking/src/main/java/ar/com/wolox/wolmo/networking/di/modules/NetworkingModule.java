package ar.com.wolox.wolmo.networking.di.modules;

import com.google.gson.Gson;

import ar.com.wolox.wolmo.networking.di.NetworkingComponent;
import ar.com.wolox.wolmo.networking.retrofit.RetrofitServices;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class NetworkingModule {

    @Provides
    Retrofit provideRetrofit(String baseUrl, GsonConverterFactory gsonConverterFactory, OkHttpClient client) {
        return new Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(gsonConverterFactory)
            .client(client).build();
    }

}
