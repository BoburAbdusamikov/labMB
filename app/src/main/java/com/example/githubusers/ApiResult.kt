package com.example.githubusers

import retrofit2.HttpException
import java.io.IOException

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    object NetworkError : ApiResult<Nothing>()
    data class HttpError(val code: Int, val message: String) : ApiResult<Nothing>()
    data class UnknownError(val message: String) : ApiResult<Nothing>()
}

suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(apiCall())
    } catch (e: IOException) {
        ApiResult.NetworkError
    } catch (e: HttpException) {
        ApiResult.HttpError(e.code(), e.message())
    } catch (e: Exception) {
        ApiResult.UnknownError(e.localizedMessage ?: "Unknown error")
    }
}

sealed class UiState {
    object Loading : UiState()
    data class Success(val users: List<User>) : UiState()
    data class Error(val message: String, val isNetworkError: Boolean = false) : UiState()
}
