package ar.com.wolox.wolmo.networking.test_utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import okhttp3.ResponseBody;
import retrofit2.Response;

public class ResponseMockFactory {

    private ResponseMockFactory() {}

    @SuppressWarnings("unchecked")
    public static <T> Response<T> createSuccessfulResponseMock(T responseBody) {
        Response<T> response = mock(Response.class);
        when(response.body()).thenReturn(responseBody);
        when(response.isSuccessful()).thenReturn(true);
        when(response.code()).thenReturn(200);
        return response;
    }

    @SuppressWarnings("unchecked")
    public static <T> Response<T> createFailedResponseMock(ResponseBody errorBody, int code) {
        Response<T> response = mock(Response.class);
        when(response.errorBody()).thenReturn(errorBody);
        when(response.isSuccessful()).thenReturn(false);
        when(response.code()).thenReturn(code);
        return response;
    }

    @SuppressWarnings("unchecked")
    public static <T> Response<T> createFailedResponseMock(int code) {
        return createFailedResponseMock(mock(ResponseBody.class), code);
    }
}
