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
package ar.com.wolox.wolmo.networking.retrofit.callback;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

@SuppressWarnings("unchecked")
public class NetworkCallbackTest {

    private NetworkCallback mNetworkCallbackSpy;
    private Response mResponseMock;
    private Object mResponseBody;
    private Call mCallMock;

    @Before
    public void beforeTest() {
        mNetworkCallbackSpy = spy(new NetworkCallback() {
            @Override
            public void onResponseSuccessful(Object response) {}

            @Override
            public void onResponseFailed(ResponseBody responseBody, int code) {}

            @Override
            public void onCallFailure(Throwable t) {}
        });

        mResponseMock = mock(Response.class);
        mCallMock = mock(Call.class);
        mResponseBody = new Object();

        when(mResponseMock.body()).thenReturn(mResponseBody);
        when(mResponseMock.code()).thenReturn(444);
        when(mResponseMock.errorBody()).thenReturn(mock(ResponseBody.class));
    }

    @Test
    public void onResponseShouldNotifySuccessfulResponse() {
        when(mResponseMock.isSuccessful()).thenReturn(true);

        mNetworkCallbackSpy.onResponse(mCallMock, mResponseMock);
        verify(mNetworkCallbackSpy, times(1)).onResponseSuccessful(eq(mResponseBody));
    }

    @Test
    public void onResponseShouldNotifyAuthErrors() {
        when(mResponseMock.isSuccessful()).thenReturn(false);
        when(mNetworkCallbackSpy.isAuthError(any(Response.class))).thenReturn(true);

        mNetworkCallbackSpy.onResponse(mCallMock, mResponseMock);
        verify(mNetworkCallbackSpy, times(1)).handleAuthError(eq(mResponseMock));
    }

    @Test
    public void onResponseShouldNotifyResponseFailed() {
        when(mResponseMock.isSuccessful()).thenReturn(false);
        when(mNetworkCallbackSpy.isAuthError(any(Response.class))).thenReturn(false);

        mNetworkCallbackSpy.onResponse(mCallMock, mResponseMock);
        verify(mNetworkCallbackSpy, times(1)).onResponseFailed(any(ResponseBody.class), eq(444));
    }
}