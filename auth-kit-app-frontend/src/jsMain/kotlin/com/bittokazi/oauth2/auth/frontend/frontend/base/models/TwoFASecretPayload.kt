package com.bittokazi.oauth2.auth.frontend.frontend.base.models

import kotlinx.serialization.Serializable

@Serializable
data class TwoFASecretPayload(
    var tenantName: String? = null,
    var secret: String? = null,
    var code: Int? = null,
    var enabled: Boolean? = null,
    var scratchCodes: List<String>? = ArrayList()
)