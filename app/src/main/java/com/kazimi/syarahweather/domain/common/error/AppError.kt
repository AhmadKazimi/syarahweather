package com.kazimi.syarahweather.domain.common.error

data class AppError(
    val type: ErrorType = ErrorType.UNKNOWN,
    var message: String = "", // Keep for backward compatibility and custom messages
    var code: Int? = null,
    val throwable: Throwable? = null,
    val params: List<Any> = emptyList() // For parameterized error messages
)

enum class ErrorType {
    UNKNOWN,
    NETWORK_ERROR,
    LOCATION_MAX_EXCEEDED,
    LOCATION_ALREADY_EXISTS,
    LOCATION_SAVE_FAILED,
    LOCATION_LOAD_FAILED,
    LOCATION_DATA_NULL,
    LOCATION_GET_FAILED,
    WEATHER_LOAD_FAILED,
    FORECAST_LOAD_FAILED,
    PERMISSION_DENIED,
    PERMISSION_PERMANENTLY_DENIED
}

fun Throwable.toAppError(): AppError {
    return AppError(
        type = ErrorType.UNKNOWN,
        message = this.message ?: "",
        throwable = this
    )
} 