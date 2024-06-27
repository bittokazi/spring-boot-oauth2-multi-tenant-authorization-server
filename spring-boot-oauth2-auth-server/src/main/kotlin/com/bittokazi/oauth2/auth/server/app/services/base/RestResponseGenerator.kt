package com.bittokazi.oauth2.auth.server.app.services.base

import com.bittokazi.oauth2.auth.server.app.models.base.RestAccessDenied
import com.bittokazi.oauth2.auth.server.app.models.base.RestNotFound
import com.bittokazi.oauth2.auth.server.app.models.base.RestUnprocessableEntity
import jakarta.servlet.http.HttpServletResponse

/**
 * @author Bitto Kazi
 */
object RestResponseGenerator {
    @JvmStatic
	fun notFound(httpServletResponse: HttpServletResponse): RestNotFound {
        httpServletResponse.status = HttpServletResponse.SC_NOT_FOUND
        val restNotFound = RestNotFound()
        restNotFound.message = "Resource Not Found"
        return restNotFound
    }

    @JvmStatic
	fun accessDenied(httpServletResponse: HttpServletResponse): RestAccessDenied {
        httpServletResponse.status = HttpServletResponse.SC_FORBIDDEN
        val restAccessDenied = RestAccessDenied()
        restAccessDenied.message = "403 Resource Access Denied"
        return restAccessDenied
    }

    fun internalError(httpServletResponse: HttpServletResponse): RestAccessDenied {
        httpServletResponse.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
        val restAccessDenied = RestAccessDenied()
        restAccessDenied.message = "500"
        return restAccessDenied
    }

    fun unprocessableEntity(httpServletResponse: HttpServletResponse, message: String?): RestUnprocessableEntity {
        httpServletResponse.status = 422
        val restUnprocessableEntity = RestUnprocessableEntity()
        restUnprocessableEntity.message = message
        return restUnprocessableEntity
    }

    fun inputError(
        httpServletResponse: HttpServletResponse,
        errors: Map<String, List<String>>
    ): Map<String, List<String>> {
        httpServletResponse.contentType = "application/json"
        httpServletResponse.status = HttpServletResponse.SC_BAD_REQUEST
        return errors
    }
}
