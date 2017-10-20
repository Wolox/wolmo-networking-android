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

package ar.com.wolox.wolmo.networking.retrofit;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import retrofit2.Retrofit;


@SuppressWarnings("unchecked")
public class RetrofitServicesTest {

    private Retrofit mRetrofit;
    private RetrofitServices mRetrofitServices;

    @Before
    public void beforeTest() {
        mRetrofit = mock(Retrofit.class);
        mRetrofitServices = new RetrofitServices(mRetrofit);
    }

    @Test
    public void testCachedRetrofitService() {
        Object service = new Object();
        when(mRetrofit.create(any(Class.class))).thenReturn(service);

        assertThat(mRetrofitServices.getService(Object.class)).isSameAs(service);
        verify(mRetrofit, times(1)).create(eq(Object.class));

        // Successive calls shouldn't call create() again
        assertThat(mRetrofitServices.getService(Object.class)).isSameAs(service);
        verify(mRetrofit, times(1)).create(eq(Object.class));
    }

}