package com.bittokazi.oauth2.auth.server.app.controllers

import com.bittokazi.oauth2.auth.server.app.services.login.LoginService
import io.swagger.v3.oas.annotations.Hidden
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.RequiredArgsConstructor
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import java.io.IOException
import java.security.Principal

@Hidden
@RestController
@RequiredArgsConstructor
class LoginController(
    private val loginService: LoginService
) {

    @GetMapping("/")
    @Throws(IOException::class)
    fun index(
        httpServletResponse: HttpServletResponse
    ) = httpServletResponse.sendRedirect("/login")

    @GetMapping("/login")
    @Throws(IOException::class)
    fun loginPage(
        model: Model,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Any = loginService.loginPage(model, request, response)

    @GetMapping("/otp-login")
    @Throws(IOException::class)
    fun otpLoginPage(
        model: Model,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Any = loginService.otpLoginPage(model, request, response)

    @GetMapping(value = ["/oauth2/consent"])
    fun consent(
        principal: Principal, model: Model,
        @RequestParam(OAuth2ParameterNames.CLIENT_ID) clientId: String,
        @RequestParam(OAuth2ParameterNames.SCOPE) scope: String,
        @RequestParam(OAuth2ParameterNames.STATE) state: String,
        @RequestParam(name = OAuth2ParameterNames.USER_CODE, required = false) userCode: String?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ModelAndView = loginService.consentPage(principal, model, clientId, scope, state, userCode, request, response)

    @GetMapping("/authorize_user")
    @Throws(IOException::class)
    fun authorizeUser(
        @RequestParam("code") code: String,
        httpServletResponse: HttpServletResponse
    ): ResponseEntity<*> = loginService.authorizeUser(code, httpServletResponse)

    @GetMapping("/oauth2/")
    @Throws(IOException::class)
    fun authorizeUserDefault(
        @RequestParam("code") code: String,
        httpServletResponse: HttpServletResponse
    ): ResponseEntity<*> = loginService.authorizeUser(code, httpServletResponse)

    @GetMapping("/oauth2/login")
    @Throws(IOException::class)
    fun loginRedirect(
        httpServletResponse: HttpServletResponse
    ) = loginService.loginRedirect(httpServletResponse)

    @PostMapping("/oauth2/refresh/token")
    @Throws(IOException::class)
    fun refreshToken(
        @RequestBody payload: HashMap<String, String>, 
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): ResponseEntity<*> = loginService.refreshToken(payload, httpServletResponse)

    @GetMapping("/oauth2/logout/redirect")
    @Throws(IOException::class)
    fun logout(
        httpServletResponse: HttpServletResponse
    ) = loginService.logout(httpServletResponse)
}
