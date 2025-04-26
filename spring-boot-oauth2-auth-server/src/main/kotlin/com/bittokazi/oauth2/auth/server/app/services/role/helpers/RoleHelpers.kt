package com.bittokazi.oauth2.auth.server.app.services.role.helpers

import com.bittokazi.oauth2.auth.server.app.models.tenant.RoleList
import com.bittokazi.oauth2.auth.server.app.repositories.tenant.RoleRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

object RoleHelpers {
    fun getRoles(page: Int, count: Int, roleRepository: RoleRepository): Any {
        val reqCount: Pageable = PageRequest.of(page, count, Sort.by(Sort.Direction.DESC, "id"))
        val pages = roleRepository.findAll(reqCount)
        return RoleList(
            pages = pages.totalPages,
            records = pages.totalElements,
            roles = pages.content
        )
    }

    fun getAllRoles(roleRepository: RoleRepository): Any {
        val roles = roleRepository.findAll()
        return RoleList(
            pages = 1,
            records = roles.size.toLong(),
            roles = roles
        )
    }
}
