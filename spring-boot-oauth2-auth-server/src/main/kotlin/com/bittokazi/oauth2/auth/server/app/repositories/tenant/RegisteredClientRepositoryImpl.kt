package com.bittokazi.oauth2.auth.server.app.repositories.tenant

import com.bittokazi.oauth2.auth.server.app.models.tenant.OauthClient
import com.bittokazi.oauth2.auth.server.config.TenantContext
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import java.time.Duration

class RegisteredClientRepositoryImpl(
    private val oauthClientRepository: OauthClientRepository
) : RegisteredClientRepository {
    override fun save(registeredClient: RegisteredClient) {
    }

    override fun findById(id: String): RegisteredClient? {
        val registeredClientOptional = oauthClientRepository.findOneById(id)
        if (registeredClientOptional!!.isPresent) {
            return generateRegisteredClient(registeredClientOptional.get())
        }
        return null
    }

    override fun findByClientId(clientId: String): RegisteredClient? {
        val registeredClientOptional = oauthClientRepository.findOneByClientId(clientId)
        if (registeredClientOptional!!.isPresent) {
            return generateRegisteredClient(registeredClientOptional.get())
        }
        return null
    }

    private fun generateRegisteredClient(oauthClient: OauthClient): RegisteredClient {
        val oidcClient = RegisteredClient.withId(oauthClient.id)
            .clientId(oauthClient.clientId)
            .clientSecret(BCrypt.hashpw(oauthClient.clientSecret!!, BCrypt.gensalt()))
            .clientAuthenticationMethod(ClientAuthenticationMethod(oauthClient.clientAuthenticationMethod))
            .postLogoutRedirectUri(oauthClient.postLogoutUrl) //                .scope(String.join(",", oauthClient.getScope()))
            .clientSettings(
                ClientSettings.builder()
                    .requireProofKey(false)
                    .requireAuthorizationConsent(oauthClient.requireConsent!!).build()
            )
            .tokenSettings(
                TokenSettings.builder()
                    .accessTokenFormat(if (oauthClient.tokenType == "jwt") OAuth2TokenFormat.SELF_CONTAINED else OAuth2TokenFormat.REFERENCE)
                    .accessTokenTimeToLive(Duration.ofSeconds(oauthClient.accessTokenValidity!!.toLong()))
                    .refreshTokenTimeToLive(Duration.ofSeconds(oauthClient.refreshTokenValidity!!.toLong())).build()
            )
        oauthClient.authorizedGrantTypesAsSet().forEach { s ->
            oidcClient.authorizationGrantType(AuthorizationGrantType(s))
        }
        oauthClient.scopeAsSet().forEach { s ->
            oidcClient.scope(s)
        }
        oauthClient.webServerRedirectUriAsSet().forEach { s ->
            oidcClient.redirectUri(s)
        }
        return oidcClient.build()
    }
}
