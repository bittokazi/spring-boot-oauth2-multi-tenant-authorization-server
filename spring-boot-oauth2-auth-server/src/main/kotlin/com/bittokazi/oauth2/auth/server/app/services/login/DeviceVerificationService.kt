package com.bittokazi.oauth2.auth.server.app.services.login

import com.bittokazi.oauth2.auth.server.app.repositories.master.TenantRepository
import com.bittokazi.oauth2.auth.server.config.AppConfig
import com.bittokazi.oauth2.auth.server.config.TenantContext
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
import org.springframework.web.servlet.ModelAndView
import java.security.Principal
import kotlin.collections.set

@Service
class DeviceVerificationService(
    private val authorizationService: OAuth2AuthorizationService,
    private val registeredClientRepository: RegisteredClientRepository,
    private val tenantRepository: TenantRepository,
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
        val tenantOptional = tenantRepository.findOneByCompanyKey(TenantContext.getCurrentTenant()!!)
        model.addAttribute(
            "tenantName",
            if (tenantOptional.isPresent) tenantOptional.get().name else AppConfig.DEFAULT_APP_NAME
        )
        model.addAttribute(
            "signInBtnColor",
            if (tenantOptional.isPresent) tenantOptional.get().signInBtnColor else "#7367f0 !important"
        )

        val viewName: String = when(tenantOptional.isPresent && tenantOptional.get().enableCustomTemplate == true) {
            true -> "${tenantOptional.get().companyKey}/device-verification"
            else -> "device-verification"
        }

        if (httpServletRequest.userPrincipal == null) {
            httpServletRequest.session.setAttribute("device_verification_redirect", true)
            httpServletResponse.sendRedirect("/login")
            return viewName
        }

        if (userCode == null) {
            model.addAllAttributes(mapOf(
                "userCode" to "",
            ))
            return viewName
        }

        val authorization = authorizationService.findByToken(
            userCode,
            OAuth2TokenType(USER_CODE_PARAMETER_NAME)
        )

        if (authorization == null ||
            authorization.getToken(OAuth2UserCode::class.java)?.metadata
                ?.get(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME) as? Boolean ?: false) {

            val viewNameResult: String = when(tenantOptional.isPresent && tenantOptional.get().enableCustomTemplate == true) {
                true -> "${tenantOptional.get().companyKey}/device-verification-result"
                else -> "device-verification-result"
            }

            model.addAttribute("message", "Invalid or expired user code.")
            return viewNameResult
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

        val modelAndView = ModelAndView(viewName, model as Map<String, *>)
        return modelAndView
    }

    fun authorizeDevice(
        userCode: String?,
        model: Model,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any {
        val tenantOptional = tenantRepository.findOneByCompanyKey(TenantContext.getCurrentTenant()!!)
        model.addAttribute(
            "tenantName",
            if (tenantOptional.isPresent) tenantOptional.get().name else AppConfig.DEFAULT_APP_NAME
        )
        model.addAttribute(
            "signInBtnColor",
            if (tenantOptional.isPresent) tenantOptional.get().signInBtnColor else "#7367f0 !important"
        )

        val viewName: String = when(tenantOptional.isPresent && tenantOptional.get().enableCustomTemplate == true) {
            true -> "${tenantOptional.get().companyKey}/device-verification-result"
            else -> "device-verification-result"
        }

        if (httpServletRequest.userPrincipal == null) {
            httpServletRequest.session.setAttribute("device_verification_redirect", true)
            httpServletResponse.sendRedirect("/login")
            return "device-verification"
        }

        val authorization = authorizationService.findByToken(userCode, OAuth2TokenType(USER_CODE_PARAMETER_NAME))

        if (authorization == null ||
            authorization?.getToken(OAuth2UserCode::class.java)?.metadata?.get(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME) as? Boolean ?: false) {
            model.addAttribute("message", "Invalid or expired user code.")
            return viewName
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
            return viewName
        }

        val registeredClient = registeredClientRepository.findById(authorization.registeredClientId)

        if (registeredClient == null) {
            model.addAttribute("message", "Invalid Client.")
            return viewName
        }

        if(httpServletRequest.getParameter("user_oauth_approval") == "true") {
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
        } else {
            authorizationService.remove(authorization)
            model.addAttribute("message", "Denied by user")
        }
        return viewName
    }
}
