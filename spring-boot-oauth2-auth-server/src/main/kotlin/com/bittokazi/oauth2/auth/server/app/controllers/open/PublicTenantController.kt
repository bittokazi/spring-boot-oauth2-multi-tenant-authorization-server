package com.bittokazi.oauth2.auth.server.app.controllers.open

import com.bittokazi.oauth2.auth.server.app.services.tenant.TenantService
import io.swagger.v3.oas.annotations.Hidden
import lombok.RequiredArgsConstructor
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Hidden
@RestController
@RequestMapping("/public/api")
@RequiredArgsConstructor
class PublicTenantController(
    private val tenantService: TenantService
) {

    @GetMapping("/tenants/info")
    fun info(): ResponseEntity<*> = tenantService.info()
}
