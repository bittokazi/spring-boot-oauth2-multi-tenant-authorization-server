package com.bittokazi.oauth2.auth.server.config.security

import com.bittokazi.oauth2.auth.server.app.repositories.tenant.UserRepository
import com.bittokazi.oauth2.auth.server.app.services.mfa.TwoFaService
import com.bittokazi.oauth2.auth.server.config.TenantContext
import com.bittokazi.oauth2.auth.server.utils.HttpReqRespUtils
import jakarta.servlet.ServletException
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.security.web.savedrequest.HttpSessionRequestCache
import org.springframework.security.web.savedrequest.RequestCache
import org.springframework.util.StringUtils
import java.io.IOException
import java.util.*

class LoginSuccessHandler(
    private val userRepository: UserRepository,
    private val twoFaService: TwoFaService
) : SimpleUrlAuthenticationSuccessHandler() {
    protected val logger: Log = LogFactory.getLog(this.javaClass)

    private var requestCache: RequestCache = HttpSessionRequestCache()

    @Throws(ServletException::class, IOException::class)
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {

        val httpServletRequest = request as HttpServletRequest
        val httpServletResponse = response as HttpServletResponse
        val initial = SecurityContextHolder.getContext().authentication
        val session: HttpSession = httpServletRequest.getSession(true)

        if (initial?.isAuthenticated == true && session.getAttribute("otp") == null) {
            val user = userRepository
                .findOneByUsernameIgnoreCase(initial.name)
            if (user.isPresent) {
                val cookies = httpServletRequest.cookies
                val deviceId = Arrays.stream(cookies).filter { cookie: Cookie -> cookie.name == "deviceId" }
                    .findFirst()
                if (deviceId.isPresent) {
                    if (Objects.nonNull(user.get().twoFaEnabled) &&
                        user.get().twoFaEnabled == true &&
                        !twoFaService.isTrustedDevice(deviceId.get().value, user.get())
                    ) {
                        if (httpServletRequest.parameterMap.containsKey("otp-code")) {
                            var code: Int? = null
                            try {
                                code = httpServletRequest.getParameter("otp-code").toString().toInt()
                            } catch (e: Exception) {
                            }
                            if ((code != null &&
                                        twoFaService.validate2FA(code, initial.name, httpServletResponse))
                                || twoFaService.validate2FAScratchCode(
                                    httpServletRequest.getParameter("otp-code"),
                                    initial.name
                                )
                            ) {
                                if (httpServletRequest.parameterMap.containsKey("trust-device")) {
                                    twoFaService.saveTrustedDevice(
                                        deviceId.get().value, user.get(),
                                        HttpReqRespUtils.getUserAgent(httpServletRequest), HttpReqRespUtils
                                            .getClientIpAddressIfServletRequestExist(httpServletRequest)
                                    )
                                }
                                session.setAttribute("otp", true);
                                checks(request, response, authentication)
                                return
                            } else {
                                session.setAttribute("otpRequired", true)
                                session.setAttribute("message", "Invalid OTP")
                                session.setAttribute("clientId", TenantContext.getCurrentClient())
                                httpServletResponse.sendRedirect(httpServletRequest.contextPath + "/otp-login")
                                return
                            }
                        }
                        if(httpServletRequest.servletPath != "/otp-login" &&
                            !httpServletRequest.servletPath.contains("/assets") &&
                            !httpServletRequest.servletPath.contains("/tenant-assets")) {
                            session.setAttribute("otpRequired", true)
                            session.setAttribute("message", "Please Enter OTP")
                            session.setAttribute("clientId", TenantContext.getCurrentClient())
                            httpServletResponse.sendRedirect(httpServletRequest.contextPath + "/otp-login")
                            return
                        }
                    } else {
                        session.setAttribute("otp", true);
                    }
                }
            }
        }

    }

    fun checks(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val savedRequest = requestCache.getRequest(request, response)
        if (savedRequest == null) {
            super.onAuthenticationSuccess(request, response, authentication)
            return
        }
        val targetUrlParameter = targetUrlParameter
        if (isAlwaysUseDefaultTargetUrl
            || (targetUrlParameter != null && StringUtils.hasText(request.getParameter(targetUrlParameter)))
        ) {
            requestCache.removeRequest(request, response)
            super.onAuthenticationSuccess(request, response, authentication)
            return
        }
        clearAuthenticationAttributes(request)
        // Use the DefaultSavedRequest URL
        val targetUrl = savedRequest.redirectUrl
        redirectStrategy.sendRedirect(request, response, targetUrl)
        return
    }

    fun setRequestCache(requestCache: RequestCache) {
        this.requestCache = requestCache
    }
}
