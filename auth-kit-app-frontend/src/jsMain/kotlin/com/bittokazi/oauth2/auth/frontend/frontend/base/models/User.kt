package com.bittokazi.oauth2.auth.frontend.frontend.base.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val username: String? = null,
    val roles: List<Role>? = listOf(),
    var avatarImage: String? = null,
    var currentPassword: String? = null,
    var newPassword: String? = null,
    var newConfirmPassword: String? = null,
    var adminTenantUser: Boolean = false,
    var twoFaEnabled: Boolean? = null
)
