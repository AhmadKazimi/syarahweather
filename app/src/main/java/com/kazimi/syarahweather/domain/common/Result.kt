package com.kazimi.syarahweather.domain.common

import com.kazimi.syarahweather.domain.common.error.AppError
import com.kazimi.syarahweather.domain.common.error.toAppError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>

    data class Error(val appError: AppError) : Result<Nothing>

    object Loading : Result<Nothing>
}

fun <T> Flow<T>.toResult(): Flow<Result<T>> {
    return this
        .map<T, Result<T>> { Result.Success(it) }
        .onStart { emit(Result.Loading) }
        .catch {
            it.printStackTrace()
            emit(Result.Error(it.toAppError()))
        }
}

fun <T> Flow<T>.catchError(): Flow<Result<T>> {
    return this
        .map<T, Result<T>> { Result.Success(it) }
        .catch {
            it.printStackTrace()
            emit(Result.Error(it.toAppError()))
        }
}

fun <T> Result<T>.getResult(): T? {
    return if (this is Result.Success) {
        this.data
    } else {
        null
    }
}

fun <T> Result<T>.isSuccessThen(then: (T) -> Unit) {
    if (this.isSuccess()) {
        then((this as Result.Success).data)
    }
}

suspend fun <T> Result<T>.isSuccessSuspendThen(then: suspend (T) -> Unit) {
    if (this.isSuccess()) {
        then((this as Result.Success).data)
    }
}

fun <T> Result<T>.isSuccess() = this is Result.Success

fun <T> Result<T>.isError() = this is Result.Error

fun <T> Result<T>.getOrNull(): T? =
    when {
        isSuccess() -> (this as Result.Success).data
        else -> null
    }

fun <T> Result<T>.exceptionOrNull(): AppError? =
    when {
        isError() -> (this as Result.Error).appError
        else -> null
    } 