package com.bittokazi.oauth2.auth.server.config.security.oauth2

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.savedrequest.HttpSessionRequestCache
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class AuthorizationLoginRedirectFilter: OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (request.requestURI == "/oauth2/authorize") {
            val authentication = SecurityContextHolder.getContext().authentication

            val notAuthenticated =
                authentication == null ||
                        !authentication.isAuthenticated ||
                        authentication is AnonymousAuthenticationToken

            if (notAuthenticated) {
                HttpSessionRequestCache().saveRequest(request, response)
                response.sendRedirect("/login")
                return
            }
        }

        filterChain.doFilter(request, response)
    }
}
