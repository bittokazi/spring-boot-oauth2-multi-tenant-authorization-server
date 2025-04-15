package com.bittokazi.oauth2.auth.frontend.frontend.base.models

sealed class Result<T, E> {

    data class Success<T, E>(
        val outcome: T
    ) : Result<T, E>()

    data class Failure<T, E>(
        val errorCode: E,
        val errorMessage: String = "",
        val cause: Throwable? = null
    ) : Result<T, E>()
}
