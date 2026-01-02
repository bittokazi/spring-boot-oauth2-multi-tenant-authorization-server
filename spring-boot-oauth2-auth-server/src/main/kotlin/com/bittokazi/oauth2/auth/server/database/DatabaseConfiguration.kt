package com.bittokazi.oauth2.auth.server.database

import com.bittokazi.oauth2.auth.server.utils.Utils.getDbConfig
import com.zaxxer.hikari.HikariDataSource
import jakarta.persistence.EntityManagerFactory
import liquibase.integration.spring.SpringLiquibase
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.jpa.autoconfigure.JpaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.JpaVendorAdapter
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.sql.SQLException
import java.util.*
import javax.sql.DataSource

/**
 * @author Bitto Kazi
 */
@Configuration
@EnableConfigurationProperties(JpaProperties::class)
@EnableJpaRepositories(
    entityManagerFactoryRef = "masterEntityManager",
    transactionManagerRef = "masterTransactionManager",
    basePackages = ["com.bittokazi.oauth2.auth.server.app.repositories.master"]
)
@EnableTransactionManagement
open class DatabaseConfiguration(jpaProperties: JpaProperties) {
    @Value("\${liquibase.context}")
    private val liquibaseContext: String? = null

    @Value("\${spring.datasource.url}")
    private val url: String? = null

    @Value("\${spring.datasource.port}")
    private val port: String? = null

    @Value("\${spring.datasource.databaseName}")
    private val databaseName: String? = null

    @Value("\${spring.datasource.username}")
    private val user: String? = null

    @Value("\${spring.datasource.password}")
    private val password: String? = null

    @Value("\${spring.datasource.dataSourceClassName}")
    private val dataSourceClassName: String? = null

    private var dataBaseExist = false

    @Bean(destroyMethod = "close")
    open fun dataSource(): DataSource? {
        val config = getDbConfig(dataSourceClassName, url, port, "postgres", user, password)

        try {
            logger.info("Configuring datasource {} {} {}", dataSourceClassName, url, user)
            config.minimumIdle = 0
            val ds = HikariDataSource(config)
            try {
                val dbExist = ("SELECT datname FROM pg_catalog.pg_database WHERE lower(datname) = lower('"
                        + databaseName + "')")
                val statementdbExist = ds.connection.prepareStatement(dbExist)
                if (statementdbExist.executeQuery().next()) {
                    statementdbExist.close()
                    ds.close()
                    this.dataBaseExist = true
                } else {
                    statementdbExist.close()
                    val sql = "CREATE DATABASE $databaseName"
                    val statement = ds.connection.prepareStatement(sql)
                    statement.executeUpdate()
                    statement.close()
                    ds.close()
                }
            } catch (e: SQLException) {
                logger.error("ERROR Configuring datasource SqlException {} {} {}", e)
                this.dataBaseExist = true
            } catch (ee: Exception) {
                this.dataBaseExist = true
                logger.debug("ERROR Configuring datasource catch ", ee)
            }
            val config2 = getDbConfig(dataSourceClassName, url, port, databaseName, user, password)
            return HikariDataSource(config2)
        } catch (e: Exception) {
            logger.error("ERROR in database configuration ", e)
        } finally {
        }
        return null
    }

    @Bean
    open fun liquibase(dataSource: DataSource?): SpringLiquibase {
        val sl = SpringLiquibase()
        sl.dataSource = dataSource
        sl.contexts = liquibaseContext
        if (this.dataBaseExist) {
            sl.changeLog = "classpath:/db-migration/db-master-alter.xml"
        } else {
            sl.changeLog = "classpath:/db-migration/db-master.xml"
        }
        sl.setShouldRun(true)
        return sl
    }

    @Bean(name = ["masterEntityManager"])
    open fun entityManagerFactory(): LocalContainerEntityManagerFactoryBean {
        val vendorAdapter: JpaVendorAdapter = HibernateJpaVendorAdapter()
        val em = LocalContainerEntityManagerFactoryBean()
        em.dataSource = dataSource()
        em.setPackagesToScan("com.bittokazi.oauth2.auth.server.app.models.master")
        em.jpaVendorAdapter = vendorAdapter
        em.setJpaProperties(Properties())
        em.persistenceUnitName = "master"
        return em
    }

    @Bean(name = ["masterTransactionManager"])
    open fun transactionManager(masterEntityManager: EntityManagerFactory?): JpaTransactionManager {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = masterEntityManager
        return transactionManager
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(DatabaseConfiguration::class.java)
    }
}
