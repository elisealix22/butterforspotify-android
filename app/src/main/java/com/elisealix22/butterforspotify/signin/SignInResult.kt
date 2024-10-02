package com.elisealix22.butterforspotify.signin

sealed class SignInResult {
    data object Canceled : SignInResult()
    data object BadState : SignInResult()
    data class Error(val errorMessage: String?): SignInResult()
    data class Success(val accessToken: String): SignInResult()
}
