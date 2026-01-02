package com.bittokazi.api.gateway.exception

import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.webflux.error.DefaultErrorAttributes
import org.springframework.core.annotation.MergedAnnotation
import org.springframework.core.annotation.MergedAnnotations
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.server.ResponseStatusException
import java.net.ConnectException

@Component
class CustomErrorAttributes : DefaultErrorAttributes() {
    override fun getErrorAttributes(request: ServerRequest, options: ErrorAttributeOptions): Map<String, Any?> {
        val attributes = super.getErrorAttributes(request, options)
        val error = getError(request)
        val responseStatusAnnotation = MergedAnnotations
            .from(error?.javaClass, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY).get(
                ResponseStatus::class.java
            )
        val errorStatus = determineHttpStatus(error!!, responseStatusAnnotation)
        attributes["status"] = (errorStatus["status"] as HttpStatus).value()
        attributes["message"] = errorStatus["message"]
        attributes["error"] = errorStatus["error"]
        return attributes
    }

    private fun determineHttpStatus(
        error: Throwable,
        responseStatusAnnotation: MergedAnnotation<ResponseStatus>
    ): Map<String, Any> {
        val res: MutableMap<String, Any> = mutableMapOf()
        if (error is ResponseStatusException) {
            res["status"] = error.statusCode as HttpStatus
            res["message"] = error.message as String
            res["error"] = error.reason as String
            return res
        }
        if(responseStatusAnnotation.getValue("code", HttpStatus::class.java).isPresent) {
            res["status"] = responseStatusAnnotation.getValue("code", HttpStatus::class.java)
            res["message"] = "Error Occurred"
            res["error"] = "Error Occurred"
            return res
        }
        if (error is ConnectException) {
            res["status"] = HttpStatus.SERVICE_UNAVAILABLE
            res["message"] = "Service Unreachable"
            res["error"] = "Service Unreachable"
            return res
        }
        res["status"] = HttpStatus.INTERNAL_SERVER_ERROR
        res["message"] = "Internal Server Error"
        res["error"] = "Internal Server Error"
        return res
    }
}
