package com.bittokazi.oauth2.auth.server.config.interceptors

import com.bittokazi.oauth2.auth.server.app.repositories.master.TenantRepository
import com.bittokazi.oauth2.auth.server.config.TenantContext.getCurrentTenant
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

/**
 *
 * @author Bitto Kazi
 */
@Component
class TenantIdentifierInterceptorAdapter(
    private val tenantRepository: TenantRepository,
    private val jwtDecoder: JwtDecoder
) : HandlerInterceptor {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Throws(Exception::class)
    override fun preHandle(req: HttpServletRequest, res: HttpServletResponse, handler: Any): Boolean {
        val token = req
            .getHeader("Authorization")
            .replace(
                "Bearer ",
                ""
            )
        return jwtDecoder.decode(token).claims["tenant"] == getCurrentTenant()
    }
}

