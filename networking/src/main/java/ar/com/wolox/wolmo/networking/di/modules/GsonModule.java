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
package ar.com.wolox.wolmo.networking.di.modules;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ar.com.wolox.wolmo.networking.retrofit.serializer.LocalDateSerializer;
import ar.com.wolox.wolmo.networking.utils.GsonTypeAdapter;

import org.joda.time.LocalDate;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class GsonModule {

    @Provides
    static GsonConverterFactory provideGsonConverterFactory(Gson gson) {
        return GsonConverterFactory.create(gson);
    }

    @Provides
    static Gson provideGson(GsonBuilder gsonBuilder) {
        return gsonBuilder.create();
    }

    @Provides
    @Named("newInstance")
    static GsonBuilder provideNewGsonBuilder() {
        return new GsonBuilder();
    }

    @Provides
    static GsonBuilder provideGsonBuilder(@Named("newInstance") GsonBuilder gsonBuilder,
                                          @NonNull FieldNamingPolicy namingPolicy,
                                          @Nullable GsonTypeAdapter... typeAdapters) {

        gsonBuilder.setFieldNamingPolicy(namingPolicy);

        if (typeAdapters != null && typeAdapters.length > 0) {
            for (GsonTypeAdapter typeAdapter : typeAdapters) {
                gsonBuilder
                        .registerTypeAdapter(typeAdapter.getType(), typeAdapter.getTypeAdapter());
            }
        } else {
            gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateSerializer());
        }

        return gsonBuilder;
    }
}
