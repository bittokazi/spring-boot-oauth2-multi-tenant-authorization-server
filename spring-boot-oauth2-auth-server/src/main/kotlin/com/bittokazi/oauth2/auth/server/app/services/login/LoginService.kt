package com.bittokazi.oauth2.auth.server.app.services.login

import com.bittokazi.oauth2.auth.server.app.models.base.Oauth2Response
import com.bittokazi.oauth2.auth.server.app.models.master.Tenant
import com.bittokazi.oauth2.auth.server.app.models.tenant.OauthClient
import com.bittokazi.oauth2.auth.server.app.repositories.master.TenantRepository
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.OauthClientRepository
import com.bittokazi.oauth2.auth.server.config.AppConfig
import com.bittokazi.oauth2.auth.server.config.TenantContext
import com.bittokazi.oauth2.auth.server.utils.CookieActionsProvider
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.core.oidc.OidcScopes
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService
import org.springframework.security.web.savedrequest.HttpSessionRequestCache
import org.springframework.stereotype.Service
import org.springframework.ui.Model
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.ModelAndView
import java.io.IOException
import java.security.Principal
import java.util.*

@Service
class LoginService(
    private val oauthClientRepository: OauthClientRepository,
    private val tenantRepository: TenantRepository,
    private val customJdbcOAuth2AuthorizationConsentService: OAuth2AuthorizationConsentService
) {

    @Throws(IOException::class)
    fun loginPage(
        model: Model,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Any {
        checkDeviceId(request, response)
        val savedRequest = HttpSessionRequestCache().getRequest(request, response)
        if (null != savedRequest) {
            val targetUrl = savedRequest.redirectUrl
            if (Objects.nonNull(request.userPrincipal)) {
                response.sendRedirect(targetUrl)
                HttpSessionRequestCache().removeRequest(request, response)
            }
        } else {
            if (Objects.nonNull(request.userPrincipal)) {
                val tenantOptional = tenantRepository
                    .findOneByCompanyKey(TenantContext.getCurrentTenant()!!)
                if (tenantOptional.isPresent) {
                    if (tenantOptional.get().defaultRedirectUrl != "") {
                        response.sendRedirect(tenantOptional.get().defaultRedirectUrl)
                    } else {
                        response.sendRedirect("/oauth2/login")
                    }
                } else {
                    response.sendRedirect("/oauth2/login")
                }
            }
        }
        val tenantOptional = tenantRepository.findOneByCompanyKey(TenantContext.getCurrentTenant()!!)
        model.addAttribute(
            "tenantName",
            if (tenantOptional.isPresent) tenantOptional.get().name else AppConfig.DEFAULT_APP_NAME
        )
        model.addAttribute(
            "signInBtnColor",
            if (tenantOptional.isPresent) tenantOptional.get().signInBtnColor else "#7367f0 !important"
        )
        model.addAttribute(
            "resetPasswordLink", if (tenantOptional.isPresent) tenantOptional.get().resetPasswordLink
            else AppConfig.DEFAULT_RESET_PASSWORD_LINK
        )
        model.addAttribute(
            "createAccountLink",
            if (tenantOptional.isPresent) tenantOptional.get().createAccountLink else ""
        )
        if (Objects.nonNull(request.getParameter("otp"))) {
            model.addAttribute("otpError", request.getParameter("otp"))
        }
        val modelAndView = ModelAndView("login", model as Map<String, *>)
        return modelAndView
    }

    @Throws(IOException::class)
    fun otpLoginPage(
        model: Model, request: HttpServletRequest,
        response: HttpServletResponse
    ): Any {
        val session = request.getSession(false)
        if (Objects.isNull(session.getAttribute("otpRequired"))) {
            response.sendRedirect(request.contextPath + "/login?otp=error")
            return ""
        }
        val tenantOptional = tenantRepository.findOneByCompanyKey(TenantContext.getCurrentTenant()!!)
        model.addAttribute("tenantName", if (tenantOptional.isPresent) tenantOptional.get().name else "AuthKit")
        model.addAttribute(
            "signInBtnColor",
            if (tenantOptional.isPresent) tenantOptional.get().signInBtnColor else "#7367f0 !important"
        )
        setOtpParam(model, session, "otpRequiredUsername")
        setOtpParam(model, session, "otpRequiredPassword")
        setOtpParam(model, session, "otpRequiredRememberMe")
        setOtpParam(model, session, "otpRequiredTrustDevice")
        setOtpParam(model, session, "otpRequired")
        setOtpParam(model, session, "message")
        val modelAndView = ModelAndView("otp-login", model as Map<String, *>)
        return modelAndView
    }

    fun consentPage(
        principal: Principal, model: Model,
        @RequestParam(OAuth2ParameterNames.CLIENT_ID) clientId: String,
        @RequestParam(OAuth2ParameterNames.SCOPE) scope: String,
        @RequestParam(OAuth2ParameterNames.STATE) state: String,
        @RequestParam(name = OAuth2ParameterNames.USER_CODE, required = false) userCode: String?
    ): ModelAndView {
        // Remove scopes that were already approved

        val scopesToApprove: MutableSet<String> = HashSet()
        val previouslyApprovedScopes: MutableSet<String> = HashSet()
        val registeredClient = oauthClientRepository.findOneByClientId(clientId)
        val currentAuthorizationConsent = customJdbcOAuth2AuthorizationConsentService
            .findById(registeredClient.get().id, principal.name)
        val authorizedScopes = if (currentAuthorizationConsent != null) {
            currentAuthorizationConsent.scopes
        } else {
            emptySet()
        }
        for (requestedScope in StringUtils.delimitedListToStringArray(scope, " ")) {
            if (OidcScopes.OPENID == requestedScope) {
                continue
            }
            if (authorizedScopes.contains(requestedScope)) {
                previouslyApprovedScopes.add(requestedScope)
            } else {
                scopesToApprove.add(requestedScope)
            }
        }

        model.addAttribute("clientId", clientId)
        model.addAttribute("state", state)
        model.addAttribute("scopes", withDescription(scopesToApprove))
        model.addAttribute("previouslyApprovedScopes", withDescription(previouslyApprovedScopes))
        model.addAttribute("principalName", principal.name)
        model.addAttribute("userCode", userCode)
        if (StringUtils.hasText(userCode)) {
            model.addAttribute("requestURI", "/oauth2/device_verification")
        } else {
            model.addAttribute("requestURI", "/oauth2/authorize")
        }

        val modelAndView = ModelAndView("consent", model as Map<String, *>)
        return modelAndView
    }

    @Throws(IOException::class)
    fun authorizeUser(@RequestParam("code") code: String, httpServletResponse: HttpServletResponse): ResponseEntity<*> {
        var optionalOauthClient = Optional.empty<OauthClient>()
        var tenantOptional = Optional.empty<Tenant>()
        if (TenantContext.getCurrentTenant() == "public") {
            optionalOauthClient = oauthClientRepository.findOneById("user_login_service")
        } else {
            optionalOauthClient = oauthClientRepository.findOneById("user_login_service")
            tenantOptional = tenantRepository.findOneByCompanyKey(TenantContext.getCurrentTenant()!!)
        }

        val restTemplate = RestTemplate()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.setBasicAuth(optionalOauthClient.get().clientId, optionalOauthClient.get().clientSecret)

        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add("grant_type", "authorization_code")
        map.add("code", code)
        map.add("redirect_uri", optionalOauthClient.get().webServerRedirectUriAsSet()?.first())
        if (TenantContext.getCurrentTenant() != "public" && AppConfig.USE_X_AUTH_TENANT) {
            headers.add("X-AUTH-TENANT", TenantContext.getCurrentTenant())
        }

        val request = HttpEntity(map, headers)

        try {
            if (TenantContext.getCurrentTenant() == "public" || (TenantContext.getCurrentTenant() != "public" && AppConfig.USE_X_AUTH_TENANT)) {
                val oauth2Response = restTemplate.postForEntity(
                    System.getenv()["APPLICATION_BACKEND_URL"] + "/oauth2/token", request,
                    Oauth2Response::class.java
                )
                updateCookies(httpServletResponse, oauth2Response.body)
                if (TenantContext.getCurrentTenant() != "public" && AppConfig.USE_X_AUTH_TENANT) {
                    httpServletResponse
                        .sendRedirect(AppConfig.HTTP_SCHEMA + tenantOptional.get().domain + "/app/dashboard")
                } else {
                    httpServletResponse.sendRedirect(System.getenv()["APPLICATION_BACKEND_URL"] + "/app/dashboard")
                }
                return ResponseEntity.ok(oauth2Response.body)
                // return ResponseEntity.ok().build();
            } else {
                val oauth2Response = restTemplate.postForEntity(
                    System.getenv()["HTTP_SCHEMA"] + tenantOptional.get().domain + "/oauth2/token",
                    request, Oauth2Response::class.java
                )
                updateCookies(httpServletResponse, oauth2Response.body)
                httpServletResponse
                    .sendRedirect(AppConfig.HTTP_SCHEMA + tenantOptional.get().domain + "/app/dashboard")
                return ResponseEntity.ok(oauth2Response.body)
            }
        } catch (e: HttpStatusCodeException) {
            if (e.statusCode.is4xxClientError) {
                return ResponseEntity.status(401).body("{ message: \"Unauthorized Access\"}")
            } else if (e.statusCode.is5xxServerError) {
                return ResponseEntity.status(500).body("{ message: \"Server Error\"}")
            }
            e.printStackTrace()
        } catch (e: RestClientException) {
            e.printStackTrace()
        }
        return ResponseEntity.status(503).body("{ message: \"Identity Service Unavailable\"}")
    }

    @Throws(IOException::class)
    fun loginRedirect(httpServletResponse: HttpServletResponse) {
        val optionalOauthClient = oauthClientRepository.findOneById("user_login_service")
        val redirectUri = when(optionalOauthClient.get().webServerRedirectUriAsSet().size) {
            0 -> ""
            else -> optionalOauthClient.get().webServerRedirectUriAsSet().first()
        }

        when(TenantContext.getCurrentTenant()) {
            "public" -> {
                val url = "${AppConfig.APPLICATION_BACKEND_URL}/oauth2/authorize?client_id=" +
                        "${optionalOauthClient.get().clientId}&response_type=code&scope=" +
                        "${optionalOauthClient.get().scopeAsSet()?.joinToString("+")}&redirect_uri=${redirectUri}"
                httpServletResponse.sendRedirect(url)
            }
            else -> {
                val tenantOptional = tenantRepository.findOneByCompanyKey(TenantContext.getCurrentTenant()!!)
                httpServletResponse.sendRedirect(
                    System.getenv()["HTTP_SCHEMA"] + tenantOptional.get().domain
                            + "/oauth2/authorize?client_id=" + optionalOauthClient.get().clientId
                            + "&response_type=code&scope=" + java.lang.String.join("+", optionalOauthClient.get().scopeAsSet())
                            + "&redirect_uri="
                            + redirectUri
                )
            }
        }
    }

    @Throws(IOException::class)
    fun refreshToken(
        @RequestBody payload: HashMap<String, String>,
        httpServletResponse: HttpServletResponse
    ): ResponseEntity<*> {
        var optionalOauthClient: Optional<OauthClient>
        var tenantOptional = Optional.empty<Tenant>()
        if (TenantContext.getCurrentTenant() == "public") {
            optionalOauthClient = oauthClientRepository.findOneById("user_login_service")
        } else {
            optionalOauthClient = oauthClientRepository.findOneById("user_login_service")
            tenantOptional = tenantRepository.findOneByCompanyKey(TenantContext.getCurrentTenant()!!)
        }

        val restTemplate = RestTemplate()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.setBasicAuth(optionalOauthClient.get().clientId, optionalOauthClient.get().clientSecret)

        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add("grant_type", "refresh_token")
        map.add("refresh_token", payload["refresh_token"])

        val request = HttpEntity(map, headers)

        try {
            if (TenantContext.getCurrentTenant() == "public") {
                val oauth2Response = restTemplate.postForEntity(
                    System.getenv()["APPLICATION_BACKEND_URL"] + "/oauth2/token", request,
                    Oauth2Response::class.java
                )
                updateCookies(httpServletResponse, oauth2Response.body)
                return ResponseEntity.ok(oauth2Response.body)
            } else {
                val oauth2Response = restTemplate.postForEntity(
                    System.getenv()["HTTP_SCHEMA"] + tenantOptional.get().domain + "/oauth2/token",
                    request, Oauth2Response::class.java
                )
                updateCookies(httpServletResponse, oauth2Response.body)
                return ResponseEntity.ok(oauth2Response.body)
            }
        } catch (e: HttpStatusCodeException) {
            if (e.statusCode.is4xxClientError) {
                return ResponseEntity.status(401).body("{ message: \"Unauthorized Access\"}")
            } else if (e.statusCode.is5xxServerError) {
                return ResponseEntity.status(500).body("{ message: \"Server Error\"}")
            }
        } catch (e: RestClientException) {
        }
        return ResponseEntity.status(503).body("{ message: \"Identity Service Unavailable\"}")
    }

    @Throws(IOException::class)
    fun logout(httpServletResponse: HttpServletResponse) {
        if (TenantContext.getCurrentTenant() == "public") {
            httpServletResponse.sendRedirect(System.getenv()["APPLICATION_BACKEND_URL"] + "/app/login")
        } else {
            val tenantOptional = tenantRepository.findOneByCompanyKey(TenantContext.getCurrentTenant()!!)
            httpServletResponse.sendRedirect(
                System.getenv()["HTTP_SCHEMA"] + tenantOptional.get().domain + "/app/login"
            )
        }
    }

    private fun updateCookies(httpServletResponse: HttpServletResponse, oauth2Response: Oauth2Response) {
        val cookieActionsProvider = CookieActionsProvider()
        cookieActionsProvider.updateFunction = CookieActionsProvider.updateCookieFunc(httpServletResponse)
        cookieActionsProvider
            .updateCookie(CookieActionsProvider.CookieValue("access_token", oauth2Response.access_token))
        cookieActionsProvider.updateCookie(
            CookieActionsProvider.CookieValue("refresh_token", oauth2Response.refresh_token)
        )
        cookieActionsProvider
            .updateCookie(CookieActionsProvider.CookieValue("token_type", oauth2Response.token_type))
        cookieActionsProvider.updateCookie(
            CookieActionsProvider.CookieValue("expires_in", oauth2Response.expires_in.toString())
        )
    }

    private fun checkDeviceId(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse) {
        val cookies = httpServletRequest.cookies
        var deviceId = Optional.empty<Cookie>()
        if (Objects.nonNull(cookies)) {
            deviceId = Arrays.stream(cookies).filter { cookie: Cookie -> cookie.name == "deviceId" }
                .findFirst()
        }
        if (!deviceId.isPresent) {
            val cookieActionsProvider = CookieActionsProvider()
            cookieActionsProvider.updateFunction = CookieActionsProvider.updateCookieFunc(httpServletResponse)
            cookieActionsProvider
                .updateCookie(CookieActionsProvider.CookieValue("deviceId", UUID.randomUUID().toString()))
        }
    }

    private fun setOtpParam(model: Model, session: HttpSession, name: String) {
        if (Objects.nonNull(session.getAttribute(name))) {
            model.addAttribute(name, session.getAttribute(name))
            session.removeAttribute(name)
        }
    }

    class ScopeWithDescription internal constructor(val scope: String) {
        val description: String = scopeDescriptions.getOrDefault(scope, DEFAULT_DESCRIPTION)

        companion object {
            private const val DEFAULT_DESCRIPTION =
                "UNKNOWN SCOPE - We cannot provide information about this permission, use caution when granting this."
            private val scopeDescriptions: MutableMap<String, String> = HashMap()

            init {
                scopeDescriptions[OidcScopes.PROFILE] =
                    "This application will be able to read your profile information."
                scopeDescriptions["tenant:write"] = "Write tenant information"
                scopeDescriptions["tenant:read"] = "Read tenant information"
                scopeDescriptions["trust"] = "Trust the client"
            }
        }
    }

    companion object {
        private fun withDescription(scopes: Set<String>): Set<ScopeWithDescription> {
            val scopeWithDescriptions: MutableSet<ScopeWithDescription> = HashSet()
            for (scope in scopes) {
                scopeWithDescriptions.add(ScopeWithDescription(scope))
            }
            return scopeWithDescriptions
        }
    }
}