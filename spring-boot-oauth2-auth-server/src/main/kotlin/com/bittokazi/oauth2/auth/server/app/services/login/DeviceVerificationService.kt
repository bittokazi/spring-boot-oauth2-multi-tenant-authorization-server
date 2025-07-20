package com.bittokazi.oauth2.auth.server.app.services.login

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.OAuth2UserCode
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.stereotype.Service
import org.springframework.ui.Model
import java.security.Principal
import kotlin.collections.set

@Service
class DeviceVerificationService(
    private val authorizationService: OAuth2AuthorizationService,
    private val registeredClientRepository: RegisteredClientRepository
) {

    companion object {
        const val USER_CODE_PARAMETER_NAME = "user_code"
    }

    fun getDeviceVerificationPage(
        userCode: String?,
        model: Model,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any {
        if (httpServletRequest.userPrincipal == null) {
            httpServletRequest.session.setAttribute("device_verification_redirect", true)
            httpServletResponse.sendRedirect("/login")
            return "device-verification"
        }

        if (userCode == null) {
            model.addAllAttributes(mapOf(
                "userCode" to "",
            ))
            return "device-verification"
        }

        val authorization = authorizationService.findByToken(
            userCode,
            OAuth2TokenType(USER_CODE_PARAMETER_NAME)
        )

        if (userCode != null && authorization == null) {
            model.addAttribute("message", "Invalid or expired user code.")
            return "device-verification-result"
        }

        // Get client ID from authorization if not provided
        val resolvedClientId = authorization?.registeredClientId

        // Get default scopes if not provided
        val resolvedScopes = authorization?.authorizedScopes?.joinToString(" ")

        model.addAllAttributes(mapOf(
            "clientId" to resolvedClientId,
            "userCode" to userCode,
            "scopes" to (resolvedScopes ?: "")
        ))
        return "device-verification"
    }

    fun authorizeDevice(
        userCode: String?,
        model: Model,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any {
        if (httpServletRequest.userPrincipal == null) {
            httpServletRequest.session.setAttribute("device_verification_redirect", true)
            httpServletResponse.sendRedirect("/login")
            return "device-verification"
        }

        val authorization = authorizationService.findByToken(userCode, OAuth2TokenType(USER_CODE_PARAMETER_NAME))

        if (authorization == null ||
            authorization?.getToken(OAuth2UserCode::class.java)?.metadata?.get(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME) as? Boolean ?: false) {
            model.addAttribute("message", "Invalid or expired user code.")
            return "device-verification-result"
        }

        val userAuthentication: Authentication = UsernamePasswordAuthenticationToken(
            httpServletRequest.userPrincipal,
            null,
            listOf(SimpleGrantedAuthority("ROLE_USER"))
        )

        // Build an access token to mark approval

        val authorizationBuilder = OAuth2Authorization.from(authorization)

        val userCode = authorization.getToken(OAuth2UserCode::class.java)?.token

        if (userCode == null) {
            model.addAttribute("message", "User code not found.")
            return "device-verification-result"
        }

        val registeredClient = registeredClientRepository.findById(authorization.registeredClientId)

        if (registeredClient == null) {
            model.addAttribute("message", "Invalid Client.")
            return "device-verification-result"
        }

        val updatedAuthorization = authorizationBuilder
            .principalName(httpServletRequest.userPrincipal.name)
            .attribute(Principal::class.java.name, userAuthentication)
            .token(userCode) { metadata ->
                metadata[OAuth2Authorization.Token.INVALIDATED_METADATA_NAME] = true
            }
            .authorizedScopes(registeredClient.scopes) // ✅ tells Spring it's approved
            .build()

        authorizationService.save(updatedAuthorization)

        model.addAttribute("message", "Successful")

        return "device-verification-result"
    }
}