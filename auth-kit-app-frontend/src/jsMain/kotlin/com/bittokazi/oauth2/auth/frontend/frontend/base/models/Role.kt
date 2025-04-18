package com.bittokazi.oauth2.auth.frontend.frontend.base.models

import kotlinx.serialization.Serializable

@Serializable
data class Role(
    val id: String? = null,
    val title: String? = "",
    val name: String? = "",
    val description: String? = ""
)
