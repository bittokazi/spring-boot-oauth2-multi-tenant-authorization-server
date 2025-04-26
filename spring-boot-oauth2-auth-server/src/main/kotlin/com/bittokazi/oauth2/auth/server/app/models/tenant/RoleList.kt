package com.bittokazi.oauth2.auth.server.app.models.tenant

data class RoleList(
    val pages: Int,
    val records: Long,
    val roles: List<Role?>
)
