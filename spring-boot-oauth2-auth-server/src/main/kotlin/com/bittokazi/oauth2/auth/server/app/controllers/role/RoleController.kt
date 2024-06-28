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

    @PreAuthorize("hasAuthority('SCOPE_role:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    @GetMapping("/roles")
    override fun getRoles(
        @RequestParam(value = "page", required = false, defaultValue = "0") page: Int,
        @RequestParam(value = "count", required = false, defaultValue = "10") count: Int
    ): Any = roleService.getRoles(page, count)

    @GetMapping("/roles/all")
    @PreAuthorize("hasAuthority('SCOPE_role:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    override fun allRoles(): Any = roleService.allRoles()

    @PreAuthorize("hasAuthority('SCOPE_role:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    @PostMapping("/roles")
    override fun addRole(
        @RequestBody role: Role,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any = roleService.addRole(role, httpServletRequest, httpServletResponse)

    @PreAuthorize("hasAuthority('SCOPE_role:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    @GetMapping("/roles/{id}")
    override fun getRole(
        @PathVariable id: String
    ): Any = roleService.getRole(id)

    @PreAuthorize("hasAuthority('SCOPE_role:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    @PutMapping("/roles/{id}")
    override fun updateRole(
        @PathVariable id: String,
        @RequestBody role: Role,
        response: HttpServletResponse
    ): Any = roleService.updateRole(role, id, response)

    @PreAuthorize("hasAuthority('SCOPE_role:all') or hasAuthority('SCOPE_SUPER_ADMIN')")
    @PostMapping("/roles/search/name")
    override fun getRoleByName(
        @RequestBody role: Role,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any = roleService.getRoleByName(role)
}
