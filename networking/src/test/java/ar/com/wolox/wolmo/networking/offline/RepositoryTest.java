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
package ar.com.wolox.wolmo.networking.offline;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.com.wolox.wolmo.core.java8.Consumer;
import ar.com.wolox.wolmo.networking.exception.CacheMissException;
import ar.com.wolox.wolmo.networking.exception.NetworkResourceException;
import ar.com.wolox.wolmo.networking.optimizations.ICallCollapser;
import ar.com.wolox.wolmo.networking.test_utils.RetrofitCallMockBuilder;

import org.junit.Before;
import org.junit.Test;

import retrofit2.Call;
import retrofit2.Callback;

public class RepositoryTest {

    private Repository<String, String> mRepository;
    private ICallCollapser mCallCollapserMock;
    private Repository.QueryStrategy<String, String> mQueryStrategyMock;
    private String mCache;

    @Before
    @SuppressWarnings("unchecked")
    public void beforeTest() {
        mCache = "Cache";
        mCallCollapserMock = mock(ICallCollapser.class);
        mQueryStrategyMock = mock(Repository.QueryStrategy.class);
        mRepository = new Repository<>(mCache, mCallCollapserMock);

        // Delegate the Collapser enqueue to the Call
        doAnswer(invocation -> {
            Call<String> call = invocation.getArgument(0);
            call.enqueue(invocation.getArgument(1));
            return null;
        }).when(mCallCollapserMock).enqueue(any(Call.class), any(Callback.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void queryDefaultAccessCacheSuccess() {
        Call<String> callMock = new RetrofitCallMockBuilder().buildSuccess("Success");

        Consumer<Throwable> onErrorMock = mock(Consumer.class);
        Consumer<String> onSuccessMock = mock(Consumer.class);

        // Cache status
        when(mQueryStrategyMock.readLocalSource(any(String.class))).thenReturn("CachedValue");

        // Do things
        Repository.Query<String> query = mRepository.query(callMock, mQueryStrategyMock);
        query.onError(onErrorMock).onSuccess(onSuccessMock);
        query.run();

        // Verify cache read
        verify(mQueryStrategyMock, times(1)).readLocalSource(eq(mCache));
        verify(onSuccessMock, times(1)).accept(eq("CachedValue"));

        // Verify no network request
        verify(mCallCollapserMock, times(0)).enqueue(eq(callMock), any(Callback.class));
        verify(mQueryStrategyMock, times(0)).consumeRemoteSource(eq("Response"), eq(mCache));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void queryDefaultAccessCacheAndNetworkSuccessful() {
        Call<String> callMock = new RetrofitCallMockBuilder().buildSuccess("Response");

        Consumer<Throwable> onErrorMock = mock(Consumer.class);
        Consumer<String> onSuccessMock = mock(Consumer.class);

        // Cache status
        when(mQueryStrategyMock.readLocalSource(any(String.class))).thenReturn(null);

        // Do things
        Repository.Query<String> query = mRepository.query(callMock, mQueryStrategyMock);
        query.onError(onErrorMock).onSuccess(onSuccessMock);
        query.run();

        // Verify cache read
        verify(mQueryStrategyMock, times(1)).readLocalSource(eq(mCache));

        // Verify network request
        verify(mCallCollapserMock, times(1)).enqueue(eq(callMock), any(Callback.class));
        verify(mQueryStrategyMock, times(1)).consumeRemoteSource(eq("Response"), eq(mCache));
        verify(onSuccessMock, times(1)).accept(eq("Response"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void queryDefaultAccessCacheAndNetworkResponseError() {
        Call<String> callMock = new RetrofitCallMockBuilder().buildError(404);

        Consumer<Throwable> onErrorMock = mock(Consumer.class);
        Consumer<String> onSuccessMock = mock(Consumer.class);

        // Cache status
        when(mQueryStrategyMock.readLocalSource(any(String.class))).thenReturn(null);

        // Do things
        Repository.Query<String> query = mRepository.query(callMock, mQueryStrategyMock);
        query.onError(onErrorMock).onSuccess(onSuccessMock);
        query.run();

        // Verify cache read
        verify(mQueryStrategyMock, times(1)).readLocalSource(eq(mCache));

        // Verify network request
        verify(mCallCollapserMock, times(1)).enqueue(eq(callMock), any(Callback.class));
        verify(mQueryStrategyMock, times(0)).consumeRemoteSource(eq("Response"), eq(mCache));
        verify(onErrorMock, times(1)).accept(any(NetworkResourceException.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void queryDefaultAccessCacheAndNetworkFail() {
        Exception exception = new Exception();
        Call<String> callMock = new RetrofitCallMockBuilder().buildFailure(exception);

        Consumer<Throwable> onErrorMock = mock(Consumer.class);
        Consumer<String> onSuccessMock = mock(Consumer.class);

        // Cache status
        when(mQueryStrategyMock.readLocalSource(any(String.class))).thenReturn(null);

        // Do things
        Repository.Query<String> query = mRepository.query(callMock, mQueryStrategyMock);
        query.onError(onErrorMock).onSuccess(onSuccessMock);
        query.run();

        // Verify cache read
        verify(mQueryStrategyMock, times(1)).readLocalSource(eq(mCache));

        // Verify network request
        verify(mCallCollapserMock, times(1)).enqueue(eq(callMock), any(Callback.class));
        verify(mQueryStrategyMock, times(0)).consumeRemoteSource(eq("Response"), eq(mCache));
        verify(onErrorMock, times(1)).accept(eq(exception));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void queryDefaultAccessCacheAndNetworkFailWithCallback() {
        Exception exception = new Exception();
        Call<String> callMock = new RetrofitCallMockBuilder().buildFailure(exception);
        IRepositoryCallback<String> repositoryCallbackMock = mock(IRepositoryCallback.class);

        // Cache status
        when(mQueryStrategyMock.readLocalSource(any(String.class))).thenReturn(null);

        // Do things
        mRepository.query(callMock, mQueryStrategyMock, repositoryCallbackMock);

        // Verify cache read
        verify(mQueryStrategyMock, times(1)).readLocalSource(eq(mCache));

        // Verify network request
        verify(mCallCollapserMock, times(1)).enqueue(eq(callMock), any(Callback.class));
        verify(mQueryStrategyMock, times(0)).consumeRemoteSource(eq("Response"), eq(mCache));
        verify(repositoryCallbackMock, times(1)).onError(eq(exception));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void queryAccessCacheOnly() {
        Call<String> callMock = mock(Call.class);
        IRepositoryCallback<String> repositoryCallbackMock = mock(IRepositoryCallback.class);

        // Cache status
        when(mQueryStrategyMock.readLocalSource(any(String.class))).thenReturn(null);

        // Do things
        mRepository.query(Repository.CACHE_ONLY, callMock, mQueryStrategyMock, repositoryCallbackMock);

        // Verify cache read
        verify(mQueryStrategyMock, times(1)).readLocalSource(eq(mCache));
        verify(repositoryCallbackMock, times(1)).onError(any(CacheMissException.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void queryAccessCacheNone() {
        Call<String> callMock = new RetrofitCallMockBuilder().buildSuccess("Success");

        Consumer<Throwable> onErrorMock = mock(Consumer.class);
        Consumer<String> onSuccessMock = mock(Consumer.class);

        // Cache status
        when(mQueryStrategyMock.readLocalSource(any(String.class))).thenReturn(null);

        // Do things
        Repository.Query<String> query = mRepository.query(Repository.CACHE_NONE, callMock, mQueryStrategyMock);
        query.onError(onErrorMock).onSuccess(onSuccessMock);
        query.run();

        // Verify cache read
        verify(mQueryStrategyMock, times(0)).readLocalSource(eq(mCache));

        // Verify network request
        verify(mCallCollapserMock, times(1)).enqueue(eq(callMock), any(Callback.class));
        verify(mQueryStrategyMock, times(1)).consumeRemoteSource(eq("Success"), eq(mCache));
        verify(onSuccessMock, times(1)).accept(eq("Success"));
    }

    @Test(expected = IllegalStateException.class)
    @SuppressWarnings("unchecked")
    public void queryAccessCacheNoneExecutedCall() {
        Call<String> callMock = new RetrofitCallMockBuilder().isExecuted(true).buildSuccess("Success");

        Consumer<Throwable> onErrorMock = mock(Consumer.class);
        Consumer<String> onSuccessMock = mock(Consumer.class);

        // Cache status
        when(mQueryStrategyMock.readLocalSource(any(String.class))).thenReturn(null);

        // Do things
        Repository.Query<String> query = mRepository.query(Repository.CACHE_NONE, callMock, mQueryStrategyMock);
        query.onError(onErrorMock).onSuccess(onSuccessMock);
        query.run();
    }

    @Test(expected = IllegalStateException.class)
    @SuppressWarnings("unchecked")
    public void queryAccessCacheNoneCancelledCall() {
        Call<String> callMock = new RetrofitCallMockBuilder().isCanceled(true).buildSuccess("Success");

        Consumer<Throwable> onErrorMock = mock(Consumer.class);
        Consumer<String> onSuccessMock = mock(Consumer.class);

        // Cache status
        when(mQueryStrategyMock.readLocalSource(any(String.class))).thenReturn(null);

        // Do things
        Repository.Query<String> query = mRepository.query(Repository.CACHE_NONE, callMock, mQueryStrategyMock);
        query.onError(onErrorMock).onSuccess(onSuccessMock);
        query.run();
    }
}
