package com.bittokazi.oauth2.auth.server.config.security.oauth2

import com.bittokazi.oauth2.auth.server.config.TenantContext
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2Token
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.util.Assert
import java.util.*

class CustomOAuth2TokenValidator<T : OAuth2Token?>(
    private val tokenValidators: Collection<OAuth2TokenValidator<T>>
) : OAuth2TokenValidator<T> {

    init {
        Assert.notNull(tokenValidators, "tokenValidators cannot be null")
    }

    @SafeVarargs
    constructor(
        vararg tokenValidators: OAuth2TokenValidator<T>
    ) : this(mutableListOf(*tokenValidators))

    override fun validate(token: T): OAuth2TokenValidatorResult {
        val errors: MutableCollection<OAuth2Error> = mutableListOf()
        val var3: Iterator<*> = tokenValidators.iterator()

        while (var3.hasNext()) {
            val validator: OAuth2TokenValidator<T> = var3.next() as OAuth2TokenValidator<T>
            errors.addAll(validator.validate(token).errors)
        }

        println("T> " + TenantContext.getCurrentTenant() + " t>" + token)

        return OAuth2TokenValidatorResult.failure(errors)
    }
}

