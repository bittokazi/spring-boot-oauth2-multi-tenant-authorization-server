package com.bittokazi.oauth2.auth.server.database

import com.bittokazi.oauth2.auth.server.app.models.master.Tenant
import com.bittokazi.oauth2.auth.server.app.repositories.master.TenantRepository
import com.bittokazi.oauth2.auth.server.utils.Utils.getDbConfig
import com.bittokazi.oauth2.auth.server.utils.logger
import com.zaxxer.hikari.HikariDataSource
import jakarta.annotation.PostConstruct
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.LiquibaseException
import liquibase.resource.ClassLoaderResourceAccessor
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.*
import javax.sql.DataSource

/**
 *
 * @author Bitto Kazi
 */
@Component
@Transactional(value = "masterTransactionManager", readOnly = true)
@EnableAsync
@EnableCaching
@EnableAspectJAutoProxy(proxyTargetClass = true)
open class MultiTenantConnectionProviderImpl(
    private val tenantRepository: TenantRepository,
    private val dataSource: DataSource
) : AbstractDataSourceBasedMultiTenantConnectionProviderImpl<String>(), ApplicationListener<ContextRefreshedEvent> {

    val logger = logger()

    private var mapCompanyNameDataSource: MutableMap<String, DataSource>? = null

    @Value("\${spring.datasource.url}")
    val url: String? = null

    @Value("\${spring.datasource.port}")
    private val port: String? = null

    @Value("\${spring.datasource.dataSourceClassName}")
    val dataSourceClassName: String? = null

    @Value("\${spring.datasource.username}")
    val user: String? = null

    @Value("\${spring.datasource.password}")
    val password: String? = null

    @Value("\${spring.datasource.databaseName}")
    private val databaseName: String? = null

    @PostConstruct
    fun load() {
        mapCompanyNameDataSource = HashMap()
    }

    private fun init() {
        logger.info("\uD83D\uDE80 Initiated all tenants. ")

        for (tenant in tenantRepository.findAll()) {
            val schemaExist =
                "SELECT schema_name FROM information_schema.schemata WHERE schema_name = '" + (tenant?.companyKey?.lowercase(
                    Locale.getDefault()
                ) ?: "") + "'"
            var statementSchemaExist: PreparedStatement? = null
            var dsMainDb: HikariDataSource? = null
            try {
                val configMainDb = getDbConfig(dataSourceClassName, url, port, databaseName, user, password)
                dsMainDb = HikariDataSource(configMainDb)
                statementSchemaExist = dsMainDb.connection.prepareStatement(schemaExist)
                if (statementSchemaExist.executeQuery().next()) {
                    statementSchemaExist.close()
                    dsMainDb.close()
                    var companySchemaUrl = ""
                    try {
                        if (tenant != null) {
                            companySchemaUrl = url + "?currentSchema=" + tenant.companyKey
                        }
                        logger.info("Configuring datasource {} {} {}", dataSourceClassName, companySchemaUrl, user)
                        val config = getDbConfig(dataSourceClassName, url, port, databaseName, user, password)
                        if (tenant != null) {
                            config.addDataSourceProperty("currentSchema", tenant.companyKey)
                        }
                        val ds = HikariDataSource(config)
                        if (tenant != null) {
                            tenant.companyKey?.let {
                                mapCompanyNameDataSource!![it] = ds
                            }
                        }

                        // Update database of each tenant with liquibase
                        try {
                            val database = DatabaseFactory.getInstance()
                                .findCorrectDatabaseImplementation(JdbcConnection(ds.connection))
                            val liquibase = Liquibase(
                                "db-migration/db-tenant-alter.xml", ClassLoaderResourceAccessor(),
                                database
                            )
                            liquibase.update("test, production")
                            database.close()
                        } catch (e: Exception) {
                            logger.error("ERROR executing liquibase {}", e)
                        }

                        // ds.close();
                    } catch (e: Exception) {
                        logger.error("Error in database URL {}", companySchemaUrl, e)
                    }
                } else {
                    //statementSchemaExist.close();
                    if (tenant != null) {
                        logger.error("Shchema Does not Exist>>>>>>>>>>>> " + tenant.companyKey)
                    }
                }
            } catch (e1: SQLException) {
                // TODO Auto-generated catch block
                try {
                    statementSchemaExist?.close()
                    dsMainDb?.close()
                } catch (e: SQLException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }
                e1.printStackTrace()
            }
        }
    }

    /**
     * @param companyName
     */
    private fun establishDBConnectionAndAddToMapping(companyName: String) {
        val config = getDbConfig(dataSourceClassName, url, port, databaseName, user, password)
        config.addDataSourceProperty("currentSchema", companyName)
        val ds = HikariDataSource(config)
        mapCompanyNameDataSource!![companyName] = ds
        try {
            val database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(JdbcConnection(ds.connection))
            val liquibase = Liquibase("db-migration/db-tenant.xml", ClassLoaderResourceAccessor(), database)
            liquibase.update("test, production")
            database.close()
        } catch (e: Exception) {
            logger.error("ERROR executing liquibase {}", e)
        }
        // ds.close();
    }

    /**
     * create DB companyName and add it to mapCompanyNameDataSource
     *
     * @param ds
     * @param companyName
     * @throws SQLException
     * @throws LiquibaseException
     */
    @Throws(SQLException::class, LiquibaseException::class)
    private fun initDbWithLiquibase(ds: HikariDataSource, companyName: String) {
        val database = DatabaseFactory.getInstance()
            .findCorrectDatabaseImplementation(JdbcConnection(ds.connection))
        val databaseConnection = database.connection as JdbcConnection
        val sql = "CREATE SCHEMA $companyName"
        val statement = databaseConnection.prepareStatement(sql)
        statement.executeUpdate()
        databaseConnection.commit()
        statement.close()
        databaseConnection.close()
        ds.close()
        establishDBConnectionAndAddToMapping(companyName) // call
    }

    fun singleTenantCreation(tenant: Tenant) {
        try {
            val config = getDbConfig(dataSourceClassName, url, port, databaseName, user, password)
            val ds = HikariDataSource(config)
            tenant.companyKey?.let { initDbWithLiquibase(ds, it) }
        } catch (e: Exception) {
            logger.error("Error in database URL {}", url, e)
        }
    }

    override fun selectAnyDataSource(): DataSource {
        logger.debug("######### Selecting any data source")
        return dataSource
    }

    override fun selectDataSource(tenantIdentifier: String): DataSource {
        logger.debug("+++++++++++ Selecting data source for {}", tenantIdentifier)
        return if (mapCompanyNameDataSource!!.containsKey(tenantIdentifier)) mapCompanyNameDataSource!![tenantIdentifier]!!
        else dataSource
    }

    fun getDataSource(tenantIdentifier: String): DataSource {
        return selectDataSource(tenantIdentifier)
    }

    override fun onApplicationEvent(contextRefreshedEvent: ContextRefreshedEvent) {
        init()
    }

    companion object {
        private const val serialVersionUID = 1L
        private val logger: Logger = LoggerFactory.getLogger(MultiTenantConnectionProviderImpl::class.java)
    }
}

