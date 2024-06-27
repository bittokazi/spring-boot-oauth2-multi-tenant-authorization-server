package com.bittokazi.oauth2.auth.server.app.services

import org.springframework.security.oauth2.server.authorization.OAuth2Authorization
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType

class OAuth2AuthorizationServiceImpl : OAuth2AuthorizationService {
    override fun save(authorization: OAuth2Authorization) {
    }

    override fun remove(authorization: OAuth2Authorization) {
    }

    override fun findById(id: String): OAuth2Authorization? {
        return null
    }

    override fun findByToken(token: String, tokenType: OAuth2TokenType): OAuth2Authorization? {
        return null
    }
}
