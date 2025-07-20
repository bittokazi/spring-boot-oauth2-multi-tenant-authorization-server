package com.bittokazi.oauth2.auth.server.config.security.oauth2.device

import org.apache.commons.logging.LogFactory
import org.springframework.core.log.LogMessage
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.*
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcScopes
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames.ID_TOKEN
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2DeviceCodeAuthenticationToken
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator
import java.security.Principal
import java.util.function.Consumer

class CustomOAuth2DeviceCodeAuthenticationProvider(
    private val authorizationService: OAuth2AuthorizationService,
    private val tokenGenerator: OAuth2TokenGenerator<out OAuth2Token>
) : AuthenticationProvider {

    private val logger = LogFactory.getLog(this::class.java)

    companion object {
        private const val DEFAULT_ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2"
        private const val DEVICE_ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc8628#section-3.5"

        val DEVICE_CODE_TOKEN_TYPE = OAuth2TokenType(OAuth2ParameterNames.DEVICE_CODE)
        const val EXPIRED_TOKEN = "expired_token"
        const val AUTHORIZATION_PENDING = "authorization_pending"
    }

    override fun authenticate(authentication: Authentication): Authentication {
        val deviceCodeAuthentication = authentication as OAuth2DeviceCodeAuthenticationToken
        val clientPrincipal = getAuthenticatedClientElseThrowInvalidClient(deviceCodeAuthentication)
        val registeredClient = clientPrincipal.registeredClient

        logger.trace("Retrieved registered client")

        var authorization = authorizationService.findByToken(deviceCodeAuthentication.deviceCode, DEVICE_CODE_TOKEN_TYPE)
            ?: throw OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT)

        logger.trace("Retrieved authorization with device code")

        val userCode = authorization.getToken(OAuth2UserCode::class.java)
        val deviceCode = authorization.getToken(OAuth2DeviceCode::class.java)

        if (registeredClient.id != authorization.registeredClientId) {
            if (!deviceCode.isInvalidated) {
                authorization = invalidate(authorization, deviceCode.token)
                authorizationService.save(authorization)
                logger.warn(
                    LogMessage.format(
                        "Invalidated device code used by registered client '%s'",
                        authorization.registeredClientId
                    )
                )
            }
            throw OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT)
        }

        if (!userCode.isInvalidated) {
            val error = OAuth2Error(AUTHORIZATION_PENDING, null, DEVICE_ERROR_URI)
            throw OAuth2AuthenticationException(error)
        }

        if (deviceCode.isInvalidated) {
            val error = OAuth2Error(OAuth2ErrorCodes.ACCESS_DENIED, null, DEVICE_ERROR_URI)
            throw OAuth2AuthenticationException(error)
        }

        if (deviceCode.isExpired) {
            authorization = invalidate(authorization, deviceCode.token)
            authorizationService.save(authorization)
            logger.warn(
                LogMessage.format(
                    "Invalidated device code used by registered client '%s'",
                    authorization.registeredClientId
                )
            )
            val error = OAuth2Error(EXPIRED_TOKEN, null, DEVICE_ERROR_URI)
            throw OAuth2AuthenticationException(error)
        }

        logger.trace("Validated device token request parameters")

        val tokenContextBuilder = DefaultOAuth2TokenContext.builder()
            .registeredClient(registeredClient)
            .principal(authorization.getAttribute(Principal::class.java.name))
            .authorizationServerContext(AuthorizationServerContextHolder.getContext())
            .authorization(authorization)
            .authorizedScopes(authorization.authorizedScopes)
            .authorizationGrantType(AuthorizationGrantType.DEVICE_CODE)
            .authorizationGrant(deviceCodeAuthentication)

        val authorizationBuilder = OAuth2Authorization.from(authorization)
            .token(deviceCode.token) { metadata ->
                metadata[OAuth2Authorization.Token.INVALIDATED_METADATA_NAME] = true
            }

        var tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build()
        val generatedAccessToken = tokenGenerator.generate(tokenContext)
            ?: throw OAuth2AuthenticationException(
                OAuth2Error(
                    OAuth2ErrorCodes.SERVER_ERROR,
                    "The token generator failed to generate the access token.",
                    DEFAULT_ERROR_URI
                )
            )

        logger.trace("Generated access token")

        val accessToken = accessToken(authorizationBuilder, generatedAccessToken, tokenContext)

        var refreshToken: OAuth2RefreshToken? = null
        if (AuthorizationGrantType.REFRESH_TOKEN in registeredClient.authorizationGrantTypes) {
            tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build()
            val generatedRefreshToken = tokenGenerator.generate(tokenContext)
            if (generatedRefreshToken !is OAuth2RefreshToken) {
                throw OAuth2AuthenticationException(
                    OAuth2Error(
                        OAuth2ErrorCodes.SERVER_ERROR,
                        "The token generator failed to generate the refresh token.",
                        DEFAULT_ERROR_URI
                    )
                )
            }
            logger.trace("Generated refresh token")
            refreshToken = generatedRefreshToken
            authorizationBuilder.refreshToken(refreshToken)
        }


        // ----- ID token -----
        val idToken: OidcIdToken?
        if (authorization.authorizedScopes.contains(OidcScopes.OPENID)) {
            tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType(ID_TOKEN)).build()
            val generatedIdToken: OAuth2Token? = this.tokenGenerator.generate(tokenContext)

            val claims: MutableMap<String, Any> = if (generatedIdToken is ClaimAccessor) {
                generatedIdToken.claims.toMutableMap()
            } else {
                throw OAuth2AuthenticationException(
                    OAuth2Error(
                        OAuth2ErrorCodes.SERVER_ERROR,
                        "ID Token must contain claims",
                        DEFAULT_ERROR_URI
                    )
                )
            }

            claims["scope"] = authorization.authorizedScopes

            idToken = OidcIdToken(
                generatedIdToken?.getTokenValue(), generatedIdToken?.getIssuedAt(),
                generatedIdToken?.getExpiresAt(), claims
            )
            authorizationBuilder.token<OidcIdToken?>(
                idToken,
                Consumer { metadata: MutableMap<String?, Any?>? ->
                    metadata!!.put(
                        OAuth2Authorization.Token.CLAIMS_METADATA_NAME,
                        claims
                    )
                })
        } else {
            idToken = null
        }

        authorizationService.save(authorizationBuilder.build())

        val additionalParameters = mutableMapOf<String?, Any?>()
        if (idToken != null) {
            additionalParameters.put(OidcParameterNames.ID_TOKEN, idToken.getTokenValue())
        }

        logger.trace("Authenticated device token request")

        return OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken, refreshToken, additionalParameters)
    }

    fun getAuthenticatedClientElseThrowInvalidClient(authentication: Authentication): OAuth2ClientAuthenticationToken {
        val clientPrincipal = authentication.principal
        if (clientPrincipal !is OAuth2ClientAuthenticationToken || !clientPrincipal.isAuthenticated) {
            throw OAuth2AuthenticationException(
                OAuth2Error(
                    OAuth2ErrorCodes.INVALID_CLIENT,
                    "Client authentication failed",
                    "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2"
                )
            )
        }
        return clientPrincipal
    }

    fun <T : OAuth2Token?> invalidate(authorization: OAuth2Authorization, token: T?): OAuth2Authorization {
        // @formatter:off

        val authorizationBuilder = OAuth2Authorization.from(authorization)
            .token<T?>(token,
                Consumer {metadata: MutableMap<String?, Any?>? -> metadata!!.put(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME, true)})

//        if (OAuth2RefreshToken::class.java.isAssignableFrom(token::class.java.javaClass)) {
            authorizationBuilder.token<OAuth2AccessToken?>(
                authorization.getAccessToken().getToken(),
                Consumer {metadata: MutableMap<String?, Any?>? -> metadata!!.put(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME, true)})

            val authorizationCode =
                authorization.getToken<OAuth2AuthorizationCode?>(OAuth2AuthorizationCode::class.java)
            if (authorizationCode != null && !authorizationCode.isInvalidated()) {
                authorizationBuilder.token<OAuth2AuthorizationCode?>(
                    authorizationCode.getToken(),
                    Consumer {metadata: MutableMap<String?, Any?>? -> metadata!!.put(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME, true)})
            }
//        }

        // @formatter:on
        return authorizationBuilder.build()
    }

    fun <T : OAuth2Token?> accessToken(
        builder: OAuth2Authorization.Builder, token: T?,
        accessTokenContext: OAuth2TokenContext
    ): OAuth2AccessToken {
        val accessToken = OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER, token!!.getTokenValue(),
            token.getIssuedAt(), token.getExpiresAt(), accessTokenContext.getAuthorizedScopes()
        )
        val accessTokenFormat = accessTokenContext.getRegisteredClient()
            .getTokenSettings()
            .getAccessTokenFormat()
        builder.token<OAuth2AccessToken?>(accessToken, Consumer { metadata: MutableMap<String?, Any?>? ->
            if (token is ClaimAccessor) {
                metadata!!.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, token.getClaims())
            }
            metadata!!.put(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME, false)
            metadata.put(OAuth2TokenFormat::class.java.getName(), accessTokenFormat.getValue())
        })

        return accessToken
    }

    override fun supports(authentication: Class<*>): Boolean {
        return OAuth2DeviceCodeAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}
