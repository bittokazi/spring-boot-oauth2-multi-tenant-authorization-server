package com.bittokazi.oauth2.auth.server.app.services.role

import com.bittokazi.oauth2.auth.server.app.models.tenant.Role
import com.bittokazi.oauth2.auth.server.app.services.base.BaseService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity

interface RoleService : BaseService {
    fun getRoles(page: Int, count: Int): Any

    fun addRole(role: Role, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse): Any

    fun getRole(id: String): ResponseEntity<*>

	fun allRoles(): Any

    fun updateRole(modifiedRole: Role, id: String, response: HttpServletResponse): Any

    fun getRoleByName(role: Role): Any
}
