package com.bittokazi.oauth2.auth.server.app.services.role.helpers

import com.bittokazi.oauth2.auth.server.app.models.tenant.Role
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.RoleRepository

object RoleAddHelper {
    fun validateRole(role: Role, roleRepository: RoleRepository): Map<String, List<String>> {
        val errors: MutableMap<String, List<String>> = HashMap()
        if (roleRepository.findOneByName(role.name).isPresent) {
            errors["name"] = mutableListOf("exist")
        }
        if (role.title == null || role.title!!.isEmpty()) {
            errors["title"] = mutableListOf("empty")
        }
        return errors
    }
}
