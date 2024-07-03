package com.bittokazi.oauth2.auth.server.app.services.file

import com.bittokazi.oauth2.auth.server.app.models.base.FileInput
import com.bittokazi.oauth2.auth.server.app.models.base.UploadObject

interface FileService {
    fun save(fileInput: FileInput): UploadObject
}

