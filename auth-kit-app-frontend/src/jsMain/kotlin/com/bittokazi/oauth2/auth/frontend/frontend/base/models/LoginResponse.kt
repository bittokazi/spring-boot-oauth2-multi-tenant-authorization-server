package com.bittokazi.oauth2.auth.frontend.frontend.base.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val access_token: String,
    val refresh_token: String
)
