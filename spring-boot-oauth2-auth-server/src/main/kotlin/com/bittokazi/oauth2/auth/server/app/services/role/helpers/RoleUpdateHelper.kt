package com.bittokazi.oauth2.auth.server.app.services.role.helpers

import com.bittokazi.oauth2.auth.server.app.models.tenant.Role
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.RoleRepository

object RoleUpdateHelper {
    fun validate(role: Role, modifiedRole: Role, roleRepository: RoleRepository): Map<String, List<String>> {
        val errors: MutableMap<String, List<String>> = HashMap()
        val existingRoleName = role.name

        if (modifiedRole.name == null || modifiedRole.name!!.isEmpty()) {
            errors["name"] = mutableListOf("empty")
        }
        if (modifiedRole.title == null || modifiedRole.title!!.isEmpty()) {
            errors["title"] = mutableListOf("empty")
        }
        if (modifiedRole.name != existingRoleName && roleRepository.findOneByName(modifiedRole.name).isPresent) {
            errors["name"] = mutableListOf("exist")
        }
        return errors
    }
}

