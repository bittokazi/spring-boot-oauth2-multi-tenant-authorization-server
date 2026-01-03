package com.bittokazi.oauth2.auth.server.app.models.tenant

data class UserList(
    var pages: Int,
    var records: Long,
    var users: List<User>
)
