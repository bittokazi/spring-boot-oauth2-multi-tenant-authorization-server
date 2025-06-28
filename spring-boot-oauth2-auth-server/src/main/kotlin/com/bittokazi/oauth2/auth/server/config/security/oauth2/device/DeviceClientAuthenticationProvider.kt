package com.bittokazi.oauth2.auth.server.config.security.oauth2.device

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken

class DeviceClientAuthenticationProvider(
    private val registeredClientRepository: RegisteredClientRepository
) : AuthenticationProvider {
    override fun authenticate(authentication: Authentication): Authentication {
        val clientAuthentication = authentication as OAuth2ClientAuthenticationToken
        val clientId = clientAuthentication.principal.toString()

        val registeredClient = registeredClientRepository.findByClientId(clientId)
            ?: throw OAuth2AuthenticationException("invalid_client")

        return OAuth2ClientAuthenticationToken(
            registeredClient,
            clientAuthentication.clientAuthenticationMethod,
            clientAuthentication.credentials
        )
    }

    override fun supports(authentication: Class<*>): Boolean {
        return OAuth2ClientAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}
