package com.bittokazi.oauth2.auth.frontend.frontend.base.models

import kotlinx.serialization.Serializable

@Serializable
data class FileResponse(
    val absoluteFilePath: String,
    val filename: String,
)
