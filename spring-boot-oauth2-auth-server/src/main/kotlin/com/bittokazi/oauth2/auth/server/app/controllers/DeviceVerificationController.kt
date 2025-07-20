package com.bittokazi.oauth2.auth.server.app.controllers

import com.bittokazi.oauth2.auth.server.app.services.login.DeviceVerificationService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/device-verification")
class DeviceVerificationController(
    private val deviceVerificationService: DeviceVerificationService
) {

    @GetMapping
    fun showVerificationPage(
        @RequestParam("user_code") userCode: String?,
        model: Model,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any = deviceVerificationService.getDeviceVerificationPage(
        userCode = userCode,
        model = model,
        httpServletRequest = httpServletRequest,
        httpServletResponse = httpServletResponse
    )

    @PostMapping()
    fun verifyUserCode(
        @RequestParam("user_code") userCode: String?,
        model: Model,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any = deviceVerificationService.authorizeDevice(
        userCode = userCode,
        model = model,
        httpServletRequest = httpServletRequest,
        httpServletResponse = httpServletResponse
    )
}
