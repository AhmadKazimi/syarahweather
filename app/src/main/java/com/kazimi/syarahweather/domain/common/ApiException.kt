package com.kazimi.syarahweather.domain.common

import com.kazimi.syarahweather.domain.common.error.AppError


class ApiException(val appError: AppError) : Exception()
