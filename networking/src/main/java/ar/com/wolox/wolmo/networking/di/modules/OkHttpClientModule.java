package ar.com.wolox.wolmo.networking.di.modules;

import android.support.annotation.Nullable;

import ar.com.wolox.wolmo.networking.BuildConfig;

import dagger.Module;
import dagger.Provides;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

@Module
public class OkHttpClientModule {

    @Provides
    OkHttpClient provideOkHttpClient(OkHttpClient.Builder okHttpBuilder, @Nullable Interceptor... interceptors) {
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggerInterceptor = new HttpLoggingInterceptor();
            loggerInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            okHttpBuilder.addInterceptor(loggerInterceptor);
        }

        if (interceptors != null) {
            for (Interceptor interceptor : interceptors) {
                okHttpBuilder.addInterceptor(interceptor);
            }
        }
        return okHttpBuilder.build();
    }

    @Provides
    OkHttpClient.Builder provideOkHttpClientBuilder() {
        return new OkHttpClient.Builder();
    }
}
