package ar.com.wolox.wolmo.networking.retrofit.handler

import retrofit2.Response

object NetworkRequestHandler {

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
    data class Success<T>(val body: T) : NetworkResponse<T>()
    data class Error<T>(val body: T) : NetworkResponse<T>()
    data class Failure<T>(val t: Throwable) : NetworkResponse<T>()
}