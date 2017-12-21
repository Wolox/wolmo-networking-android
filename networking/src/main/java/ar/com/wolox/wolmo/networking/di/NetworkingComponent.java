/*
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
package ar.com.wolox.wolmo.networking.di;

import android.support.annotation.Nullable;

import com.google.gson.FieldNamingPolicy;

import ar.com.wolox.wolmo.networking.di.modules.GsonModule;
import ar.com.wolox.wolmo.networking.di.modules.NetworkingModule;
import ar.com.wolox.wolmo.networking.di.modules.OkHttpClientModule;
import ar.com.wolox.wolmo.networking.di.scopes.NetworkingScope;
import ar.com.wolox.wolmo.networking.retrofit.RetrofitServices;
import ar.com.wolox.wolmo.networking.utils.GsonTypeAdapter;

import dagger.BindsInstance;
import dagger.Component;
import okhttp3.Interceptor;

@NetworkingScope
@Component(modules = { GsonModule.class, OkHttpClientModule.class, NetworkingModule.class })
public interface NetworkingComponent {

    RetrofitServices retrofitServices();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder baseUrl(String baseUrl);

        @BindsInstance
        Builder okHttpInterceptors(@Nullable Interceptor... interceptors);

        @BindsInstance
        Builder gsonNamingPolicy(FieldNamingPolicy namingPolicy);

        @BindsInstance
        Builder gsonTypeAdapters(@Nullable GsonTypeAdapter... typeAdapters);

        NetworkingComponent build();
    }
}
