package com.bittokazi.oauth2.auth.server.config.security.mfa

import com.bittokazi.oauth2.auth.server.app.repositories.tenant.UserRepository
import com.bittokazi.oauth2.auth.server.app.services.mfa.TwoFaService
import com.bittokazi.oauth2.auth.server.utils.HttpReqRespUtils
import com.bittokazi.oauth2.auth.server.utils.logger
import jakarta.servlet.*
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.extern.slf4j.Slf4j
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
        if (request.getContentType() != null && httpServletRequest.servletPath == "/login") {
            val user = userRepository
                .findOneByUsernameIgnoreCase(httpServletRequest.getParameter("username"))
            if (user.isPresent) {
                val bCryptPasswordEncoder = BCryptPasswordEncoder()
                if (bCryptPasswordEncoder.matches(
                        httpServletRequest.getParameter("password"),
                        user.get().password
                    )
                ) {
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
                                if (code != null &&
                                    (twoFaService.validate2FA(code, httpServletRequest, httpServletResponse)
                                    || twoFaService.validate2FAScratchCode(
                                        httpServletRequest.getParameter("otp-code"),
                                        httpServletRequest
                                    ))
                                ) {
                                    if (httpServletRequest.parameterMap.containsKey("trust-device")) {
                                        twoFaService.saveTrustedDevice(
                                            deviceId.get().value, user.get(),
                                            HttpReqRespUtils.getUserAgent(httpServletRequest), HttpReqRespUtils
                                                .getClientIpAddressIfServletRequestExist(httpServletRequest)
                                        )
                                    }
                                    chain.doFilter(request, response)
                                    return
                                } else {
                                    val session = httpServletRequest.getSession(true)
                                    session.setAttribute(
                                        "otpRequiredUsername",
                                        httpServletRequest.getParameter("username")
                                    )
                                    session.setAttribute(
                                        "otpRequiredPassword",
                                        httpServletRequest.getParameter("password")
                                    )
                                    session.setAttribute(
                                        "otpRequiredRememberMe",
                                        httpServletRequest.getParameter("remember-me")
                                    )
                                    session.setAttribute(
                                        "otpRequiredTrustDevice",
                                        httpServletRequest.getParameter("trust-device")
                                    )
                                    session.setAttribute("otpRequired", true)
                                    session.setAttribute("message", "Invalid OTP")
                                    httpServletResponse.sendRedirect(httpServletRequest.contextPath + "/otp-login")
                                    return
                                }
                            }
                            val session = httpServletRequest.getSession(true)
                            session.setAttribute("otpRequiredUsername", httpServletRequest.getParameter("username"))
                            session.setAttribute("otpRequiredPassword", httpServletRequest.getParameter("password"))
                            session.setAttribute(
                                "otpRequiredRememberMe",
                                httpServletRequest.getParameter("remember-me")
                            )
                            session.setAttribute(
                                "otpRequiredTrustDevice",
                                httpServletRequest.getParameter("trust-device")
                            )
                            session.setAttribute("otpRequired", true)
                            session.setAttribute("message", "Please Enter OTP")
                            httpServletResponse.sendRedirect(httpServletRequest.contextPath + "/otp-login")
                            return
                        }
                    }
                }
            }
        }
        chain.doFilter(request, response)
    }

    override fun destroy() {
    }
}
