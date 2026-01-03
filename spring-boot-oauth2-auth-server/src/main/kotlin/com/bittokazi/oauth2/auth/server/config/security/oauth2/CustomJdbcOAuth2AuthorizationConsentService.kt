package com.bittokazi.oauth2.auth.server.config.security.oauth2

import com.bittokazi.oauth2.auth.server.config.TenantContext
import com.bittokazi.oauth2.auth.server.database.MultiTenantConnectionProviderImpl
import jakarta.transaction.Transactional
import org.springframework.dao.DataRetrievalFailureException
import org.springframework.jdbc.core.*
import org.springframework.lang.Nullable
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.util.Assert
import org.springframework.util.StringUtils
import java.sql.ResultSet
import java.sql.SQLException
import java.util.function.Function

@Transactional
open class CustomJdbcOAuth2AuthorizationConsentService(
    jdbcOperations: JdbcOperations,
    registeredClientRepository: RegisteredClientRepository,
    multiTenantConnectionProviderImpl: MultiTenantConnectionProviderImpl
) : OAuth2AuthorizationConsentService {
    private val jdbcOperations: JdbcOperations
    private var authorizationConsentRowMapper: RowMapper<OAuth2AuthorizationConsent>
    private var authorizationConsentParametersMapper: Function<OAuth2AuthorizationConsent, MutableList<SqlParameterValue>>

    private val multiTenantConnectionProviderImpl: MultiTenantConnectionProviderImpl

    init {
        Assert.notNull(jdbcOperations, "jdbcOperations cannot be null")
        Assert.notNull(registeredClientRepository, "registeredClientRepository cannot be null")
        this.jdbcOperations = jdbcOperations
        this.authorizationConsentRowMapper = OAuth2AuthorizationConsentRowMapper(registeredClientRepository)
        this.authorizationConsentParametersMapper = OAuth2AuthorizationConsentParametersMapper()
        this.multiTenantConnectionProviderImpl = multiTenantConnectionProviderImpl
    }

    private val tenantId: String
        get() = TenantContext.getCurrentTenant()!!

    override fun save(authorizationConsent: OAuth2AuthorizationConsent) {
        Assert.notNull(authorizationConsent, "authorizationConsent cannot be null")
        val existingAuthorizationConsent =
            this.findById(authorizationConsent.registeredClientId, authorizationConsent.principalName)
        if (existingAuthorizationConsent == null) {
            this.insertAuthorizationConsent(authorizationConsent)
        } else {
            this.updateAuthorizationConsent(authorizationConsent)
        }
    }

    private fun updateAuthorizationConsent(authorizationConsent: OAuth2AuthorizationConsent) {
        val tenantId = tenantId
        val jdbcOperations: JdbcOperations = JdbcTemplate(multiTenantConnectionProviderImpl.getDataSource(tenantId))

        val parameters: MutableList<SqlParameterValue> =
            authorizationConsentParametersMapper.apply(authorizationConsent)
        parameters.add(parameters.removeAt(0))
        parameters.add(parameters.removeAt(0))
        val pss: PreparedStatementSetter = ArgumentPreparedStatementSetter(parameters.toTypedArray())
        jdbcOperations.update(
            "UPDATE oauth2_authorization_consent SET authorities = ? WHERE registered_client_id = ? AND principal_name = ?",
            pss
        )
    }

    private fun insertAuthorizationConsent(authorizationConsent: OAuth2AuthorizationConsent) {
        val tenantId = tenantId
        val jdbcOperations: JdbcOperations = JdbcTemplate(multiTenantConnectionProviderImpl.getDataSource(tenantId))

        val pss: PreparedStatementSetter = ArgumentPreparedStatementSetter(
            authorizationConsentParametersMapper.apply(authorizationConsent).toTypedArray()
        )
        jdbcOperations.update(
            "INSERT INTO oauth2_authorization_consent (registered_client_id, principal_name, authorities) VALUES (?, ?, ?)",
            pss
        )
    }

    override fun remove(authorizationConsent: OAuth2AuthorizationConsent) {
        val tenantId = tenantId
        val jdbcOperations: JdbcOperations = JdbcTemplate(multiTenantConnectionProviderImpl.getDataSource(tenantId))

        Assert.notNull(authorizationConsent, "authorizationConsent cannot be null")
        val parameters = arrayOf(
            SqlParameterValue(12, authorizationConsent.registeredClientId),
            SqlParameterValue(12, authorizationConsent.principalName)
        )
        val pss: PreparedStatementSetter = ArgumentPreparedStatementSetter(parameters)
        jdbcOperations.update(
            "DELETE FROM oauth2_authorization_consent WHERE registered_client_id = ? AND principal_name = ?",
            pss
        )
    }

    @Nullable
    override fun findById(registeredClientId: String, principalName: String): OAuth2AuthorizationConsent? {
        val tenantId = tenantId
        val jdbcOperations: JdbcOperations = JdbcTemplate(multiTenantConnectionProviderImpl.getDataSource(tenantId))

        Assert.hasText(registeredClientId, "registeredClientId cannot be empty")
        Assert.hasText(principalName, "principalName cannot be empty")
        val parameters = arrayOf(SqlParameterValue(12, registeredClientId), SqlParameterValue(12, principalName))
        val pss: PreparedStatementSetter = ArgumentPreparedStatementSetter(parameters)
        val result = jdbcOperations.query(
            "SELECT registered_client_id, principal_name, authorities FROM oauth2_authorization_consent WHERE registered_client_id = ? AND principal_name = ?",
            pss,
            this.authorizationConsentRowMapper
        )
        return if (!result.isEmpty()) (result[0] as OAuth2AuthorizationConsent) else null
    }

    fun setAuthorizationConsentRowMapper(authorizationConsentRowMapper: RowMapper<OAuth2AuthorizationConsent>) {
        Assert.notNull(authorizationConsentRowMapper, "authorizationConsentRowMapper cannot be null")
        this.authorizationConsentRowMapper = authorizationConsentRowMapper
    }

    fun setAuthorizationConsentParametersMapper(authorizationConsentParametersMapper: Function<OAuth2AuthorizationConsent, MutableList<SqlParameterValue>>) {
        Assert.notNull(authorizationConsentParametersMapper, "authorizationConsentParametersMapper cannot be null")
        this.authorizationConsentParametersMapper = authorizationConsentParametersMapper
    }

    protected fun getJdbcOperations(): JdbcOperations {
        val tenantId = tenantId
        val jdbcOperations: JdbcOperations = JdbcTemplate(multiTenantConnectionProviderImpl.getDataSource(tenantId))

        return jdbcOperations
    }

    protected fun getAuthorizationConsentRowMapper(): RowMapper<OAuth2AuthorizationConsent> {
        return this.authorizationConsentRowMapper
    }

    protected fun getAuthorizationConsentParametersMapper(): Function<OAuth2AuthorizationConsent, MutableList<SqlParameterValue>> {
        return this.authorizationConsentParametersMapper
    }

    class OAuth2AuthorizationConsentRowMapper(registeredClientRepository: RegisteredClientRepository) :
        RowMapper<OAuth2AuthorizationConsent> {
        protected val registeredClientRepository: RegisteredClientRepository

        init {
            Assert.notNull(registeredClientRepository, "registeredClientRepository cannot be null")
            this.registeredClientRepository = registeredClientRepository
        }

        @Throws(SQLException::class)
        override fun mapRow(rs: ResultSet, rowNum: Int): OAuth2AuthorizationConsent {
            val registeredClientId = rs.getString("registered_client_id")
            val registeredClient = registeredClientRepository.findById(registeredClientId)
            if (registeredClient == null) {
                throw DataRetrievalFailureException("The RegisteredClient with id '$registeredClientId' was not found in the RegisteredClientRepository.")
            } else {
                val principalName = rs.getString("principal_name")
                val builder = OAuth2AuthorizationConsent.withId(registeredClientId, principalName)
                val authorizationConsentAuthorities = rs.getString("authorities")
                if (authorizationConsentAuthorities != null) {
                    val var8: Iterator<*> =
                        StringUtils.commaDelimitedListToSet(authorizationConsentAuthorities).iterator()

                    while (var8.hasNext()) {
                        val authority = var8.next() as String
                        builder.authority(SimpleGrantedAuthority(authority))
                    }
                }

                return builder.build()
            }
        }
    }

    class OAuth2AuthorizationConsentParametersMapper : Function<OAuth2AuthorizationConsent, MutableList<SqlParameterValue>> {
        override fun apply(authorizationConsent: OAuth2AuthorizationConsent): MutableList<SqlParameterValue> {
            val parameters: MutableList<SqlParameterValue> = mutableListOf()
            parameters.add(SqlParameterValue(12, authorizationConsent.registeredClientId))
            parameters.add(SqlParameterValue(12, authorizationConsent.principalName))
            val authorities: MutableSet<String?> = mutableSetOf()
            val var4: Iterator<*> = authorizationConsent.authorities.iterator()

            while (var4.hasNext()) {
                val authority = var4.next() as GrantedAuthority
                authorities.add(authority.authority)
            }

            parameters.add(SqlParameterValue(12, StringUtils.collectionToDelimitedString(authorities, ",")))
            return parameters
        }
    }

    companion object {
        private const val COLUMN_NAMES = "registered_client_id, principal_name, authorities"
        private const val TABLE_NAME = "oauth2_authorization_consent"
        private const val PK_FILTER = "registered_client_id = ? AND principal_name = ?"
        private const val LOAD_AUTHORIZATION_CONSENT_SQL =
            "SELECT registered_client_id, principal_name, authorities FROM oauth2_authorization_consent WHERE registered_client_id = ? AND principal_name = ?"
        private const val SAVE_AUTHORIZATION_CONSENT_SQL =
            "INSERT INTO oauth2_authorization_consent (registered_client_id, principal_name, authorities) VALUES (?, ?, ?)"
        private const val UPDATE_AUTHORIZATION_CONSENT_SQL =
            "UPDATE oauth2_authorization_consent SET authorities = ? WHERE registered_client_id = ? AND principal_name = ?"
        private const val REMOVE_AUTHORIZATION_CONSENT_SQL =
            "DELETE FROM oauth2_authorization_consent WHERE registered_client_id = ? AND principal_name = ?"
    }
}

