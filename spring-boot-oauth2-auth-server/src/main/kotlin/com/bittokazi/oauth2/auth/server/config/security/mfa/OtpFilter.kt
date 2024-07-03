package com.bittokazi.oauth2.auth.server.config.security.mfa

import com.bittokazi.oauth2.auth.server.app.repositories.tenant.UserRepository
import com.bittokazi.oauth2.auth.server.app.services.mfa.TwoFaService
import com.bittokazi.oauth2.auth.server.config.TenantContext
import com.bittokazi.oauth2.auth.server.utils.HttpReqRespUtils
import com.bittokazi.oauth2.auth.server.utils.logger
import jakarta.servlet.*
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import lombok.extern.slf4j.Slf4j
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.*

@Component
class OtpFilter(
    private val userRepository: UserRepository,
    private val twoFaService: TwoFaService
) : Filter {

    val logger = logger()

    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {
        logger.info("OtpFilter ➡\uFE0F Init")
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
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
                                chain.doFilter(request, response)
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
        chain.doFilter(request, response)
    }

    override fun destroy() {
    }
}
