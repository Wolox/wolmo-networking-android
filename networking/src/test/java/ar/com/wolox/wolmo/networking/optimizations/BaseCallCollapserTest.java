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
package ar.com.wolox.wolmo.networking.optimizations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import androidx.annotation.NonNull;

import ar.com.wolox.wolmo.networking.test_utils.RetrofitCallMockBuilder;
import ar.com.wolox.wolmo.networking.test_utils.service.RetrofitTestService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BaseCallCollapserTest {

    private BaseCallCollapser mBaseCallCollapser;
    private MockWebServer mMockWebServer;
    private Retrofit mRetrofit;

    private Semaphore mSemaphore;
    private Callback<String> mCallbackBase;

    @Before
    public void beforeTest() throws IOException {
        mBaseCallCollapser = new BaseCallCollapser();
        mMockWebServer = new MockWebServer();
        mMockWebServer.start();

        mRetrofit = new Retrofit.Builder().baseUrl(mMockWebServer.url(""))
                .addConverterFactory(GsonConverterFactory.create()).client(new OkHttpClient())
                .build();

        mSemaphore = new Semaphore(0);
        mCallbackBase = new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                mSemaphore.release();
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                mSemaphore.release();
            }
        };
    }

    @Test
    @SuppressWarnings("unchecked")
    public void enqueueGetCall() throws Exception {
        mMockWebServer.enqueue(new MockResponse().setBody("\"Hello First\""));

        Callback<String> callbackMock = mock(Callback.class);
        Call<String> callMock =
                Mockito.spy(mRetrofit.create(RetrofitTestService.class).retrofitGetMethodString());

        // Enqueue Two GET requests
        mBaseCallCollapser.enqueue(callMock, callbackMock);
        mBaseCallCollapser.enqueue(callMock, callbackMock);

        // Wait for a server response and Verify server call
        RecordedRequest request = mMockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/api/get/");
        assertThat(mMockWebServer.getRequestCount()).isEqualTo(1);
        verify(callMock, times(1)).enqueue(any(Callback.class));

        // Verify that both callbacks get called
        ArgumentCaptor<Response<String>> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(callbackMock, times(2)).onResponse(eq(callMock), responseCaptor.capture());
        assertThat(responseCaptor.getValue().body()).isEqualTo("Hello First");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void enqueueGetFailingCall() throws Exception {
        mMockWebServer.enqueue(new MockResponse().setResponseCode(404));

        Callback<String> callbackSpy = spy(mCallbackBase);
        Call<String> callMock = new RetrofitCallMockBuilder()
                .runBefore(1, TimeUnit.SECONDS)
                .buildFailure(new Exception());

        // Enqueue Two GET requests
        mBaseCallCollapser.enqueue(callMock, callbackSpy);
        mBaseCallCollapser.enqueue(callMock, callbackSpy);

        // Wait for a server response and Verify server call
        mSemaphore.acquire(2);
        assertThat(mMockWebServer.getRequestCount()).isEqualTo(0);
        verify(callMock, times(1)).enqueue(any(Callback.class));

        // Verify that both callbacks get called
        verify(callbackSpy, times(2)).onFailure(eq(callMock), any(Exception.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void enqueueGetAndPostCall() throws Exception {
        mMockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getMethod().equals("GET")) {
                    return new MockResponse().setBody("\"Hello GET\"");
                } else if (request.getMethod().equals("POST")) {
                    return new MockResponse().setBody("\"Hello POST\"");
                }
                return new MockResponse();
            }
        });

        // Prepare mocks
        Callback<String> getCallbackSpy = spy(mCallbackBase);
        Callback<String> postCallbackSpy = spy(mCallbackBase);
        Call<String> getCallMock =
                Mockito.spy(mRetrofit.create(RetrofitTestService.class).retrofitGetMethodString());
        Call<String> postCallMock =
                Mockito.spy(mRetrofit.create(RetrofitTestService.class).retrofitPostMethodString());

        // Enqueue Two requests
        mBaseCallCollapser.enqueue(getCallMock, getCallbackSpy);
        mBaseCallCollapser.enqueue(postCallMock, postCallbackSpy);

        // Wait for a server response
        mSemaphore.acquire(2);

        // Verify server call
        assertThat(mMockWebServer.getRequestCount()).isEqualTo(2);
        verify(postCallMock, times(1)).enqueue(any(Callback.class));
        verify(getCallMock, times(1)).enqueue(any(Callback.class));

        // Verify that both callbacks get called
        ArgumentCaptor<Response<String>> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(getCallbackSpy, times(1)).onResponse(eq(getCallMock), responseCaptor.capture());
        assertThat(responseCaptor.getValue().body()).isEqualTo("Hello GET");

        ArgumentCaptor<Response<String>> responseCaptor2 = ArgumentCaptor.forClass(Response.class);
        verify(postCallbackSpy, times(1)).onResponse(eq(postCallMock), responseCaptor2.capture());
        assertThat(responseCaptor2.getValue().body()).isEqualTo("Hello POST");
    }
}
