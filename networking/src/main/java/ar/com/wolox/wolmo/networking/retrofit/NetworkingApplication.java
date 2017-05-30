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

import android.support.annotation.CallSuper;

import ar.com.wolox.wolmo.core.WoloxApplication;

/**
 * This class extends {@link WoloxApplication} to easily initialize and get and instance of
 * {@link RetrofitServices} to perform API calls.
 */
public abstract class NetworkingApplication extends WoloxApplication {

    private static RetrofitServices sRetrofitServices;

    /**
     * Overrides the {@link android.app.Application} onCreate() method to initialize retrofit
     * services provided by the subclass.
     */
    @CallSuper
    @Override
    public void onCreate() {
        super.onCreate();
        sRetrofitServices = getRetrofitServices();
        sRetrofitServices.init();
    }

    /**
     * Must provide an instance of {@link RetrofitServices} that will be initialized and
     * used to perform API calls by the application. It's not necessary to call init() on the
     * {@link RetrofitServices} instance, it will be called by this class.
     *
     * @return an instance of {@link RetrofitServices} available to perform API requests
     */
    public abstract RetrofitServices getRetrofitServices();

}