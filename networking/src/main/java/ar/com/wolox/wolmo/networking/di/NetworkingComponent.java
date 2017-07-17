package ar.com.wolox.wolmo.networking.di;

import android.support.annotation.Nullable;

import ar.com.wolox.wolmo.networking.di.modules.GsonModule;
import ar.com.wolox.wolmo.networking.di.modules.NetworkingModule;
import ar.com.wolox.wolmo.networking.di.modules.OkHttpClientModule;
import ar.com.wolox.wolmo.networking.retrofit.RetrofitServices;

import java.util.List;

import dagger.BindsInstance;
import dagger.Component;
import dagger.Subcomponent;
import okhttp3.Interceptor;
import retrofit2.converter.gson.GsonConverterFactory;

@Component(modules = { GsonModule.class, OkHttpClientModule.class, NetworkingModule.class })
public interface NetworkingComponent {

    RetrofitServices retrofitServices();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder baseUrl(String baseUrl);

        @BindsInstance
        Builder okHttpInterceptors(@Nullable Interceptor... interceptors);

        NetworkingComponent build();

    }
}
