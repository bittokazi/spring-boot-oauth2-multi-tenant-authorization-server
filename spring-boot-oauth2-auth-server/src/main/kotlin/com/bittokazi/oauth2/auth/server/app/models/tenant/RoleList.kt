package com.bittokazi.oauth2.auth.server.app.models.tenant

data class RoleList(
    var pages: Int,
    var records: Long,
    var roles: List<Role?>
)
