/**
 * MIT License
 * <p>
 * Copyright (c) 2017 Wolox S.A
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package ar.com.wolox.wolmo.networking.retrofit;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import ar.com.wolox.wolmo.networking.retrofit.serializer.BaseGsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * This class handles {@link Retrofit} main class initialization and services instances to perform
 * API calls to several endpoints.
 */
public abstract class RetrofitServices {

    private Retrofit mRetrofit;
    private boolean mIsInitialized = false;
    private Map<Class, Object> mServices;

    /**
     * This method must be called to start using this class. It initializes required variables
     * and Retrofit.
     * Please note that calling this method on an already initialized class will reset it to a
     * clean state, configuring Retrofit to work with the endpoint provided in getApiEndpoint()
     */
    public void init() {
        mServices = new HashMap<>();
        mRetrofit = new Retrofit.Builder()
                .baseUrl(getApiEndpoint())
                .addConverterFactory(GsonConverterFactory.create(getGson()))
                .client(getOkHttpClient())
                .build();
        mIsInitialized = true;
    }

    /**
     * Returns the API endpoint.
     *
     * @return URL endpoint
     */
    @NonNull
    public abstract String getApiEndpoint();

    /**
     * Returns an instance of Gson to use for conversion.
     * This method calls <i>initGson(builder)</i> to configure the Gson Builder.
     *
     * @return A configured Gson instance
     */
    @NonNull
    protected Gson getGson() {
        com.google.gson.GsonBuilder builder = BaseGsonBuilder.getBaseGsonBuilder();
        initGson(builder);
        return builder.create();
    }

    /**
     * Override if needed to configure a gson builder.
     * You should add serializers and/or deserializers inside this method.
     *
     * @param builder Builder to configure
     */
    protected void initGson(@NonNull com.google.gson.GsonBuilder builder) {
    }

    /**
     * Returns an OkHttpClient.
     * This method calls <i>initClient(builder)</i> to configure the builder for OkHttpClient.
     *
     * @return A configured instance of OkHttpClient.
     */
    @NonNull
    protected OkHttpClient getOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        initClient(builder);
        return builder.build();
    }

    /**
     * Configures an <i>OkHttpClient.Builder</i>.
     * You must add interceptors and configure the builder inside this method.
     */
    protected void initClient(@NonNull OkHttpClient.Builder builder) {
        HttpLoggingInterceptor loggerInterceptor = new HttpLoggingInterceptor();
        loggerInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        builder.addInterceptor(loggerInterceptor);
    }

    /**
     * Builds and returns a Retrofit Service.
     * If the service wasn't accessed, it'll be created and cached internally.
     * On successive requests, the already created instance will be returned.
     * <p>
     * Usage:
     * Override this class and define the services like this:
     * public static UserService user() {
     * return getService(UserService.class);
     * }
     *
     * @param clazz RetrofitService Class
     * @param <T>   Service class
     * @return service
     */
    public <T> T getService(@NonNull Class<T> clazz) {
        if (!mIsInitialized) throw new RuntimeException("RetrofitServices is not initialized! " +
                "Must call init() at least once before calling getService(clazz)");

        T service = (T) mServices.get(clazz);
        if (service != null) return service;
        service = mRetrofit.create(clazz);
        mServices.put(clazz, service);
        return service;
    }
}