package com.bittokazi.oauth2.auth.server.app.services.role.helpers

import com.bittokazi.oauth2.auth.server.app.repositories.tenant.RoleRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

object RoleHelpers {
    fun getRoles(page: Int, count: Int, roleRepository: RoleRepository): Any {
        val json: MutableMap<String, Any> = HashMap()
        val reqCount: Pageable = PageRequest.of(page, count, Sort.by(Sort.Direction.DESC, "id"))
        val pages = roleRepository.findAll(reqCount)
        json["roles"] = pages.content
        json["pages"] = pages.totalPages
        json["records"] = pages.totalElements
        return json
    }

    fun getAllRoles(roleRepository: RoleRepository): Any {
        val json: MutableMap<String, Any> = HashMap()
        val roles = roleRepository.findAll()
        json["roles"] = roles
        json["pages"] = 1
        json["records"] = roles.size
        return json
    }
}
