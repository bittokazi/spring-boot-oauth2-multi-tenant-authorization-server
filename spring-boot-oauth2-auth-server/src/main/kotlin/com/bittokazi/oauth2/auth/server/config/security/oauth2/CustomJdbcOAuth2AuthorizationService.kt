package com.bittokazi.oauth2.auth.server.config.security.oauth2

import com.bittokazi.oauth2.auth.server.app.models.tenant.security.RoleOauth
import com.bittokazi.oauth2.auth.server.app.models.tenant.security.UserOauth
import com.bittokazi.oauth2.auth.server.config.TenantContext
import com.bittokazi.oauth2.auth.server.database.MultiTenantConnectionProviderImpl
import com.fasterxml.jackson.databind.Module
import jakarta.transaction.Transactional
import org.springframework.core.ParameterizedTypeReference
import org.springframework.dao.DataRetrievalFailureException
import org.springframework.jdbc.core.*
import org.springframework.jdbc.support.lob.DefaultLobHandler
import org.springframework.jdbc.support.lob.LobCreator
import org.springframework.jdbc.support.lob.LobHandler
import org.springframework.lang.Nullable
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.jackson.SecurityJacksonModules
import org.springframework.security.jackson2.SecurityJackson2Modules
import org.springframework.security.oauth2.core.*
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.server.authorization.*
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
//import org.springframework.security.oauth2.server.authorization.jackson.OAuth2AuthorizationServerJacksonModule
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module
import org.springframework.util.Assert
import org.springframework.util.CollectionUtils
import org.springframework.util.StringUtils
import tools.jackson.core.StreamReadFeature
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import java.lang.Deprecated
import java.nio.charset.StandardCharsets
import java.sql.*
import java.time.Instant
import java.util.*
import java.util.function.Consumer
import java.util.function.Function

@Transactional
open class CustomJdbcOAuth2AuthorizationService(
    jdbcOperations: JdbcOperations,
    registeredClientRepository: RegisteredClientRepository,
    lobHandler: LobHandler
) : OAuth2AuthorizationService {

    val TABLE_NAME: String = "oauth2_authorization"

    val SAVE_AUTHORIZATION_SQL: String = "INSERT INTO " + TABLE_NAME +
            " (" + COLUMN_NAMES + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private val jdbcOperations: JdbcOperations
    protected val lobHandler: LobHandler
    private var authorizationRowMapper: RowMapper<OAuth2Authorization>
    private var authorizationParametersMapper: Function<OAuth2Authorization, MutableList<SqlParameterValue>>

    private var multiTenantConnectionProviderImpl: MultiTenantConnectionProviderImpl? = null

    private var registeredClientRepository: RegisteredClientRepository? = null

    constructor(
        jdbcOperations: JdbcOperations,
        registeredClientRepository: RegisteredClientRepository,
        multiTenantConnectionProviderImpl: MultiTenantConnectionProviderImpl?
    ) : this(jdbcOperations, registeredClientRepository, DefaultLobHandler()) {
        this.multiTenantConnectionProviderImpl = multiTenantConnectionProviderImpl
        this.registeredClientRepository = registeredClientRepository
    }

    init {
        Assert.notNull(jdbcOperations, "jdbcOperations cannot be null")
        Assert.notNull(registeredClientRepository, "registeredClientRepository cannot be null")
        Assert.notNull(lobHandler, "lobHandler cannot be null")
        this.jdbcOperations = jdbcOperations
        this.lobHandler = lobHandler
        val authorizationRowMapper = OAuth2AuthorizationRowMapper(registeredClientRepository)
        authorizationRowMapper.setLobHandler(lobHandler)
        this.authorizationRowMapper = authorizationRowMapper
        this.authorizationParametersMapper = OAuth2AuthorizationParametersMapper()
        initColumnMetadata(jdbcOperations)
    }

    private val tenantId: String
        get() = TenantContext.getCurrentTenant()!!

    override fun save(authorization: OAuth2Authorization) {
        Assert.notNull(authorization, "authorization cannot be null")
        val existingAuthorization = this.findById(authorization.id)
        if (existingAuthorization == null) {
            this.insertAuthorization(authorization)
        } else {
            this.updateAuthorization(authorization)
        }
    }

    private fun updateAuthorization(authorization: OAuth2Authorization) {
        val tenantId = tenantId
        val jdbcOperations: JdbcOperations = JdbcTemplate(multiTenantConnectionProviderImpl!!.getDataSource(tenantId))


        val parameters: MutableList<SqlParameterValue> =
            authorizationParametersMapper.apply(authorization)
        parameters.add(parameters.removeAt(0))
        val lobCreator = lobHandler.lobCreator

        try {
            val pss: PreparedStatementSetter =
                LobCreatorArgumentPreparedStatementSetter(lobCreator, parameters.toTypedArray())
            jdbcOperations.update(
                "UPDATE oauth2_authorization SET registered_client_id = ?, principal_name = ?, authorization_grant_type = ?, authorized_scopes = ?, attributes = ?, state = ?, authorization_code_value = ?, authorization_code_issued_at = ?, authorization_code_expires_at = ?, authorization_code_metadata = ?, access_token_value = ?, access_token_issued_at = ?, access_token_expires_at = ?, access_token_metadata = ?, access_token_type = ?, access_token_scopes = ?, oidc_id_token_value = ?, oidc_id_token_issued_at = ?, oidc_id_token_expires_at = ?, oidc_id_token_metadata = ?, refresh_token_value = ?, refresh_token_issued_at = ?, refresh_token_expires_at = ?, refresh_token_metadata = ?, user_code_value = ?, user_code_issued_at = ?, user_code_expires_at = ?, user_code_metadata = ?, device_code_value = ?, device_code_issued_at = ?, device_code_expires_at = ?, device_code_metadata = ? WHERE id = ?",
                pss
            )
        } catch (var8: Throwable) {
            if (lobCreator != null) {
                try {
                    lobCreator.close()
                } catch (var7: Throwable) {
                    var8.addSuppressed(var7)
                }
            }

            throw var8
        }

        if (lobCreator != null) {
            lobCreator.close()
        }
    }

    private fun insertAuthorization(authorization: OAuth2Authorization) {
        val tenantId = tenantId
        val jdbcOperations: JdbcOperations = JdbcTemplate(multiTenantConnectionProviderImpl!!.getDataSource(tenantId))

        val parameters: MutableList<SqlParameterValue> = this.authorizationParametersMapper.apply(authorization)
        this.lobHandler.getLobCreator().use { lobCreator ->
            val pss: PreparedStatementSetter = LobCreatorArgumentPreparedStatementSetter(
                lobCreator,
                parameters.toTypedArray()
            )
            jdbcOperations.update(SAVE_AUTHORIZATION_SQL, pss)
        }
    }

    override fun remove(authorization: OAuth2Authorization) {
        val tenantId = tenantId
        val jdbcOperations: JdbcOperations = JdbcTemplate(multiTenantConnectionProviderImpl!!.getDataSource(tenantId))

        Assert.notNull(authorization, "authorization cannot be null")
        val parameters = arrayOf(SqlParameterValue(12, authorization.id))
        val pss: PreparedStatementSetter = ArgumentPreparedStatementSetter(parameters)
        jdbcOperations.update("DELETE FROM oauth2_authorization WHERE id = ?", pss)
    }

    @Nullable
    override fun findById(id: String): OAuth2Authorization? {
        Assert.hasText(id, "id cannot be empty")
        val parameters: MutableList<SqlParameterValue> = mutableListOf()
        parameters.add(SqlParameterValue(12, id))
        return this.findBy("id = ?", parameters)
    }

    @Nullable
    override fun findByToken(token: String, @Nullable tokenType: OAuth2TokenType?): OAuth2Authorization? {
        Assert.hasText(token, "token cannot be empty")
        val parameters: MutableList<SqlParameterValue> = mutableListOf()
        if (tokenType == null) {
            parameters.add(SqlParameterValue(12, token))
            parameters.add(mapToSqlParameter("authorization_code_value", token))
            parameters.add(mapToSqlParameter("access_token_value", token))
            parameters.add(mapToSqlParameter("oidc_id_token_value", token))
            parameters.add(mapToSqlParameter("refresh_token_value", token))
            parameters.add(mapToSqlParameter("user_code_value", token))
            parameters.add(mapToSqlParameter("device_code_value", token))
            return this.findBy(
                "state = ? OR authorization_code_value = ? OR access_token_value = ? OR oidc_id_token_value = ? OR refresh_token_value = ? OR user_code_value = ? OR device_code_value = ?",
                parameters
            )
        } else if ("state" == tokenType.value) {
            parameters.add(SqlParameterValue(12, token))
            return this.findBy("state = ?", parameters)
        } else if ("code" == tokenType.value) {
            parameters.add(mapToSqlParameter("authorization_code_value", token))
            return this.findBy("authorization_code_value = ?", parameters)
        } else if (OAuth2TokenType.ACCESS_TOKEN == tokenType) {
            parameters.add(mapToSqlParameter("access_token_value", token))
            return this.findBy("access_token_value = ?", parameters)
        } else if ("id_token" == tokenType.value) {
            parameters.add(mapToSqlParameter("oidc_id_token_value", token))
            return this.findBy("oidc_id_token_value = ?", parameters)
        } else if (OAuth2TokenType.REFRESH_TOKEN == tokenType) {
            parameters.add(mapToSqlParameter("refresh_token_value", token))
            return this.findBy("refresh_token_value = ?", parameters)
        } else if ("user_code" == tokenType.value) {
            parameters.add(mapToSqlParameter("user_code_value", token))
            return this.findBy("user_code_value = ?", parameters)
        } else if ("device_code" == tokenType.value) {
            parameters.add(mapToSqlParameter("device_code_value", token))
            return this.findBy("device_code_value = ?", parameters)
        } else {
            return null
        }
    }

    private fun findBy(filter: String, parameters: List<SqlParameterValue>): OAuth2Authorization? {
        val lobCreator = lobHandler.lobCreator

        val var6: OAuth2Authorization?
        try {
            val pss: PreparedStatementSetter =
                LobCreatorArgumentPreparedStatementSetter(lobCreator, parameters.toTypedArray())
            val result = getJdbcOperations().query(
                "SELECT id, registered_client_id, principal_name, authorization_grant_type, authorized_scopes, attributes, state, authorization_code_value, authorization_code_issued_at, authorization_code_expires_at,authorization_code_metadata,access_token_value,access_token_issued_at,access_token_expires_at,access_token_metadata,access_token_type,access_token_scopes,oidc_id_token_value,oidc_id_token_issued_at,oidc_id_token_expires_at,oidc_id_token_metadata,refresh_token_value,refresh_token_issued_at,refresh_token_expires_at,refresh_token_metadata,user_code_value,user_code_issued_at,user_code_expires_at,user_code_metadata,device_code_value,device_code_issued_at,device_code_expires_at,device_code_metadata FROM oauth2_authorization WHERE $filter",
                pss,
                this.getAuthorizationRowMapper()
            )
            var6 = if (!result.isEmpty()) (result[0] as OAuth2Authorization) else null
        } catch (var8: Throwable) {
            if (lobCreator != null) {
                try {
                    lobCreator.close()
                } catch (var7: Throwable) {
                    var8.addSuppressed(var7)
                }
            }

            throw var8
        }

        if (lobCreator != null) {
            lobCreator.close()
        }

        return var6
    }

    fun setAuthorizationRowMapper(authorizationRowMapper: RowMapper<OAuth2Authorization>) {
        Assert.notNull(authorizationRowMapper, "authorizationRowMapper cannot be null")
        this.authorizationRowMapper = authorizationRowMapper
    }

    fun setAuthorizationParametersMapper(authorizationParametersMapper: Function<OAuth2Authorization, MutableList<SqlParameterValue>>) {
        Assert.notNull(authorizationParametersMapper, "authorizationParametersMapper cannot be null")
        this.authorizationParametersMapper = authorizationParametersMapper
    }

    protected fun getJdbcOperations(): JdbcOperations {
        val tenantId = tenantId
        val jdbcOperations: JdbcOperations = JdbcTemplate(multiTenantConnectionProviderImpl!!.getDataSource(tenantId))
        return jdbcOperations
    }

    protected fun getAuthorizationRowMapper(): RowMapper<OAuth2Authorization> {
        return this.authorizationRowMapper
    }

    protected fun getAuthorizationParametersMapper(): Function<OAuth2Authorization, MutableList<SqlParameterValue>> {
        return this.authorizationParametersMapper
    }

    open class JsonMapperOAuth2AuthorizationRowMapper(
        registeredClientRepository: RegisteredClientRepository
    ): AbstractOAuth2AuthorizationRowMapper(registeredClientRepository) {
        private val jsonMapper: JsonMapper? = Jackson3.createJsonMapper()

        override fun readValue(data: String?): MutableMap<String?, Any?> {
            val typeReference: ParameterizedTypeReference<MutableMap<String?, Any?>> =
                object : ParameterizedTypeReference<MutableMap<String?, Any?>>() {
                }
            val javaType = this.jsonMapper!!.getTypeFactory()
                .constructType(typeReference.getType())
            return this.jsonMapper.readValue(data, javaType)
        }
    }

    open class OAuth2AuthorizationRowMapper(protected val registeredClientRepository: RegisteredClientRepository) :
        RowMapper<OAuth2Authorization> {
        private var lobHandler: LobHandler = DefaultLobHandler()
        private var objectMapper: com.fasterxml.jackson.databind.ObjectMapper = Jackson2.createObjectMapper()

        @Transactional
        @Throws(SQLException::class)
        override fun mapRow(rs: ResultSet, rowNum: Int): OAuth2Authorization {
            val registeredClientId = rs.getString("registered_client_id")
            val registeredClient = registeredClientRepository.findById(registeredClientId)
            if (registeredClient == null) {
                throw DataRetrievalFailureException("The RegisteredClient with id '$registeredClientId' was not found in the RegisteredClientRepository.")
            } else {
                val builder = OAuth2Authorization.withRegisteredClient(registeredClient)
                val id = rs.getString("id")
                val principalName = rs.getString("principal_name")
                val authorizationGrantType = rs.getString("authorization_grant_type")
                var authorizedScopes: Set<String?>? = emptySet<String>()
                val authorizedScopesString = rs.getString("authorized_scopes")
                if (authorizedScopesString != null) {
                    authorizedScopes = StringUtils.commaDelimitedListToSet(authorizedScopesString)
                }

                val attributes = this.parseMap(this.getLobValue(rs, "attributes"))
                builder.id(id).principalName(principalName)
                    .authorizationGrantType(AuthorizationGrantType(authorizationGrantType))
                    .authorizedScopes(authorizedScopes).attributes { attrs: MutableMap<String, Any> ->
                    attrs.putAll(attributes)
                }
                val state = rs.getString("state")
                if (StringUtils.hasText(state)) {
                    builder.attribute("state", state)
                }

                val authorizationCodeValue = this.getLobValue(rs, "authorization_code_value")
                var tokenIssuedAt: Instant?
                var tokenExpiresAt: Instant?
                if (StringUtils.hasText(authorizationCodeValue)) {
                    tokenIssuedAt = rs.getTimestamp("authorization_code_issued_at").toInstant()
                    tokenExpiresAt = rs.getTimestamp("authorization_code_expires_at").toInstant()
                    val authorizationCodeMetadata = this.parseMap(this.getLobValue(rs, "authorization_code_metadata"))
                    val authorizationCode =
                        OAuth2AuthorizationCode(authorizationCodeValue, tokenIssuedAt, tokenExpiresAt)
                    builder.token(authorizationCode) { metadata: MutableMap<String, Any> ->
                        metadata.putAll(authorizationCodeMetadata)
                    }
                }

                val accessTokenValue = this.getLobValue(rs, "access_token_value")
                var deviceCodeValue: String?
                if (StringUtils.hasText(accessTokenValue)) {
                    tokenIssuedAt = rs.getTimestamp("access_token_issued_at").toInstant()
                    tokenExpiresAt = rs.getTimestamp("access_token_expires_at").toInstant()
                    val accessTokenMetadata = this.parseMap(this.getLobValue(rs, "access_token_metadata"))
                    var tokenType: TokenType? = null
                    if (TokenType.BEARER.value.equals(rs.getString("access_token_type"), ignoreCase = true)) {
                        tokenType = TokenType.BEARER
                    }

                    var scopes: Set<String?>? = emptySet<String>()
                    deviceCodeValue = rs.getString("access_token_scopes")
                    if (deviceCodeValue != null) {
                        scopes = StringUtils.commaDelimitedListToSet(deviceCodeValue)
                    }

                    val accessToken =
                        OAuth2AccessToken(tokenType, accessTokenValue, tokenIssuedAt, tokenExpiresAt, scopes)
                    builder.token(accessToken) { metadata: MutableMap<String, Any> ->
                        metadata.putAll(accessTokenMetadata)
                    }
                }

                val oidcIdTokenValue = this.getLobValue(rs, "oidc_id_token_value")
                if (StringUtils.hasText(oidcIdTokenValue)) {
                    tokenIssuedAt = rs.getTimestamp("oidc_id_token_issued_at").toInstant()
                    tokenExpiresAt = rs.getTimestamp("oidc_id_token_expires_at").toInstant()
                    val oidcTokenMetadata = this.parseMap(this.getLobValue(rs, "oidc_id_token_metadata"))
                    val oidcToken = OidcIdToken(
                        oidcIdTokenValue,
                        tokenIssuedAt,
                        tokenExpiresAt,
                        oidcTokenMetadata[OAuth2Authorization.Token.CLAIMS_METADATA_NAME] as MutableMap<String, Any>
                    )
                    builder.token(oidcToken) { metadata: MutableMap<String, Any> ->
                        metadata.putAll(oidcTokenMetadata)
                    }
                }

                val refreshTokenValue = this.getLobValue(rs, "refresh_token_value")
                var userCodeMetadata: Map<*, *>
                if (StringUtils.hasText(refreshTokenValue)) {
                    tokenIssuedAt = rs.getTimestamp("refresh_token_issued_at").toInstant()
                    tokenExpiresAt = null
                    val refreshTokenExpiresAt = rs.getTimestamp("refresh_token_expires_at")
                    if (refreshTokenExpiresAt != null) {
                        tokenExpiresAt = refreshTokenExpiresAt.toInstant()
                    }

                    userCodeMetadata = this.parseMap(this.getLobValue(rs, "refresh_token_metadata"))
                    val refreshToken = OAuth2RefreshToken(refreshTokenValue, tokenIssuedAt, tokenExpiresAt)
                    val finalUserCodeMetadata = userCodeMetadata
                    builder.token(refreshToken) { metadata: MutableMap<String?, Any?> ->
                        metadata.putAll(finalUserCodeMetadata)
                    }
                }

                val userCodeValue = this.getLobValue(rs, "user_code_value")
                if (StringUtils.hasText(userCodeValue)) {
                    tokenIssuedAt = rs.getTimestamp("user_code_issued_at").toInstant()
                    tokenExpiresAt = rs.getTimestamp("user_code_expires_at").toInstant()
                    userCodeMetadata = this.parseMap(this.getLobValue(rs, "user_code_metadata"))
                    val userCode = OAuth2UserCode(userCodeValue, tokenIssuedAt, tokenExpiresAt)
                    val finalUserCodeMetadata1 = userCodeMetadata
                    builder.token(userCode) { metadata: MutableMap<String?, Any?> ->
                        metadata.putAll(finalUserCodeMetadata1)
                    }
                }

                deviceCodeValue = this.getLobValue(rs, "device_code_value")
                if (StringUtils.hasText(deviceCodeValue)) {
                    tokenIssuedAt = rs.getTimestamp("device_code_issued_at").toInstant()
                    tokenExpiresAt = rs.getTimestamp("device_code_expires_at").toInstant()
                    val deviceCodeMetadata = this.parseMap(this.getLobValue(rs, "device_code_metadata"))
                    val deviceCode = OAuth2DeviceCode(deviceCodeValue, tokenIssuedAt, tokenExpiresAt)
                    builder.token(deviceCode) { metadata: MutableMap<String, Any> ->
                        metadata.putAll(deviceCodeMetadata)
                    }
                }

                return builder.build()
            }
        }

        @Throws(SQLException::class)
        private fun getLobValue(rs: ResultSet, columnName: String): String? {
            var columnValue: String? = null
            val columnMetadata = columnMetadataMap[columnName]
            if (2004 == columnMetadata!!.getDataType()) {
                val columnValueBytes = lobHandler.getBlobAsBytes(rs, columnName)
                if (columnValueBytes != null) {
                    columnValue = String(columnValueBytes, StandardCharsets.UTF_8)
                }
            } else if (2005 == columnMetadata.getDataType()) {
                columnValue = lobHandler.getClobAsString(rs, columnName)
            } else {
                columnValue = rs.getString(columnName)
            }

            return columnValue
        }

        fun setLobHandler(lobHandler: LobHandler) {
            Assert.notNull(lobHandler, "lobHandler cannot be null")
            this.lobHandler = lobHandler
        }

        protected fun getLobHandler(): LobHandler {
            return this.lobHandler
        }

        private fun parseMap(data: String?): Map<String, Any> {
            try {
                return objectMapper.readValue(data!!, object : com.fasterxml.jackson.core.type.TypeReference<Map<String, Any>?>() {
                }) as Map<String, Any>
            } catch (var3: Exception) {
                throw IllegalArgumentException(var3.message, var3)
            }
        }
    }

    class OAuth2AuthorizationParametersMapper : Function<OAuth2Authorization, MutableList<SqlParameterValue>> {
        private var objectMapper: com.fasterxml.jackson.databind.ObjectMapper

        init {
            objectMapper = Jackson2.createObjectMapper()
        }

        override fun apply(authorization: OAuth2Authorization): MutableList<SqlParameterValue> {
            val parameters: MutableList<SqlParameterValue> = mutableListOf()
            parameters.add(SqlParameterValue(12, authorization.id))
            parameters.add(SqlParameterValue(12, authorization.registeredClientId))
            parameters.add(SqlParameterValue(12, authorization.principalName))
            parameters.add(SqlParameterValue(12, authorization.authorizationGrantType.value))
            var authorizedScopes: String? = null
            if (!CollectionUtils.isEmpty(authorization.authorizedScopes)) {
                authorizedScopes = StringUtils.collectionToDelimitedString(authorization.authorizedScopes, ",")
            }

            parameters.add(SqlParameterValue(12, authorizedScopes))
            val attributes = this.writeMap(authorization.attributes)
            parameters.add(mapToSqlParameter("attributes", attributes))
            var state: String? = null
            val authorizationState = authorization.getAttribute<Any>("state") as String?
            if (StringUtils.hasText(authorizationState)) {
                state = authorizationState
            }

            parameters.add(SqlParameterValue(12, state))
            val authorizationCode = authorization.getToken(
                OAuth2AuthorizationCode::class.java
            )
            val authorizationCodeSqlParameters =
                this.toSqlParameterList("authorization_code_value", "authorization_code_metadata", authorizationCode)
            parameters.addAll(authorizationCodeSqlParameters)
            val accessToken = authorization.getToken(
                OAuth2AccessToken::class.java
            )
            val accessTokenSqlParameters =
                this.toSqlParameterList("access_token_value", "access_token_metadata", accessToken)
            parameters.addAll(accessTokenSqlParameters)
            var accessTokenType: String? = null
            var accessTokenScopes: String? = null
            if (accessToken != null) {
                accessTokenType = (accessToken.token as OAuth2AccessToken).tokenType.value
                if (!CollectionUtils.isEmpty((accessToken.token as OAuth2AccessToken).scopes)) {
                    accessTokenScopes =
                        StringUtils.collectionToDelimitedString((accessToken.token as OAuth2AccessToken).scopes, ",")
                }
            }

            parameters.add(SqlParameterValue(12, accessTokenType))
            parameters.add(SqlParameterValue(12, accessTokenScopes))
            val oidcIdToken = authorization.getToken(OidcIdToken::class.java)
            val oidcIdTokenSqlParameters =
                this.toSqlParameterList("oidc_id_token_value", "oidc_id_token_metadata", oidcIdToken)
            parameters.addAll(oidcIdTokenSqlParameters)
            val refreshToken = authorization.refreshToken
            val refreshTokenSqlParameters =
                this.toSqlParameterList("refresh_token_value", "refresh_token_metadata", refreshToken)
            parameters.addAll(refreshTokenSqlParameters)
            val userCode = authorization.getToken(
                OAuth2UserCode::class.java
            )
            val userCodeSqlParameters = this.toSqlParameterList("user_code_value", "user_code_metadata", userCode)
            parameters.addAll(userCodeSqlParameters)
            val deviceCode = authorization.getToken(
                OAuth2DeviceCode::class.java
            )
            val deviceCodeSqlParameters =
                this.toSqlParameterList("device_code_value", "device_code_metadata", deviceCode)
            parameters.addAll(deviceCodeSqlParameters)
            return parameters
        }

        fun setObjectMapper(objectMapper: com.fasterxml.jackson.databind.ObjectMapper) {
            Assert.notNull(objectMapper, "objectMapper cannot be null")
            this.objectMapper = objectMapper
        }

        protected fun getObjectMapper(): com.fasterxml.jackson.databind.ObjectMapper {
            return this.objectMapper
        }

        private fun <T : OAuth2Token?> toSqlParameterList(
            tokenColumnName: String,
            tokenMetadataColumnName: String,
            token: OAuth2Authorization.Token<T>?
        ): List<SqlParameterValue> {
            val parameters: MutableList<SqlParameterValue> = mutableListOf()
            var tokenValue: String? = null
            var tokenIssuedAt: Timestamp? = null
            var tokenExpiresAt: Timestamp? = null
            var metadata: String? = null
            if (token != null) {
                tokenValue = token.token!!.tokenValue
                if (token.token!!.issuedAt != null) {
                    tokenIssuedAt = Timestamp.from(token.token!!.issuedAt)
                }

                if (token.token!!.expiresAt != null) {
                    tokenExpiresAt = Timestamp.from(token.token!!.expiresAt)
                }

                metadata = this.writeMap(token.metadata)
            }

            parameters.add(mapToSqlParameter(tokenColumnName, tokenValue))
            parameters.add(SqlParameterValue(93, tokenIssuedAt))
            parameters.add(SqlParameterValue(93, tokenExpiresAt))
            parameters.add(mapToSqlParameter(tokenMetadataColumnName, metadata))
            return parameters
        }

        private fun writeMap(data: Map<String, Any>): String {
            try {
                return objectMapper.writeValueAsString(data)
            } catch (var3: Exception) {
                throw IllegalArgumentException(var3.message, var3)
            }
        }
    }

    private class LobCreatorArgumentPreparedStatementSetter(private val lobCreator: LobCreator?, args: Array<Any>) :
        ArgumentPreparedStatementSetter(args) {

        @Throws(SQLException::class)
        override fun doSetValue(ps: PreparedStatement, parameterPosition: Int, argValue: Any?) {
            if (argValue is SqlParameterValue) {
                if (argValue.sqlType == 2004) {
                    if (argValue.value != null) {
                        Assert.isInstanceOf(
                            ByteArray::class.java,
                            argValue.value,
                            "Value of blob parameter must be byte[]"
                        )
                    }

                    val valueBytes = argValue.value as ByteArray
                    lobCreator!!.setBlobAsBytes(ps, parameterPosition, valueBytes)
                    return
                }

                if (argValue.sqlType == 2005) {
                    if (argValue.value != null) {
                        Assert.isInstanceOf(
                            String::class.java,
                            argValue.value,
                            "Value of clob parameter must be String"
                        )
                    }

                    val valueString = argValue.value as String
                    lobCreator!!.setClobAsString(ps, parameterPosition, valueString)
                    return
                }
            }

            super.doSetValue(ps, parameterPosition, argValue)
        }
    }

    private class ColumnMetadata(private val columnName: String, private val dataType: Int) {

        fun getColumnName(): String {
            return this.columnName
        }

        fun getDataType(): Int {
            return this.dataType
        }
    }

    companion object {
        private const val COLUMN_NAMES =
            "id, registered_client_id, principal_name, authorization_grant_type, authorized_scopes, attributes, state, authorization_code_value, authorization_code_issued_at, authorization_code_expires_at,authorization_code_metadata,access_token_value,access_token_issued_at,access_token_expires_at,access_token_metadata,access_token_type,access_token_scopes,oidc_id_token_value,oidc_id_token_issued_at,oidc_id_token_expires_at,oidc_id_token_metadata,refresh_token_value,refresh_token_issued_at,refresh_token_expires_at,refresh_token_metadata,user_code_value,user_code_issued_at,user_code_expires_at,user_code_metadata,device_code_value,device_code_issued_at,device_code_expires_at,device_code_metadata"
        private const val TABLE_NAME = "oauth2_authorization"
        private const val PK_FILTER = "id = ?"
        private const val UNKNOWN_TOKEN_TYPE_FILTER =
            "state = ? OR authorization_code_value = ? OR access_token_value = ? OR oidc_id_token_value = ? OR refresh_token_value = ? OR user_code_value = ? OR device_code_value = ?"
        private const val STATE_FILTER = "state = ?"
        private const val AUTHORIZATION_CODE_FILTER = "authorization_code_value = ?"
        private const val ACCESS_TOKEN_FILTER = "access_token_value = ?"
        private const val ID_TOKEN_FILTER = "oidc_id_token_value = ?"
        private const val REFRESH_TOKEN_FILTER = "refresh_token_value = ?"
        private const val USER_CODE_FILTER = "user_code_value = ?"
        private const val DEVICE_CODE_FILTER = "device_code_value = ?"
        private const val LOAD_AUTHORIZATION_SQL =
            "SELECT id, registered_client_id, principal_name, authorization_grant_type, authorized_scopes, attributes, state, authorization_code_value, authorization_code_issued_at, authorization_code_expires_at,authorization_code_metadata,access_token_value,access_token_issued_at,access_token_expires_at,access_token_metadata,access_token_type,access_token_scopes,oidc_id_token_value,oidc_id_token_issued_at,oidc_id_token_expires_at,oidc_id_token_metadata,refresh_token_value,refresh_token_issued_at,refresh_token_expires_at,refresh_token_metadata,user_code_value,user_code_issued_at,user_code_expires_at,user_code_metadata,device_code_value,device_code_issued_at,device_code_expires_at,device_code_metadata FROM oauth2_authorization WHERE "
        private const val SAVE_AUTHORIZATION_SQL =
            "INSERT INTO oauth2_authorization (id, registered_client_id, principal_name, authorization_grant_type, authorized_scopes, attributes, state, authorization_code_value, authorization_code_issued_at, authorization_code_expires_at,authorization_code_metadata,access_token_value,access_token_issued_at,access_token_expires_at,access_token_metadata,access_token_type,access_token_scopes,oidc_id_token_value,oidc_id_token_issued_at,oidc_id_token_expires_at,oidc_id_token_metadata,refresh_token_value,refresh_token_issued_at,refresh_token_expires_at,refresh_token_metadata,user_code_value,user_code_issued_at,user_code_expires_at,user_code_metadata,device_code_value,device_code_issued_at,device_code_expires_at,device_code_metadata) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        private const val UPDATE_AUTHORIZATION_SQL =
            "UPDATE oauth2_authorization SET registered_client_id = ?, principal_name = ?, authorization_grant_type = ?, authorized_scopes = ?, attributes = ?, state = ?, authorization_code_value = ?, authorization_code_issued_at = ?, authorization_code_expires_at = ?, authorization_code_metadata = ?, access_token_value = ?, access_token_issued_at = ?, access_token_expires_at = ?, access_token_metadata = ?, access_token_type = ?, access_token_scopes = ?, oidc_id_token_value = ?, oidc_id_token_issued_at = ?, oidc_id_token_expires_at = ?, oidc_id_token_metadata = ?, refresh_token_value = ?, refresh_token_issued_at = ?, refresh_token_expires_at = ?, refresh_token_metadata = ?, user_code_value = ?, user_code_issued_at = ?, user_code_expires_at = ?, user_code_metadata = ?, device_code_value = ?, device_code_issued_at = ?, device_code_expires_at = ?, device_code_metadata = ? WHERE id = ?"
        private const val REMOVE_AUTHORIZATION_SQL = "DELETE FROM oauth2_authorization WHERE id = ?"
        private var columnMetadataMap: MutableMap<String, ColumnMetadata> = mutableMapOf()
        private fun initColumnMetadata(jdbcOperations: JdbcOperations) {
            var columnMetadata = getColumnMetadata(jdbcOperations, "attributes", 2004)
            columnMetadataMap[columnMetadata.getColumnName()] = columnMetadata
            columnMetadata = getColumnMetadata(jdbcOperations, "authorization_code_value", 2004)
            columnMetadataMap[columnMetadata.getColumnName()] = columnMetadata
            columnMetadata = getColumnMetadata(jdbcOperations, "authorization_code_metadata", 2004)
            columnMetadataMap[columnMetadata.getColumnName()] = columnMetadata
            columnMetadata = getColumnMetadata(jdbcOperations, "access_token_value", 2004)
            columnMetadataMap[columnMetadata.getColumnName()] = columnMetadata
            columnMetadata = getColumnMetadata(jdbcOperations, "access_token_metadata", 2004)
            columnMetadataMap[columnMetadata.getColumnName()] = columnMetadata
            columnMetadata = getColumnMetadata(jdbcOperations, "oidc_id_token_value", 2004)
            columnMetadataMap[columnMetadata.getColumnName()] = columnMetadata
            columnMetadata = getColumnMetadata(jdbcOperations, "oidc_id_token_metadata", 2004)
            columnMetadataMap[columnMetadata.getColumnName()] = columnMetadata
            columnMetadata = getColumnMetadata(jdbcOperations, "refresh_token_value", 2004)
            columnMetadataMap[columnMetadata.getColumnName()] = columnMetadata
            columnMetadata = getColumnMetadata(jdbcOperations, "refresh_token_metadata", 2004)
            columnMetadataMap[columnMetadata.getColumnName()] = columnMetadata
            columnMetadata = getColumnMetadata(jdbcOperations, "user_code_value", 2004)
            columnMetadataMap[columnMetadata.getColumnName()] = columnMetadata
            columnMetadata = getColumnMetadata(jdbcOperations, "user_code_metadata", 2004)
            columnMetadataMap[columnMetadata.getColumnName()] = columnMetadata
            columnMetadata = getColumnMetadata(jdbcOperations, "device_code_value", 2004)
            columnMetadataMap[columnMetadata.getColumnName()] = columnMetadata
            columnMetadata = getColumnMetadata(jdbcOperations, "device_code_metadata", 2004)
            columnMetadataMap[columnMetadata.getColumnName()] = columnMetadata
        }

        private fun getColumnMetadata(
            jdbcOperations: JdbcOperations,
            columnName: String,
            defaultDataType: Int
        ): ColumnMetadata {
            val dataType = jdbcOperations.execute(ConnectionCallback { conn: Connection ->
                val databaseMetaData = conn.metaData
                var rs =
                    databaseMetaData.getColumns(null as String?, null as String?, "oauth2_authorization", columnName)
                if (rs.next()) {
                    return@ConnectionCallback rs.getInt("DATA_TYPE")
                } else {
                    rs = databaseMetaData.getColumns(
                        null as String?,
                        null as String?,
                        "oauth2_authorization".uppercase(Locale.getDefault()),
                        columnName.uppercase(Locale.getDefault())
                    )
                    return@ConnectionCallback if (rs.next()) rs.getInt("DATA_TYPE") else null
                }
            }) as Int
            return ColumnMetadata(columnName, dataType ?: defaultDataType)
        }

        private fun mapToSqlParameter(columnName: String, value: String?): SqlParameterValue {
            val columnMetadata = columnMetadataMap[columnName]
            return if (2004 == columnMetadata!!.getDataType() && StringUtils.hasText(value)) SqlParameterValue(
                2004, value!!.toByteArray(
                    StandardCharsets.UTF_8
                )
            ) else SqlParameterValue(columnMetadata.getDataType(), value)
        }
    }

    private object Jackson3 {
        fun createJsonMapper(): JsonMapper? {
            // 1. Use String patterns to allow broad categories of classes
            val ptv = BasicPolymorphicTypeValidator.builder()
                /// 1. CRITICAL: Allow the Base Type java.lang.Object (Kotlin's Any)
                .allowIfBaseType(Any::class.java)
                .allowIfBaseType(Object::class.java)

                // 2. Allow all types under the base java.lang package (covers Object itself)
                .allowIfSubType("java.lang.")

                // 3. Allow all types under the java.security package (covers Principal, etc.)
                .allowIfSubType("java.security.")

                // 4. Allow standard Java collections
                .allowIfSubType("java.util.")

                // 5. Allow Spring Security types
                .allowIfSubType("org.springframework.security.")

                // 6. Allow your specific application models
                .allowIfSubType("com.bittokazi.")
                .build()

            val modules: MutableList<JacksonModule> =
                SecurityJacksonModules.getModules(Jackson3::class.java.getClassLoader())

            return JsonMapper
                .builder()
                .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
                .polymorphicTypeValidator(ptv) // Apply the validator
                .addModules(modules)
                .addMixIn(UserOauth::class.java, UserDetails::class.java)
                .addMixIn(RoleOauth::class.java, UserDetails::class.java)
                // .addMixIn(RoleOauth::class.java, UserOauth::class.java) // <--- Remove this, it looks incorrect/circular
                .build()
        }
    }

    @Deprecated(forRemoval = true, since = "7.0")
    private object Jackson2 {
        fun createObjectMapper(): com.fasterxml.jackson.databind.ObjectMapper {
            val objectMapper = com.fasterxml.jackson.databind.ObjectMapper()
            val classLoader = Jackson2::class.java.getClassLoader()
            val securityModules: MutableList<Module> = SecurityJackson2Modules.getModules(classLoader)
            objectMapper.registerModules(securityModules)
            objectMapper.registerModule(OAuth2AuthorizationServerJackson2Module())
            objectMapper.addMixIn(UserOauth::class.java, UserDetails::class.java)
            objectMapper.addMixIn(RoleOauth::class.java, UserDetails::class.java)
            objectMapper.addMixIn(RoleOauth::class.java, UserOauth::class.java)
            return objectMapper
        }
    }

    abstract class AbstractOAuth2AuthorizationRowMapper(
        registeredClientRepository: RegisteredClientRepository
    ): RowMapper<OAuth2Authorization> {
        protected val registeredClientRepository: RegisteredClientRepository

        private var lobHandler: LobHandler = DefaultLobHandler()

        init {
            Assert.notNull(registeredClientRepository, "registeredClientRepository cannot be null")
            this.registeredClientRepository = registeredClientRepository
        }

        @Throws(SQLException::class)
        override fun mapRow(rs: ResultSet, rowNum: Int): OAuth2Authorization {
            val registeredClientId = rs.getString("registered_client_id")
            val registeredClient = this.registeredClientRepository.findById(registeredClientId)
            if (registeredClient == null) {
                throw DataRetrievalFailureException(
                    ("The RegisteredClient with id '" + registeredClientId
                            + "' was not found in the RegisteredClientRepository.")
                )
            }

            val builder = OAuth2Authorization.withRegisteredClient(registeredClient)
            val id = rs.getString("id")
            val principalName = rs.getString("principal_name")
            val authorizationGrantType = rs.getString("authorization_grant_type")
            var authorizedScopes = mutableSetOf<String>()
            val authorizedScopesString = rs.getString("authorized_scopes")
            if (authorizedScopesString != null) {
                authorizedScopes = StringUtils.commaDelimitedListToSet(authorizedScopesString)
            }
            val attributes = parseMap(getLobValue(rs, "attributes"))

            builder.id(id)
                .principalName(principalName)
                .authorizationGrantType(AuthorizationGrantType(authorizationGrantType))
                .authorizedScopes(authorizedScopes)
                .attributes(Consumer { attrs: MutableMap<String?, Any?>? -> attrs!!.putAll(attributes) })

            val state = rs.getString("state")
            if (StringUtils.hasText(state)) {
                builder.attribute(OAuth2ParameterNames.STATE, state)
            }

            var tokenIssuedAt: Instant?
            var tokenExpiresAt: Instant?
            val authorizationCodeValue = getLobValue(rs, "authorization_code_value")

            if (StringUtils.hasText(authorizationCodeValue)) {
                tokenIssuedAt = rs.getTimestamp("authorization_code_issued_at").toInstant()
                tokenExpiresAt = rs.getTimestamp("authorization_code_expires_at").toInstant()
                val authorizationCodeMetadata =
                    parseMap(getLobValue(rs, "authorization_code_metadata"))

                val authorizationCode = OAuth2AuthorizationCode(
                    authorizationCodeValue,
                    tokenIssuedAt, tokenExpiresAt
                )
                builder.token<OAuth2AuthorizationCode?>(
                    authorizationCode,
                    Consumer { metadata: MutableMap<String?, Any?>? -> metadata!!.putAll(authorizationCodeMetadata) })
            }

            val accessTokenValue = getLobValue(rs, "access_token_value")
            if (StringUtils.hasText(accessTokenValue)) {
                tokenIssuedAt = rs.getTimestamp("access_token_issued_at").toInstant()
                tokenExpiresAt = rs.getTimestamp("access_token_expires_at").toInstant()
                val accessTokenMetadata =
                    parseMap(getLobValue(rs, "access_token_metadata"))
                var tokenType: TokenType? = null
                if (TokenType.BEARER.getValue().equals(rs.getString("access_token_type"), ignoreCase = true)) {
                    tokenType = TokenType.BEARER
                } else if (TokenType.DPOP.getValue()
                        .equals(rs.getString("access_token_type"), ignoreCase = true)
                ) {
                    tokenType = TokenType.DPOP
                }

                var scopes = mutableSetOf<String>()
                val accessTokenScopes = rs.getString("access_token_scopes")
                if (accessTokenScopes != null) {
                    scopes = StringUtils.commaDelimitedListToSet(accessTokenScopes)
                }
                val accessToken = OAuth2AccessToken(
                    tokenType, accessTokenValue, tokenIssuedAt,
                    tokenExpiresAt, scopes
                )
                builder.token<OAuth2AccessToken?>(
                    accessToken,
                    Consumer { metadata: MutableMap<String?, Any?>? -> metadata!!.putAll(accessTokenMetadata) })
            }

            val oidcIdTokenValue = getLobValue(rs, "oidc_id_token_value")
            if (StringUtils.hasText(oidcIdTokenValue)) {
                tokenIssuedAt = rs.getTimestamp("oidc_id_token_issued_at").toInstant()
                tokenExpiresAt = rs.getTimestamp("oidc_id_token_expires_at").toInstant()
                val oidcTokenMetadata = parseMap(getLobValue(rs, "oidc_id_token_metadata"))

                val oidcToken = OidcIdToken(
                    oidcIdTokenValue, tokenIssuedAt, tokenExpiresAt,
                    oidcTokenMetadata.get(OAuth2Authorization.Token.CLAIMS_METADATA_NAME) as MutableMap<String?, Any?>?
                )
                builder.token<OidcIdToken?>(
                    oidcToken,
                    Consumer { metadata: MutableMap<String?, Any?>? -> metadata!!.putAll(oidcTokenMetadata) })
            }

            val refreshTokenValue = getLobValue(rs, "refresh_token_value")
            if (StringUtils.hasText(refreshTokenValue)) {
                tokenIssuedAt = rs.getTimestamp("refresh_token_issued_at").toInstant()
                tokenExpiresAt = null
                val refreshTokenExpiresAt = rs.getTimestamp("refresh_token_expires_at")
                if (refreshTokenExpiresAt != null) {
                    tokenExpiresAt = refreshTokenExpiresAt.toInstant()
                }
                val refreshTokenMetadata =
                    parseMap(getLobValue(rs, "refresh_token_metadata"))

                val refreshToken = OAuth2RefreshToken(
                    refreshTokenValue, tokenIssuedAt,
                    tokenExpiresAt
                )
                builder.token<OAuth2RefreshToken?>(
                    refreshToken,
                    Consumer { metadata: MutableMap<String?, Any?>? -> metadata!!.putAll(refreshTokenMetadata) })
            }

            val userCodeValue = getLobValue(rs, "user_code_value")
            if (StringUtils.hasText(userCodeValue)) {
                tokenIssuedAt = rs.getTimestamp("user_code_issued_at").toInstant()
                tokenExpiresAt = rs.getTimestamp("user_code_expires_at").toInstant()
                val userCodeMetadata = parseMap(getLobValue(rs, "user_code_metadata"))

                val userCode = OAuth2UserCode(userCodeValue, tokenIssuedAt, tokenExpiresAt)
                builder.token<OAuth2UserCode?>(
                    userCode,
                    Consumer { metadata: MutableMap<String?, Any?>? -> metadata!!.putAll(userCodeMetadata) })
            }

            val deviceCodeValue = getLobValue(rs, "device_code_value")
            if (StringUtils.hasText(deviceCodeValue)) {
                tokenIssuedAt = rs.getTimestamp("device_code_issued_at").toInstant()
                tokenExpiresAt = rs.getTimestamp("device_code_expires_at").toInstant()
                val deviceCodeMetadata = parseMap(getLobValue(rs, "device_code_metadata"))

                val deviceCode = OAuth2DeviceCode(deviceCodeValue, tokenIssuedAt, tokenExpiresAt)
                builder.token<OAuth2DeviceCode?>(
                    deviceCode,
                    Consumer { metadata: MutableMap<String?, Any?>? -> metadata!!.putAll(deviceCodeMetadata) })
            }

            return builder.build()
        }

        @Throws(SQLException::class)
        fun getLobValue(rs: ResultSet, columnName: String): String? {
            var columnValue: String? = null
            val columnMetadata: ColumnMetadata =
                columnMetadataMap.get(columnName)!!
            if (Types.BLOB == columnMetadata.getDataType()) {
                val columnValueBytes: ByteArray = this.lobHandler.getBlobAsBytes(rs, columnName)!!
                if (columnValueBytes != null) {
                    columnValue = String(columnValueBytes, StandardCharsets.UTF_8)
                }
            } else if (Types.CLOB == columnMetadata.getDataType()) {
                columnValue = this.lobHandler.getClobAsString(rs, columnName)
            } else {
                columnValue = rs.getString(columnName)
            }
            return columnValue
        }

        fun setLobHandler(lobHandler: LobHandler) {
            Assert.notNull(lobHandler, "lobHandler cannot be null")
            this.lobHandler = lobHandler
        }

        protected fun getLobHandler(): LobHandler {
            return this.lobHandler
        }

        fun parseMap(data: String?): MutableMap<String?, Any?> {
            try {
                return readValue(data)
            } catch (ex: java.lang.Exception) {
                throw java.lang.IllegalArgumentException(ex.message, ex)
            }
        }

        @Throws(java.lang.Exception::class)
        abstract fun readValue(data: String?): MutableMap<String?, Any?>
    }
}
