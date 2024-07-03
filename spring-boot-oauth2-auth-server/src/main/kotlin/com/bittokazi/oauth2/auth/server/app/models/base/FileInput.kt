package com.bittokazi.oauth2.auth.server.app.models.base

import org.springframework.web.multipart.MultipartFile

data class FileInput(
    val file: MultipartFile,
    val folder: String,
    val fileName: String = ""
)
