package com.bittokazi.oauth2.auth.frontend.frontend.base.models

import kotlinx.serialization.Serializable

@Serializable
data class UserTrustedDevice(
    val id: Long? = null,
    var user: User? = null,
    var instanceId: String? = null,
    var deviceIp: String? = null,
    var userAgent: String? = null,
    val createdDate: String?,
    var updatedDate: String?
)
