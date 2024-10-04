package com.elisealix22.butterforspotify.data.error

data class ErrorResponse(
    val error: ErrorMessageResponse
)

data class ErrorMessageResponse(
    val message: String
)
