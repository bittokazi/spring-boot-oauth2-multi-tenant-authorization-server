package com.bittokazi.oauth2.auth.server.app.controllers.role

import com.bittokazi.oauth2.auth.server.app.models.tenant.Role
import com.bittokazi.oauth2.auth.server.app.services.role.RoleService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * @author Bitto Kazi
 */
@RestController
@RequestMapping("/api")
class RoleController(
    private val roleService: RoleService
): RoleControllerApi {

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('SCOPE_role:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    override fun getRoles(
        @RequestParam(value = "page", required = false, defaultValue = "0") page: Int,
        @RequestParam(value = "count", required = false, defaultValue = "10") count: Int
    ): Any = roleService.getRoles(page, count)

    @GetMapping("/roles/all")
    @PreAuthorize("hasAuthority('SCOPE_role:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    override fun allRoles(): Any = roleService.allRoles()

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('SCOPE_role:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    override fun addRole(
        @RequestBody role: Role,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any = roleService.addRole(role, httpServletRequest, httpServletResponse)

    @GetMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('SCOPE_role:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    override fun getRole(
        @PathVariable id: String
    ): Any = roleService.getRole(id)

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('SCOPE_role:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    override fun updateRole(
        @PathVariable id: String,
        @RequestBody role: Role,
        response: HttpServletResponse
    ): Any = roleService.updateRole(role, id, response)

    @PostMapping("/roles/search/name")
    @PreAuthorize("hasAuthority('SCOPE_role:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    override fun getRoleByName(
        @RequestBody role: Role,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any = roleService.getRoleByName(role)
}
