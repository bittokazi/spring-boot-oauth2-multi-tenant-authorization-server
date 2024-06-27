package com.bittokazi.oauth2.auth.server.app.models.tenant.security

import java.io.Serializable

data class RoleOauth(
    var id: String? = null,
    var name: String? = null,
    var title: String? = null
) : Serializable
