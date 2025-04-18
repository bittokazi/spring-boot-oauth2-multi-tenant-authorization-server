package com.bittokazi.oauth2.auth.frontend.frontend.base.services

import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.FileResponse
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.Result
import io.kvision.form.upload.UploadInput
import io.kvision.jquery.jQuery
import kotlinx.serialization.json.Json
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.xhr.FormData
import kotlin.js.json

class FileService {

    fun upload(
        fileName: String,
        uploadInput: UploadInput,
        retry: Boolean = true,
        fn: (Result<FileResponse, FileServiceError>) -> Unit
    ) {
        val formData = FormData()

        uploadInput.value?.forEach { file ->
            console.log(uploadInput.getNativeFile(file)!!.size)
            formData.set("file", uploadInput.getNativeFile(file)!!)
            formData.set(
                "uploadObject",
                Blob(
                    blobParts = arrayOf(
                        Json.encodeToString(
                            mapOf(
                                "filename" to fileName,
                                "absoluteFilePath" to ""
                            )
                        )
                    ),
                    BlobPropertyBag("application/json")
                )
            )
        }

        val token = AppEngine.defaultAuthHolder.getAuth()?.token ?: run { return@run "" }

        jQuery.ajaxSettings.contentType = false
        jQuery.ajaxSettings.processData = false
        jQuery.ajaxSettings.method = "POST"
        jQuery.ajaxSettings.url = "${AppEngine.restService.API}/tenants/templates"
        jQuery.ajaxSettings.data = formData
        jQuery.ajaxSettings.headers = json(
            Pair("Authorization", "Bearer $token")
        )

        jQuery.post("${AppEngine.restService.API}/tenants/templates", formData).then(
            { result: dynamic, textStatus: String, jqXHR: dynamic ->
                console.log("POST request successful")
                console.log("Result: $result")
                console.log("Status: $textStatus")
                console.log("XHR: $jqXHR")

                // Handle the response
                if (result != null) {
                    val jsonString = JSON.stringify(result)
                    val fileResponse = Json.decodeFromString<FileResponse>(jsonString)
                    fn(Result.Success(fileResponse))
                }
            },
            { jqXHR, textStatus, errorThrown ->
                console.error("POST request failed")
                console.error("XHR: $jqXHR")
                console.error("Status: $textStatus")
                console.error("Error: $errorThrown")

                val httpStatusCode = jqXHR.status.toInt() // Get the HTTP status code

                console.error("HTTP Status Code: $httpStatusCode") // Log the status code

                if (httpStatusCode == 401) {
                    if (retry) {
                        AppEngine.restService.refreshTokenAndRetry<Any>(null, null, null) {
                            if(it) {
                                upload(
                                    fileName = fileName,
                                    uploadInput = uploadInput,
                                    fn = fn,
                                    retry = false
                                )
                            } else {
                                fn(Result.Failure(FileServiceError.SERVER_ERROR))
                            }
                        }
                    } else {
                        AppEngine.authService.logout()
                    }
                } else {
                    val errorMessage = if (jqXHR.responseJSON != null) {
                        JSON.stringify(jqXHR.responseJSON)
                    } else {
                        errorThrown
                    }

                    fn(Result.Failure(FileServiceError.SERVER_ERROR))
                }
            }
        )
    }
}

enum class FileServiceError {
    SERVER_ERROR;
}
