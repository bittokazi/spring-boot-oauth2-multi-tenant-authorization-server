package com.bittokazi.oauth2.auth.server.app.controllers

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2DeviceCode
import org.springframework.security.oauth2.core.OAuth2UserCode
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import java.lang.reflect.Field
import java.security.Principal
import java.util.*


@Controller
@RequestMapping("/device-verification")
class DeviceVerificationController(
    private val authorizationService: OAuth2AuthorizationService,
    private val registeredClientRepository: RegisteredClientRepository
) {

    @GetMapping
    fun showVerificationPage(
        @RequestParam("user_code") userCode: String,
        @RequestParam(name = "client_id", required = false) clientId: String?,
        @RequestParam(name = "scope", required = false) scopes: String?,
        @RequestParam(name = "state", required = false) state: String?,
        model: Model
    ): String {
        // Find pending authorization by user code
        val authorization = authorizationService.findByToken(
            userCode,
            OAuth2TokenType(USER_CODE_PARAMETER_NAME)
        ) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid user code")

        val resolvedState = state ?: UUID.randomUUID().toString()

        // Get client ID from authorization if not provided
        val resolvedClientId = clientId ?: authorization.registeredClientId

        // Get default scopes if not provided
        val resolvedScopes = scopes ?: authorization.authorizedScopes?.joinToString(" ")

        // Store state in authorization if it was generated
        if (state == null) {
            val updatedAuthorization = OAuth2Authorization.from(authorization)
                .attribute("state", resolvedState)
                .build()
            authorizationService.save(updatedAuthorization)
        }

        model.addAllAttributes(mapOf(
            "clientId" to resolvedClientId,
            "userCode" to userCode,
            "scopes" to (resolvedScopes ?: ""),
            "state" to resolvedState
        ))
        return "device-verification"
    }

    @PostMapping()
    fun verifyUserCode(@RequestParam("user_code") userCode: String?, model: Model, httpServletRequest: HttpServletRequest): String {
        val authorization = authorizationService.findByToken(userCode, OAuth2TokenType(USER_CODE_PARAMETER_NAME))

        if (authorization == null) {
            model.addAttribute("error", "Invalid or expired user code.")
            return "device-verification-form"
        }

        val userAuthentication: Authentication = UsernamePasswordAuthenticationToken(
            httpServletRequest.userPrincipal,
            null,
            listOf(SimpleGrantedAuthority("ROLE_USER"))
        )

        // Build an access token to mark approval

        val authorizationBuilder = OAuth2Authorization.from(authorization)

        val token = authorization.getToken(OAuth2DeviceCode::class.java)?.token
            ?: throw IllegalStateException("Device code not found")

        val userCode = authorization.getToken(OAuth2UserCode::class.java)?.token
            ?: throw IllegalStateException("User code not found")

        val registeredClient = registeredClientRepository.findById(authorization.registeredClientId)
            ?: throw OAuth2AuthenticationException("invalid_client")

        val updatedAuthorization = authorizationBuilder
            .principalName(httpServletRequest.userPrincipal.name)
            .attribute(Principal::class.java.name, userAuthentication)
            .token(userCode) { metadata ->
                metadata[OAuth2Authorization.Token.INVALIDATED_METADATA_NAME] = true
            }
            .authorizedScopes(registeredClient.scopes) // ✅ tells Spring it's approved
            .build()

        authorizationService.save(updatedAuthorization)

        //authorizationService.save(updated)

        return "device-verification-success"
    }

    companion object {
        const val USER_CODE_PARAMETER_NAME = "user_code"
    }

    fun findUserCodeToken(authorization: OAuth2Authorization): OAuth2Authorization.Token<*>? {
        val field: Field = OAuth2Authorization::class.java.getDeclaredField("tokens")
        field.isAccessible = true
        val tokens = field.get(authorization) as Map<*, *>

        return tokens.values
            .filterIsInstance<OAuth2Authorization.Token<*>>()
            .firstOrNull { token ->
                val metadata = token::class.java.getDeclaredField("metadata")
                metadata.isAccessible = true
                val map = metadata.get(token) as? Map<*, *>
                map?.get("token_type") == "user_code"
            }
    }
}
