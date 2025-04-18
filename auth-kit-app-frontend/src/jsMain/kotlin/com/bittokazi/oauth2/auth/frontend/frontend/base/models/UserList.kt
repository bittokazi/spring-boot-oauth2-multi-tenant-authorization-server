package com.bittokazi.oauth2.auth.frontend.frontend.base.models

import kotlinx.serialization.Serializable

@Serializable
data class UserList(
    val pages: Int,
    val records: Int,
    val users: List<User>
)
