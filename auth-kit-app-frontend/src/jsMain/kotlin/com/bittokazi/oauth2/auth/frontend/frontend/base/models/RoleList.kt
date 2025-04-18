package com.bittokazi.oauth2.auth.frontend.frontend.base.models

import kotlinx.serialization.Serializable

@Serializable
data class RoleList(
    val pages: Int,
    val records: Int,
    val roles: List<Role>
)
