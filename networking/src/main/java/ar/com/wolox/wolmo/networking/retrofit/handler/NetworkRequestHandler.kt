package ar.com.wolox.wolmo.networking.retrofit.handler

import retrofit2.Response

/**
 * An adapter that converts Retrofit's responses to other, more specific, ones
 * depending on network status codes or failures when using Kotlin's Coroutines.
 * It will return a [NetworkResponse] object indicating operation result.
 */
object NetworkRequestHandler {

    /**
     * Static method that allows to execute requests from a [suspend] function of [Response] type
     * and returns a [NetworkResponse] object depending on HTTP response.
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
     * It will return a [T] object, the API JSON response converted to a Java/Kotlin object,
     * which includes the API response code.
     */
    data class Success<T>(val response: T) : NetworkResponse<T>()

    /**
     * Successful HTTP response from the server, but has an error body.
     * The server received the request, answered it and reported an error.
     * It will return a [T], the API JSON response converted to a Java/Kotlin object,
     * which includes the API response code.
     */
    data class Error<T>(val response: T) : NetworkResponse<T>()

    /**
     * The HTTP request to the server failed on the local device, no data was transmitted.
     * Invoked when a network or unexpected exception occurred during the HTTP request, meaning
     * that the request couldn't be executed. The cause of the failure will be given through a
     * [Throwable] object
     */
    data class Failure<T>(val t: Throwable) : NetworkResponse<T>()
}