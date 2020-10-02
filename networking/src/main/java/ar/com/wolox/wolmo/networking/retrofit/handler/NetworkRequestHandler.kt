package ar.com.wolox.wolmo.networking.retrofit.handler

import retrofit2.Response

/**
 * An adapter that converts Retrofit's responses to other, more specific, ones
 * depending on network status codes or failures when using Kotlin's Coroutines.
 *
 * @param <T> the type of object expected to be returned from the API call
 */
object NetworkRequestHandler {

    /**
     * Static method that allows to execute requests and returns a NetworkResponse object {@link NetworkResponse}
     * depending on HTTP response
     *
     * @param block is a suspend function of Response<T> type
     */
    suspend fun <T : Response<*>> safeApiCall(block: suspend () -> T): NetworkResponse<T> =
            try {
                val response = block.invoke()
                if (response.isSuccessful) {
                    NetworkResponse.Success(response)
                } else {
                    NetworkResponse.Error(response)
                }
            } catch (t: Throwable) {
                NetworkResponse.Failure(t)
            }
}

sealed class NetworkResponse<T> {

    /**
     * Successful HTTP response from the server.
     * The server received the request, answered it and the response is not of an error type.
     *
     * @param response the API JSON response converted to a Java/Kotlin object.
     * The API response code is included in the response object.
     */
    data class Success<T>(val response: T) : NetworkResponse<T>()

    /**
     * Successful HTTP response from the server, but has an error body.
     * The server received the request, answered it and reported an error.
     *
     * @param response the API JSON response converted to a Java/Kotlin object.
     * The API response code is included in the response object.
     */
    data class Error<T>(val response: T) : NetworkResponse<T>()

    /**
     * The HTTP request to the server failed on the local device, no data was transmitted.
     * Invoked when a network or unexpected exception occurred during the HTTP request, meaning
     * that the request couldn't be executed.
     *
     * @param t A Throwable with the cause of the call failure
     */
    data class Failure<T>(val t: Throwable) : NetworkResponse<T>()
}