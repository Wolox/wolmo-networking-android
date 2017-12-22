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
package ar.com.wolox.wolmo.networking.retrofit.interceptor;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.Interceptor.Chain;
import okhttp3.Request;
import okhttp3.Request.Builder;

public class ApiRestInterceptorTest {

    private static final String JSON_APP = "application/json";

    private ApiRestInterceptor mApiRestInterceptor;

    @Before
    public void beforeTest() {
        mApiRestInterceptor = spy(new ApiRestInterceptor() {
            @Override
            public void addHeaders(@NonNull Builder requestBuilder) {
            }
        });
    }

    @Test
    public void interceptShouldAddJsonHeaders() throws IOException {
        Chain chainMock = mock(Chain.class);
        Request request = new Request.Builder().url("http://test.com").build();
        when(chainMock.request()).thenReturn(request);

        mApiRestInterceptor.intercept(chainMock);

        // Capture the request
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(chainMock, times(1)).proceed(requestCaptor.capture());
        verify(mApiRestInterceptor, times(1)).addHeaders(any(Request.Builder.class));

        // Verify that the request has the required headers
        Request responseRequest = requestCaptor.getValue();
        assertThat(responseRequest.headers().toMultimap()).hasSize(2)
                .contains(entry(ApiRestInterceptor.CONTENT_TYPE_HEADER, Arrays.asList(JSON_APP)))
                .contains(entry(ApiRestInterceptor.ACCEPT_HEADER, Arrays.asList(JSON_APP)));
    }

}
