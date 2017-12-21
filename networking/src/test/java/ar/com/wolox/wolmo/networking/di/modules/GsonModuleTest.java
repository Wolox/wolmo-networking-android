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

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory.Adapter;

import ar.com.wolox.wolmo.networking.retrofit.serializer.LocalDateSerializer;
import ar.com.wolox.wolmo.networking.utils.GsonTypeAdapter;

import org.joda.time.LocalDate;
import org.junit.Test;

import java.lang.reflect.Type;

import retrofit2.converter.gson.GsonConverterFactory;

public class GsonModuleTest {

    @Test
    public void provideGsonShouldUseGivenBuilder() {
        Gson gson = mock(Gson.class);
        GsonBuilder builder = mock(GsonBuilder.class);
        when(builder.create()).thenReturn(gson);

        assertThat(GsonModule.provideGson(builder)).isSameAs(gson);
        verify(builder, times(1)).create();
    }


    @Test
    public void provideGsonBuilderShouldRegisterAdapters() {
        Type type = mock(Type.class);
        TypeAdapter adapter = mock(Adapter.class);
        GsonBuilder builderMock = mock(GsonBuilder.class);

        GsonTypeAdapter typeAdapter = new GsonTypeAdapter(type, adapter);
        GsonTypeAdapter typeAdapter2 = new GsonTypeAdapter(type, adapter);

        GsonBuilder builder = GsonModule.provideGsonBuilder(builderMock,
                FieldNamingPolicy.UPPER_CAMEL_CASE, typeAdapter, typeAdapter2);

        assertThat(builder).isSameAs(builderMock);
        verify(builderMock, times(1)).setFieldNamingPolicy(any(FieldNamingPolicy.class));
        verify(builderMock, times(1)).setFieldNamingPolicy(eq(FieldNamingPolicy.UPPER_CAMEL_CASE));
        verify(builderMock, times(2)).registerTypeAdapter(eq(type), eq(adapter));
    }

    @Test
    public void provideGsonBuilderWithoutAdaptersShouldAddDefaultAdapter() {
        GsonBuilder builderMock = mock(GsonBuilder.class);

        GsonBuilder builder = GsonModule.provideGsonBuilder(builderMock,
                FieldNamingPolicy.UPPER_CAMEL_CASE);

        assertThat(builder).isSameAs(builderMock);
        verify(builderMock, times(1)).setFieldNamingPolicy(any(FieldNamingPolicy.class));
        verify(builderMock, times(1)).setFieldNamingPolicy(eq(FieldNamingPolicy.UPPER_CAMEL_CASE));
        verify(builderMock, times(1)).registerTypeAdapter(eq(LocalDate.class),
                any(LocalDateSerializer.class));
    }

    @Test
    public void provideGsonBuilderWithNullShouldAddDefaultAdapter() {
        GsonBuilder builderMock = mock(GsonBuilder.class);

        GsonBuilder builder = GsonModule.provideGsonBuilder(builderMock,
                FieldNamingPolicy.UPPER_CAMEL_CASE, (GsonTypeAdapter[]) null);

        assertThat(builder).isSameAs(builderMock);
        verify(builderMock, times(1)).setFieldNamingPolicy(any(FieldNamingPolicy.class));
        verify(builderMock, times(1)).setFieldNamingPolicy(eq(FieldNamingPolicy.UPPER_CAMEL_CASE));
        verify(builderMock, times(1)).registerTypeAdapter(eq(LocalDate.class),
                any(LocalDateSerializer.class));
    }

    @Test
    public void provideNewGsonBuilderShouldReturnNewInstance() {
        GsonBuilder gsonBuilder1 = GsonModule.provideNewGsonBuilder();
        GsonBuilder gsonBuilder2 = GsonModule.provideNewGsonBuilder();
        assertThat(gsonBuilder1).isNotSameAs(gsonBuilder2);
        assertThat(gsonBuilder1).isNotEqualTo(gsonBuilder2);
    }

    @Test
    public void provideGsonConverterFactoryShouldUseProvidedGson() {
        Gson gsonMock = mock(Gson.class);
        GsonConverterFactory converterFactory = GsonModule.provideGsonConverterFactory(gsonMock);
        assertThat(converterFactory).extracting("gson").containsOnly(gsonMock);
    }
}
