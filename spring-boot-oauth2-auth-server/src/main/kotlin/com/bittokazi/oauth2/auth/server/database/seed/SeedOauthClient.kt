package com.bittokazi.oauth2.auth.server.database.seed

import com.bittokazi.oauth2.auth.server.utils.Utils.randomNumberGenerator
import liquibase.change.custom.CustomTaskChange
import liquibase.database.Database
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.CustomChangeException
import liquibase.exception.SetupException
import liquibase.exception.ValidationErrors
import liquibase.resource.ResourceAccessor
import org.apache.commons.csv.CSVFormat
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InputStreamReader
import java.io.Reader
import java.util.*
import java.util.stream.Collectors

class SeedOauthClient : CustomTaskChange {
    private var resourceAccessor: ResourceAccessor? = null

    private var clientFileName: String? = null

    fun setClientFileName(clientFileName: String?) {
        this.clientFileName = clientFileName
    }

    @Throws(CustomChangeException::class)
    override fun execute(database: Database) {
        val databaseConnection = database.connection as JdbcConnection
        try {
            logger.info(clientFileName)
            val stream = resourceAccessor!!.openStream(null, clientFileName)
            val `in`: Reader = InputStreamReader(stream)

            val records = CSVFormat.EXCEL.withHeader().parse(`in`).records.stream().collect(Collectors.toSet())
            for (record in records) {
                val sql =
                    "INSERT INTO oauth_client_details (id,client_id,resource_ids,client_secret,scope,client_authentication_method,authorized_grant_types,web_server_redirect_uri,access_token_validity,refresh_token_validity,additional_information,require_consent,post_logout_url,token_type) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

                val statement = databaseConnection.prepareStatement(sql)
                statement.setString(1, record["id"])
                statement.setString(2, UUID.randomUUID().toString())
                statement.setString(3, record["resource_ids"])
                statement.setString(4, randomNumberGenerator(30))
                statement.setString(5, record["scope"])
                statement.setString(6, record["client_authentication_method"])
                statement.setString(7, record["authorized_grant_types"])
                statement.setString(8, record["web_server_redirect_uri"])
                statement.setInt(9, record["access_token_validity"].toInt())
                statement.setInt(10, record["refresh_token_validity"].toInt())
                statement.setString(11, record["additional_information"])
                statement.setBoolean(12, record["require_consent"].toBoolean())
                statement.setString(13, record["post_logout_url"])
                statement.setString(14, record["token_type"])

                statement.executeUpdate()
                databaseConnection.commit()
                statement.close()
            }
            `in`.close()
        } catch (e: Exception) {
            throw CustomChangeException(e)
        }
    }

    override fun getConfirmationMessage(): String? {
        return null
    }

    @Throws(SetupException::class)
    override fun setUp() {
    }

    override fun setFileOpener(resourceAccessor: ResourceAccessor) {
        this.resourceAccessor = resourceAccessor
    }

    override fun validate(database: Database): ValidationErrors? {
        return null
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(SeedOauthClient::class.java)
    }
}
