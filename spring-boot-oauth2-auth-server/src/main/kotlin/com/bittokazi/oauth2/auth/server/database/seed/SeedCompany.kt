package com.bittokazi.oauth2.auth.server.database.seed

import com.bittokazi.oauth2.auth.server.database.seed.SeedOauthClient
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
import java.util.stream.Collectors

class SeedCompany : CustomTaskChange {
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
                val sql = "INSERT INTO tenant (id,company_key,enabled,name,domain) VALUES(?,?,?,?,?)"

                val statement = databaseConnection.prepareStatement(sql)
                statement.setString(1, record["id"])
                statement.setString(2, record["company_key"])
                statement.setBoolean(3, record["enabled"].toBoolean())
                statement.setString(4, record["name"])
                statement.setString(5, record["domain"])



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
