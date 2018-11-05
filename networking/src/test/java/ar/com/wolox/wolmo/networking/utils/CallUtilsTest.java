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

package ar.com.wolox.wolmo.networking.utils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.com.wolox.wolmo.core.java8.Predicate;
import ar.com.wolox.wolmo.networking.test_utils.RetrofitCallMockBuilder;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("unchecked")
public class CallUtilsTest {

    private static final int TRIES = 5;

    private Semaphore mSemaphore;
    private Callback<String> mCallbackSpy;

    @Before
    public void beforeTest() {
        mSemaphore = new Semaphore(0);

        mCallbackSpy = spy(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                mSemaphore.release();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                mSemaphore.release();
            }
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPollWithDelayUntilTimeout() throws Exception {
        Predicate<Response<String>> pollingConditionMock = mock(Predicate.class);
        when(pollingConditionMock.test(any(Response.class))).thenReturn(true);

        Call<String> callMock = new RetrofitCallMockBuilder().build((call, callback) -> {
            callback.onResponse(call, mock(Response.class));
            mSemaphore.release();
        });

        CallUtils.pollWithDelay(TRIES, callMock, pollingConditionMock, mCallbackSpy, 100,
                TimeUnit.MILLISECONDS);
        mSemaphore.acquire(TRIES + 1); // Tries + 1 Extra permit on final callback

        verify(pollingConditionMock, times(TRIES)).test(any(Response.class));
        verify(callMock, times(TRIES)).enqueue(any(Callback.class));
        verify(mCallbackSpy, times(1)).onFailure(eq(callMock), any(Exception.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPollWithDelaySuccess() throws Exception {
        Predicate<Response<String>> pollingConditionMock = mock(Predicate.class);
        when(pollingConditionMock.test(any(Response.class))).thenReturn(true).thenReturn(true)
                .thenReturn(false); // 3 Tries

        Call<String> callMock = new RetrofitCallMockBuilder().build((call, callback) -> {
            callback.onResponse(call, mock(Response.class));
            mSemaphore.release();
        });

        CallUtils.pollWithDelay(TRIES, callMock, pollingConditionMock, mCallbackSpy, 100,
                TimeUnit.MILLISECONDS);
        mSemaphore.acquire(4); // 3 Tries + 1 Extra permit on final callback

        verify(pollingConditionMock, times(3)).test(any(Response.class));
        verify(callMock, times(3)).enqueue(any(Callback.class));
        verify(mCallbackSpy, times(1)).onResponse(any(Call.class), any(Response.class));
    }
}