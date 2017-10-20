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

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

public class OkHttpClientModuleTest {

    @Test
    public void provideOkHttpClientShouldAddInterceptors() {
        OkHttpClient.Builder okHttpBuilderSpy = spy(new OkHttpClient.Builder());
        Interceptor interceptorMock = mock(Interceptor.class);
        Interceptor interceptorMock2 = mock(Interceptor.class);

        OkHttpClient client = OkHttpClientModule.provideOkHttpClient(okHttpBuilderSpy,
                interceptorMock, interceptorMock2);

        assertThat(client).isNotNull();
        verify(okHttpBuilderSpy, times(1)).addInterceptor(eq(interceptorMock));
        verify(okHttpBuilderSpy, times(1)).addInterceptor(eq(interceptorMock2));
        verify(okHttpBuilderSpy, times(1)).build();
    }

    @Test
    public void provideOkHttpClientShouldWorkWithoutInterceptors() {
        OkHttpClient.Builder okHttpBuilderSpy = spy(new OkHttpClient.Builder());

        OkHttpClient client = OkHttpClientModule.provideOkHttpClient(okHttpBuilderSpy);

        assertThat(client).isNotNull();
        verify(okHttpBuilderSpy, times(1)).build();
    }

    @Test
    public void provideOkHttpClientShouldWorkWithNullInterceptors() {
        OkHttpClient.Builder okHttpBuilderSpy = spy(new OkHttpClient.Builder());

        OkHttpClient client = OkHttpClientModule.provideOkHttpClient(okHttpBuilderSpy, (Interceptor[]) null);

        assertThat(client).isNotNull();
        verify(okHttpBuilderSpy, times(1)).build();
    }
}
