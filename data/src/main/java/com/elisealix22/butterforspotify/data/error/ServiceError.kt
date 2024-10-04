package com.elisealix22.butterforspotify.data.error

sealed class ServiceError(
    open val loggingMessage: String? = null,
    open val userFriendlyMessage: String? = null
) : Exception(loggingMessage) {

    data class ApiError(
        val code: Int,
        override val loggingMessage: String?,
        override val userFriendlyMessage: String?
    ) : ServiceError(loggingMessage)

    data class IOError(
        override val loggingMessage: String?
    ) : ServiceError(loggingMessage)

    data class UnexpectedResponseError(
        override val loggingMessage: String?
    ) : ServiceError(loggingMessage)
}
