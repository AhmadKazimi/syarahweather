package com.kazimi.syarahweather.data.net

import com.google.gson.Gson
import com.kazimi.syarahweather.domain.common.ApiException
import com.kazimi.syarahweather.domain.common.error.AppError
import retrofit2.Response

fun <T> Response<T>.validate(): T {
    if (isSuccessful) {
        return body()!!
    } else if (errorBody() == null) {
        val appError = AppError()
        // We might have Constants file for all error codes
        appError.code =  404
        throw ApiException(appError)
    } else {
        val appError =
            runCatching {
                // This in case we have specific error json format we can simply parse into AppError
                Gson().fromJson(errorBody()!!.string(), AppError::class.java)
            }.onFailure {
                it.printStackTrace()
                val error = AppError(message = "Something wrong")
                error.message = it.message ?: "Something wrong"
            }.getOrDefault(AppError())

            runCatching {
                // Ignore Cannot access 'request': it is package-private in 'Response' because the issue in the gradle
                this.raw().request.url.encodedPath
            }.getOrDefault("Can not get URL ")
        throw ApiException(appError)
    }
}



