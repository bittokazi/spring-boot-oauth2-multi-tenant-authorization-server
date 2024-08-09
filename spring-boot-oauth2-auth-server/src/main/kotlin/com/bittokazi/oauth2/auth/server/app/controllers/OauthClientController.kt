package com.bittokazi.oauth2.auth.server.app.controllers

import com.bittokazi.oauth2.auth.server.app.models.tenant.OauthClient
import com.bittokazi.oauth2.auth.server.app.services.client.ClientService
import io.swagger.v3.oas.annotations.Hidden
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Hidden
@RestController
@RequestMapping("/api")
class OauthClientController(private val oauthService: ClientService) {
    
    @GetMapping("/clients")
    @PreAuthorize("hasAuthority('SCOPE_client:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    fun allOauthClients(): Any  = oauthService.allOauthClients()

    @PostMapping("/clients")
    @PreAuthorize("hasAuthority('SCOPE_client:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    fun addOauthClient(
        @RequestBody oauthClient: OauthClient,
        httpServletResponse: HttpServletResponse
    ): Any = oauthService.saveOauthClient(oauthClient, httpServletResponse)

    @GetMapping("/clients/{id}")
    @PreAuthorize("hasAuthority('SCOPE_client:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    fun getOauthClients(
        @PathVariable id: String,
        httpServletResponse: HttpServletResponse
    ): Any = oauthService.getOauthClient(id, httpServletResponse)

    @PutMapping("/clients/{id}")
    @PreAuthorize("hasAuthority('SCOPE_client:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    fun updateOauthClient(
        @RequestBody oauthClient: OauthClient,
        httpServletResponse: HttpServletResponse
    ): Any = oauthService.updateOauthClient(oauthClient, httpServletResponse)

    @DeleteMapping("/clients/{id}")
    @PreAuthorize("hasAuthority('SCOPE_client:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    fun deleteOauthClient(
        @PathVariable id: String,
        httpServletResponse: HttpServletResponse
    ): Any = oauthService.deleteOauthClient(id, httpServletResponse)
}
