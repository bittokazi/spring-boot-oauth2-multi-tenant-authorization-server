package com.bittokazi.oauth2.auth.server.config.interceptors

import com.bittokazi.oauth2.auth.server.app.models.master.Tenant
import com.bittokazi.oauth2.auth.server.app.repositories.master.TenantRepository
import com.bittokazi.oauth2.auth.server.config.AppConfig
import com.bittokazi.oauth2.auth.server.config.TenantContext.setCurrentDataTenant
import com.bittokazi.oauth2.auth.server.config.TenantContext.setCurrentIssuer
import com.bittokazi.oauth2.auth.server.config.TenantContext.setCurrentTenant
import jakarta.servlet.ServletRequestEvent
import jakarta.servlet.ServletRequestListener
import jakarta.servlet.http.HttpServletRequest
import java.util.*

class TenantContextListener(
    private val tenantRepository: TenantRepository
) : ServletRequestListener {

    override fun requestInitialized(servletRequestEvent: ServletRequestEvent) {
        val request = servletRequestEvent.servletRequest as HttpServletRequest
        val host = request.getHeader("host").replace("www.", "")
        var tenantOptional = Optional.empty<Tenant>()
        var dataTenantOptional = Optional.empty<Tenant>()
        var authTenantOptional = Optional.empty<Tenant>()
        if (Objects.nonNull(host)) {
            tenantOptional = tenantRepository.findOneByDomain(host)
        }
        if (!tenantOptional.isPresent) {
            val header = request.getHeader("X-DATA-TENANT")
            if (Objects.nonNull(header)) {
                dataTenantOptional = tenantRepository.findOneByCompanyKey(header)
            }
        }

        if (tenantOptional.isPresent) {
            setCurrentTenant(tenantOptional.get().companyKey)
            setCurrentDataTenant(tenantOptional.get().companyKey)
        } else {
            if (AppConfig.USE_X_AUTH_TENANT) {
                val header = request.getHeader("X-AUTH-TENANT")
                if (Objects.nonNull(header)) {
                    authTenantOptional = tenantRepository.findOneByCompanyKey(header)
                }
            }
            if (AppConfig.USE_X_AUTH_TENANT && authTenantOptional.isPresent) {
                setCurrentTenant(authTenantOptional.get().companyKey)
                setCurrentDataTenant(authTenantOptional.get().companyKey)
            } else {
                setCurrentTenant("public")
                if (dataTenantOptional.isPresent) {
                    setCurrentDataTenant(dataTenantOptional.get().companyKey)
                } else {
                    setCurrentDataTenant("public")
                }
            }
        }
        if (authTenantOptional.isPresent) {
            setCurrentIssuer(AppConfig.HTTP_SCHEMA + authTenantOptional.get().domain)
        } else {
            setCurrentIssuer(AppConfig.HTTP_SCHEMA + host)
        }
    }
}