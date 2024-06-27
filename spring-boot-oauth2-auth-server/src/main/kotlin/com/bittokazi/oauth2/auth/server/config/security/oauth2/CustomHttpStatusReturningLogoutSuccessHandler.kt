package com.bittokazi.oauth2.auth.server.config.security.oauth2

import com.bittokazi.oauth2.auth.server.app.repositories.tenant.OauthClientRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class CustomHttpStatusReturningLogoutSuccessHandler(
    private val oauthClientRepository: OauthClientRepository
) : HttpStatusReturningLogoutSuccessHandler(
        HttpStatus.OK
    ) {

    @Throws(IOException::class)
    override fun onLogoutSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        response.status = HttpStatus.TEMPORARY_REDIRECT.value()
        val optionalOauthClient = oauthClientRepository.findOneByClientId(request.getParameter("id"))
        if (optionalOauthClient.isPresent) {
            response.sendRedirect(optionalOauthClient.get().postLogoutUrl)
        } else {
            response.sendRedirect("/app/login")
        }
        response.writer.flush()
    }
}
