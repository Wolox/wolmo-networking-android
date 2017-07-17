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

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Retrofit;

/**
 * This class handles {@link Retrofit} main class initialization and services instances to perform
 * API calls to several endpoints.
 */
public class RetrofitServices {

    private Retrofit mRetrofit;
    private Map<Class, Object> mServices;

    @Inject
    public RetrofitServices(Retrofit retrofit) {
        mRetrofit = retrofit;
        mServices = new HashMap<>();
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
    @SuppressWarnings("unchecked")
    public <T> T getService(@NonNull Class<T> clazz) {
        T service = (T) mServices.get(clazz);
        if (service != null) return service;
        service = mRetrofit.create(clazz);
        mServices.put(clazz, service);
        return service;
    }
}