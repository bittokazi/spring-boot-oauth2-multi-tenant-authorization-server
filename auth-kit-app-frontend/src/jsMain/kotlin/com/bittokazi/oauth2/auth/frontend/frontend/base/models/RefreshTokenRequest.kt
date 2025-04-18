package com.bittokazi.oauth2.auth.frontend.frontend.base.models

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(
    val refresh_token: String
)
