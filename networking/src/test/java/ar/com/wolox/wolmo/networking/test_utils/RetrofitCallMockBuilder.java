package ar.com.wolox.wolmo.networking.test_utils;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.stubbing.Answer;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Builder to customize a Retrofit's {@link Call} mock.
 *
 * @param <T> Type of the call.
 */
@ParametersAreNonnullByDefault
public class RetrofitCallMockBuilder<T> {

    public static final String HTTP_METHOD_GET = "GET";
    public static final String HTTP_METHOD_POST = "POST";
    public static final String DEFAULT_URL = "http://test.com/test";

    private String mHttpMethod;
    private String mUrl;
    private CallbackMockConsumer<T> mCallbackAnswer;
    private Exception mException;
    private long mRunBefore;
    private boolean mExecuted;
    private boolean mCanceled;

    /**
     * Sets the HTTP method for the mock.
     * If not HTTP method is set, the value {@link #HTTP_METHOD_GET} will be used.
     *
     * @param httpMethod String http method of the mock.
     *
     * @return Builder
     */
    public RetrofitCallMockBuilder setHttpMethod(String httpMethod) {
        mHttpMethod = httpMethod;
        return this;
    }

    /**
     * Sets the URL for the mock.
     * If not URL is set, the value {@link #DEFAULT_URL} will be used.
     *
     * @param url String url of the mock.
     *
     * @return Builder
     */
    public RetrofitCallMockBuilder setUrl(String url) {
        mUrl = url;
        return this;
    }

    /**
     * Set the executed status of the mock with the value provided.
     * When calling {@link Call#isExecuted()} the value of <b>executed</b> will be returned.
     *
     * @param executed If the mock was executed or not
     *
     * @return Builder
     */
    public RetrofitCallMockBuilder isExecuted(boolean executed) {
        mExecuted = executed;
        return this;
    }

    /**
     * Set the cancel status of the mock with the value provided.
     * When calling {@link Call#isCanceled()} ()} the value of <b>canceled</b> will be returned.
     *
     * @param canceled If the mock was executed or not
     *
     * @return Builder
     */
    public RetrofitCallMockBuilder isCanceled(boolean canceled) {
        mCanceled = canceled;
        return this;
    }

    /**
     * Configures the {@link Call} to run before an amount of time.
     * Setting this to a number greater than 0 will create a new {@link Thread} to run the call,
     * otherwise it'll run in the same thread it was created.
     *
     * @param time Time to wait before "executing" the call.
     * @param timeUnit TimeUnit
     *
     * @return Builder
     */
    public RetrofitCallMockBuilder runBefore(long time, TimeUnit timeUnit) {
        mRunBefore = timeUnit.toMillis(time);
        return this;
    }

    /**
     * Builds a Success Call.
     * This mocked {@link Call} will make a call to {@link Callback#onResponse(Call, Response)}
     * with
     * a success {@link Response} and with the given response.
     *
     * @param response Response to send at the callback.
     *
     * @return new {@link Call} mock.
     */
    public Call<T> buildSuccess(T response) {
        return build((call, callback) -> callback.onResponse(call, Response.success(response)));
    }

    /**
     * Builds an error call.
     * This mocked {@link Call} will make a call to {@link Callback#onResponse(Call, Response)}
     * with an error {@link Response}. The response will have the httpCode given in the parameter
     * and a Mock as the response body.
     *
     * @param httpCode Response code for the response
     *
     * @return a new {@link Call} mock
     */
    public Call<T> buildError(int httpCode) {
        return build((call, callback) -> callback
                .onResponse(call, Response.error(httpCode, mock(ResponseBody.class))));
    }

    /**
     * Builds a failed call.
     * This mocker {@link Call} will call {@link Callback#onFailure(Call, Throwable)} with the
     * given
     * exception.
     *
     * @param exception Exception to send to the callback.
     *
     * @return a new {@link Call} mock
     */
    public Call<T> buildFailure(@Nullable Exception exception) {
        mException = exception;
        return build();
    }

    /**
     * Builds a call with a custom behaviour.
     * You can pass a {@link CallbackMockConsumer} who receives a {@link Call} and a {@link
     * Callback} to
     * take custom logic.
     *
     * @param callbackMockConsumer Consumer for the call.
     *
     * @return a new {@link Call} mock.
     */
    public Call<T> build(CallbackMockConsumer<T> callbackMockConsumer) {
        mCallbackAnswer = callbackMockConsumer;
        return build();
    }

    /**
     * Build a mocked {@link Call} and all the required (mocked) dependencies.
     *
     * @return a new {@link Call} mock.
     */
    @SuppressWarnings("unchecked")
    private Call<T> build() {
        Call<T> callMock = mock(Call.class);
        Request requestMock = buildOkHttpRequest();
        when(callMock.request()).thenReturn(requestMock);
        when(callMock.isExecuted()).thenReturn(mExecuted);
        when(callMock.isCanceled()).thenReturn(mCanceled);

        // Custom answer
        doAnswer(buildCallAnswer(callMock)).when(callMock).enqueue(any(Callback.class));
        when(callMock.clone()).thenReturn(callMock);
        return callMock;
    }

    private Request buildOkHttpRequest() {
        Request okHttpRequestMock = mock(Request.class);

        HttpUrl urlMock = mock(HttpUrl.class);
        when(urlMock.toString()).thenReturn(mUrl == null ? DEFAULT_URL : mUrl);

        when(okHttpRequestMock.method()).thenReturn(mHttpMethod == null ? HTTP_METHOD_GET : mHttpMethod);
        when(okHttpRequestMock.url()).thenReturn(urlMock);
        return okHttpRequestMock;
    }

    private Answer<T> buildCallAnswer(final Call<T> callMock) {
        CallbackMockConsumer<T> consumer = (call, callback) -> {
            if (mException != null) {
                callback.onFailure(call, mException);
            } else {
                mCallbackAnswer.consume(call, callback);
            }
        };

        if (mRunBefore > 0) {
            return invocation -> {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Callback<T> callback = invocation.getArgument(0);
                        try {
                            consumer.consume(callMock, callback);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, mRunBefore);
                return null;
            };
        } else {
            return invocation -> {
                Callback<T> callback = invocation.getArgument(0);
                consumer.consume(callMock, callback);
                return null;
            };
        }
    }

    /**
     * Helper consumer interface to handle callbacks
     *
     * @param <T> Type of the call requested
     */
    public interface CallbackMockConsumer<T> {
        void consume(Call<T> call, Callback<T> callback) throws Exception;
    }
}
