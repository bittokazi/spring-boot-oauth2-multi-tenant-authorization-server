package com.bittokazi.oauth2.auth.frontend.frontend.base.models

import kotlinx.serialization.Serializable

@Serializable
data class TenantInfo (
    val cpanel: Boolean = true,
    val enabledConfigPanel: Boolean = true,
    val name: String = "",
    val systemVersion: String = "v0.0.0_Dev"
)
