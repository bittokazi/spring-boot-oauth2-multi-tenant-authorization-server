package com.bittokazi.oauth2.auth.server.config.security.mfa

import com.bittokazi.oauth2.auth.server.app.repositories.tenant.UserRepository
import com.bittokazi.oauth2.auth.server.app.services.mfa.TwoFaService
import com.bittokazi.oauth2.auth.server.utils.HttpReqRespUtils
import com.bittokazi.oauth2.auth.server.utils.logger
import jakarta.servlet.*
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.security.web.savedrequest.HttpSessionRequestCache
import org.springframework.security.web.savedrequest.RequestCache
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import java.io.IOException
import java.util.*

@Component
class OtpFilter(
    private val userRepository: UserRepository,
    private val twoFaService: TwoFaService
) : SimpleUrlAuthenticationSuccessHandler(), Filter {

    val logger = logger()

    private var requestCache: RequestCache = HttpSessionRequestCache()

    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {
        logger.info("OtpFilter ➡\uFE0F Init")
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpServletRequest = request as HttpServletRequest
        val httpServletResponse = response as HttpServletResponse
        if (request.userPrincipal != null
            && request.session.getAttribute("otp") !=null
            && (request.session.getAttribute("otp") as Boolean)
            && !httpServletRequest.servletPath.contains("/public/api")
            && !httpServletRequest.servletPath.contains("/assets")
            && !httpServletRequest.servletPath.contains("/template-assets")) {
            val user = userRepository
                .findOneByUsernameIgnoreCase(request.userPrincipal.name)
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
                                    twoFaService.validate2FA(code, user.get().username!!, httpServletResponse))
                            || twoFaService.validate2FAScratchCode(
                                httpServletRequest.getParameter("otp-code"),
                                user.get().username!!
                            )
                        ) {
                            httpServletRequest.session.removeAttribute("otp")
                            clearAuthenticationAttributes(request)
                            if (httpServletRequest.parameterMap.containsKey("trust-device")) {
                                twoFaService.saveTrustedDevice(
                                    deviceId.get().value, user.get(),
                                    HttpReqRespUtils.getUserAgent(httpServletRequest), HttpReqRespUtils
                                        .getClientIpAddressIfServletRequestExist(httpServletRequest)
                                )
                            }

                            deviceAuthorizationRedirectCheck(httpServletRequest, httpServletResponse)

                            val savedRequest = requestCache.getRequest(request, response)
                            if (savedRequest == null) {
                                super.onAuthenticationSuccess(request, response, SecurityContextHolder.getContext().authentication!!)
                                return
                            }
                            val targetUrlParameter = targetUrlParameter
                            if (isAlwaysUseDefaultTargetUrl
                                || (targetUrlParameter != null && StringUtils.hasText(request.getParameter(targetUrlParameter)))
                            ) {
                                requestCache.removeRequest(request, response)
                                super.onAuthenticationSuccess(request, response, SecurityContextHolder.getContext().authentication!!)
                                return
                            }
                            // Use the DefaultSavedRequest URL
                            val targetUrl = savedRequest.redirectUrl
                            redirectStrategy.sendRedirect(request, response, targetUrl)
                            return
                        } else {
                            val session = httpServletRequest.getSession(true)
                            session.setAttribute(
                                "otpRequiredTrustDevice",
                                httpServletRequest.getParameter("trust-device")
                            )
                            session.setAttribute("otpRequired", true)
                            session.setAttribute("message", "Invalid OTP")
                        }
                    } else {
                        val session = httpServletRequest.getSession(true)
                        session.setAttribute(
                            "otpRequiredTrustDevice",
                            httpServletRequest.getParameter("trust-device")
                        )
                        session.setAttribute("otpRequired", true)
                        session.setAttribute("message", "Please Enter OTP")
                        if(!httpServletRequest.servletPath.contains("/otp-login")) {
                            httpServletResponse.sendRedirect(httpServletRequest.contextPath + "/otp-login")
                            return
                        }
                    }
                } else {
                    val session = httpServletRequest.getSession(true)
                    session.setAttribute("otpRequired", false)
                    httpServletRequest.session.removeAttribute("otp")
                    deviceAuthorizationRedirectCheck(httpServletRequest, httpServletResponse)
                }
            }
        }
        chain.doFilter(request, response)
    }

    fun deviceAuthorizationRedirectCheck(request: HttpServletRequest, response: HttpServletResponse) {
        val deviceRedirect = request.session.getAttribute("device_verification_redirect") as? Boolean
        if (deviceRedirect != null) {
            logger.debug("Device verification redirect found in session. Redirecting to $deviceRedirect")
            request.session.removeAttribute("device_verification_redirect")
            response.sendRedirect("/device-verification")
            return
        }
    }

    override fun destroy() {
    }
}
