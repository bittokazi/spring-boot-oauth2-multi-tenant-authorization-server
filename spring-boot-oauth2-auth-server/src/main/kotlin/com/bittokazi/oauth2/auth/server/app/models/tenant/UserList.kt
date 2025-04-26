package com.bittokazi.oauth2.auth.server.app.models.tenant

data class UserList(
    val pages: Int,
    val records: Long,
    val users: List<User?>
)
