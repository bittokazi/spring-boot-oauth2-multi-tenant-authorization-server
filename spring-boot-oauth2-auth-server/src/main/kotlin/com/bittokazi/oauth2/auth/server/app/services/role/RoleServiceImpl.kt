package com.bittokazi.oauth2.auth.server.app.services.role

import com.bittokazi.oauth2.auth.server.app.models.tenant.Role
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.RoleRepository
import com.bittokazi.oauth2.auth.server.app.services.base.RestResponseGenerator
import com.bittokazi.oauth2.auth.server.app.services.role.helpers.RoleAddHelper
import com.bittokazi.oauth2.auth.server.app.services.role.helpers.RoleHelpers
import com.bittokazi.oauth2.auth.server.app.services.role.helpers.RoleUpdateHelper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

/**
 * @author Bitto Kazi
 */
@Service
class RoleServiceImpl(
    private val roleRepository: RoleRepository
) : RoleService {

    override fun getRoles(page: Int, count: Int): Any {
        return RoleHelpers.getRoles(page, count, roleRepository)
    }

    override fun addRole(
        role: Role,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): Any {
        var role = role
        val errors = RoleAddHelper.validateRole(role, roleRepository)
        if (errors.size > 0) {
            return RestResponseGenerator.inputError(httpServletResponse, errors)
        }
        role = roleRepository.save(role)
        return role
    }

    override fun updateRole(modifiedRole: Role, id: String, response: HttpServletResponse): Any {
        val roleOptional = roleRepository.findById(id)
        if (roleOptional.isPresent) {
            val role = roleOptional.get()
            val errors = RoleUpdateHelper.validate(role, modifiedRole, roleRepository)
            if (errors.size > 0) {
                return RestResponseGenerator.inputError(response, errors)
            }

            modifiedRole.id = role.id
            return roleRepository.save(modifiedRole)
        }
        return RestResponseGenerator.notFound(response)
    }

    override fun getRoleByName(role: Role): ResponseEntity<*> {
        val roleOptional = roleRepository.findOneByName(
            role.name
        )
        return when(roleOptional.isPresent) {
            true -> ResponseEntity.ok(roleOptional.get())
            false -> ResponseEntity.notFound().build<Any>()
        }
    }

    override fun getRole(id: String): ResponseEntity<*> {
        val roleOptional = roleRepository.findById(id)
        return when(roleOptional.isPresent) {
            true -> ResponseEntity.ok(roleOptional.get())
            false ->  ResponseEntity.notFound().build<Any>()
        }
    }

    override fun allRoles(): Any = RoleHelpers.getAllRoles(roleRepository)
}
