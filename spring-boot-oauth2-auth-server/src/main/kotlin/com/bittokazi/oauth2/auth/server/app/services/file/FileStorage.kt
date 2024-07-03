package com.bittokazi.oauth2.auth.server.app.services.file

import com.bittokazi.oauth2.auth.server.app.models.base.FileInput
import com.bittokazi.oauth2.auth.server.app.models.base.UploadObject
import org.springframework.stereotype.Service
import java.io.File

@Service
class FileStorageProvider {

    val services: Map<String, FileService> = mapOf(
        "zip" to ZipFileSaver()
    )

    fun upload(fileInput: FileInput): UploadObject? {
        return services.get(getFileExtension(fileInput.file.originalFilename))?.save(fileInput)
    }

    fun getFileExtension(fullName: String?): String {
        if (fullName == "") return ""
        val fileName: String = File(fullName).getName()
        val dotIndex = fileName.lastIndexOf('.')
        return if ((dotIndex == -1)) "" else fileName.substring(dotIndex + 1)
    }
}
