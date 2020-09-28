package ar.com.wolox.wolmo.networking.retrofit.handler

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.mockito.stubbing.Answer
import retrofit2.Response

@ExperimentalCoroutinesApi
class NetworkRequestHandlerTest {

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `given a succesfull response with code between 200 and 300, NetworkResponse Success state is called`() = runBlocking {
        // GIVEN
        val apiResponse = mock(Response::class.java) as Response<Any>


        // WHEN
        `when`(apiResponse.isSuccessful).thenReturn(true)

        // THEN
        val networkResponse = NetworkRequestHandler.safeApiCall { apiResponse }
        assert(networkResponse is NetworkResponse.Success)
    }

    @Test
    fun `given a succesfull response with code above 300, NetworkResponse Error state is called`() = runBlocking {
        // GIVEN
        val apiResponse = mock(Response::class.java) as Response<Any>

        // WHEN
        `when`(apiResponse.isSuccessful).thenReturn(false)

        // THEN
        val networkResponse = NetworkRequestHandler.safeApiCall { apiResponse }
        assert(networkResponse is NetworkResponse.Error)
    }

    @Test
    fun `given a non-succesfull response, NetworkResponse Failure state is called`() = runBlocking {
        // GIVEN
        val answer: Answer<Exception> = Answer { Exception() }
        val apiResponse = mock (Response::class.java, answer) as Response<Any>

        // THEN
        val networkResponse = NetworkRequestHandler.safeApiCall { apiResponse }
        assert(networkResponse is NetworkResponse.Failure)
    }
}