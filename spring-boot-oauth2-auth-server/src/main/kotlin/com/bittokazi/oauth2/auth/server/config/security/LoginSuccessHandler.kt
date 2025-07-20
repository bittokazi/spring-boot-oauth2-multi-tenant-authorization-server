package com.bittokazi.oauth2.auth.server.config.security

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.security.web.savedrequest.HttpSessionRequestCache
import org.springframework.security.web.savedrequest.RequestCache
import org.springframework.util.StringUtils
import java.io.IOException

class LoginSuccessHandler : SimpleUrlAuthenticationSuccessHandler() {
    protected val logger: Log = LogFactory.getLog(this.javaClass)

    private var requestCache: RequestCache = HttpSessionRequestCache()

    @Throws(ServletException::class, IOException::class)
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        if(request.session.getAttribute("otp") !=null  && request.session.getAttribute("otp") as Boolean) {
            response.sendRedirect(request.contextPath + "/otp-login")
            return
        }

        val deviceRedirect = request.session.getAttribute("device_verification_redirect") as? Boolean
        if (deviceRedirect != null) {
            logger.debug("Device verification redirect found in session. Redirecting to $deviceRedirect")
            request.session.removeAttribute("device_verification_redirect")
            response.sendRedirect("/device-verification")
            return
        }

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
    }

    fun setRequestCache(requestCache: RequestCache) {
        this.requestCache = requestCache
    }
}
