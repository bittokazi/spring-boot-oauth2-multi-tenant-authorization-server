package com.bittokazi.oauth2.auth.server.app.repositories.master

import com.bittokazi.oauth2.auth.server.app.models.master.Tenant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TenantRepository : JpaRepository<Tenant, String> {
    fun findOneByCompanyKey(companyKey: String): Optional<Tenant>

    fun findOneByLogo(logo: String): Optional<Tenant>

    fun findOneByDomain(domain: String): Optional<Tenant>
}
