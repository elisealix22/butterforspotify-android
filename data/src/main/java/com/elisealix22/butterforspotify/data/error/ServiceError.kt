package com.elisealix22.butterforspotify.data.error

sealed class ServiceError(
    loggingMessage: String? = null
) : Exception(loggingMessage) {

    data class ApiError(
        val code: Int,
        val loggingMessage: String?,
        val userFriendlyMessage: String?
    ) : ServiceError(loggingMessage)

    data class IOError(
        val loggingMessage: String?
    ) : ServiceError(loggingMessage)

    data class UnexpectedResponseError(
        val loggingMessage: String?
    ) : ServiceError(loggingMessage)
}
