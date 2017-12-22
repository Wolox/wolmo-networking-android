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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.Retrofit.Builder;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkingModuleTest {

    @Test
    public void provideRetrofitShouldConfigureBuilder() {
        Builder builderSpy = spy(new Builder());
        OkHttpClient clientMock = mock(OkHttpClient.class);
        GsonConverterFactory converterFactoryMock = mock(GsonConverterFactory.class);

        Retrofit retrofit = NetworkingModule.provideRetrofit(builderSpy, "http://web.com",
                converterFactoryMock, clientMock);

        assertThat(retrofit).isNotNull();
        verify(builderSpy, times(1)).baseUrl(eq("http://web.com"));
        verify(builderSpy, times(1)).addConverterFactory(eq(converterFactoryMock));
        verify(builderSpy, times(1)).client(eq(clientMock));
        verify(builderSpy, times(1)).build();
    }

    @Test
    public void provideRetrofitBuilderShouldReturnNewInstance() {
        Retrofit.Builder builder1 = NetworkingModule.provideRetrofitBuilder();
        Retrofit.Builder builder2 = NetworkingModule.provideRetrofitBuilder();
        assertThat(builder1).isNotSameAs(builder2);
        assertThat(builder1).isNotEqualTo(builder2);
    }
}
