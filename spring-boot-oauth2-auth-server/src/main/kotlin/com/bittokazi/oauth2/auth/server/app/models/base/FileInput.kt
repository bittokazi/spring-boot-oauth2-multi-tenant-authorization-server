package com.bittokazi.oauth2.auth.server.app.models.base

import org.springframework.web.multipart.MultipartFile

data class FileInput(
    var file: MultipartFile,
    var folder: String,
    var fileName: String = ""
)
