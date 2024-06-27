package com.bittokazi.oauth2.auth.server.app.models.master

import java.io.Serializable

data class TenantInfo(
    val cpanel: Boolean = true,
    val enabledConfigPanel: Boolean = true,
    val name: String = ""
) : Serializable
