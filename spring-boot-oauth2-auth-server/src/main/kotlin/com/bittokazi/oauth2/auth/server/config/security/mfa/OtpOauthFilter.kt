package com.bittokazi.oauth2.auth.server.config.security.mfa

import com.bittokazi.oauth2.auth.server.utils.logger
import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.web.savedrequest.HttpSessionRequestCache
import org.springframework.security.web.savedrequest.RequestCache
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.*

/**
 *
 * @author Bitto Kazi
 */
@Component
class OtpOauthFilter : Filter {

    val logger = logger()

    private var requestCache: RequestCache = HttpSessionRequestCache()

    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {
        logger.info("OtpOauth2Filter ➡\uFE0F Init")
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpServletRequest = request as HttpServletRequest
        val httpServletResponse = response as HttpServletResponse
        if (
            httpServletRequest.servletPath.contains("/oauth2/authorize") &&
            httpServletRequest.session.getAttribute("otp") !=null  &&
            httpServletRequest.session.getAttribute("otp") as Boolean
        ) {
            httpServletResponse.sendRedirect(httpServletRequest.contextPath + "/otp-login")
            return
        }
        chain.doFilter(request, response)
    }

    override fun destroy() {
    }
}