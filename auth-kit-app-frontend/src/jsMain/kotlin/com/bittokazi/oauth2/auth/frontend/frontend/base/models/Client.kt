package com.bittokazi.oauth2.auth.frontend.frontend.base.models

import kotlinx.serialization.Serializable

@Serializable
data class Client(
    val id: String? = null,
    var clientId: String? = null,
    var resourceIds: String? = null,
    var scope: List<String>? = null,
    var clientAuthenticationMethod: String? = null,
    var authorizedGrantTypes: List<String>? = null,
    var webServerRedirectUri: List<String>? = null,
    var authorities: String? = null,
    var accessTokenValidity: Int? = null,
    var refreshTokenValidity: Int? = null,
    var additionalInformation: String? = null,
    var requireConsent: Boolean? = null,
    var postLogoutUrl: String? = null,
    var tokenType: String? = null,
    var generateSecret: Boolean = false,
    var newSecret: String? = null
)
