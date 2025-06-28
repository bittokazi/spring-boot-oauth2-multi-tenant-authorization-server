package com.bittokazi.oauth2.auth.server.config.security.oauth2.device

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken
import org.springframework.security.web.authentication.AuthenticationConverter

class DeviceClientAuthenticationConverter : AuthenticationConverter {
    override fun convert(request: HttpServletRequest): OAuth2ClientAuthenticationToken? {
        val clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID)
            ?: return null // No client_id parameter

        return OAuth2ClientAuthenticationToken(
            clientId,
            ClientAuthenticationMethod.NONE,
            null,
            null
        )
    }
}
