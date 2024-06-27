package com.bittokazi.oauth2.auth.server.database

import com.bittokazi.oauth2.auth.server.config.TenantContext.getCurrentDataTenant
import org.hibernate.context.spi.CurrentTenantIdentifierResolver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * CurrentTenantResolverImpl implements CurrentTenantIdentifierResolver of
 * hibernate to get information about current tenant from attribute.
 *
 * @author Bitto Kazi
 */
@Component
open class CurrentTenantResolverImpl : CurrentTenantIdentifierResolver<String> {
    private val logger: Logger = LoggerFactory.getLogger(CurrentTenantResolverImpl::class.java)

    /**
     * @return Current tenant information from attribute called
     * CURRENT_TENANT_IDENTIFIER. It is called in every request as it is
     * called in interceptor.
     */
    override fun resolveCurrentTenantIdentifier(): String {
        if (getCurrentDataTenant() != null) {
            return getCurrentDataTenant()!!
        }
        return "UNKNOWN_TENANT"
    }

    /**
     * Just an override method of CurrentTenantIdentifierResolver
     *
     * @return true allways.
     */
    override fun validateExistingCurrentSessions(): Boolean {
        return true
    }
}

