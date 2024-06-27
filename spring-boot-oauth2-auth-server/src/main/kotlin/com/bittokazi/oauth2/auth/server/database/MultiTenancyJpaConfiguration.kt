package com.bittokazi.oauth2.auth.server.database

import jakarta.persistence.EntityManagerFactory
import org.hibernate.cfg.Environment
import org.hibernate.context.spi.CurrentTenantIdentifierResolver
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.JpaVendorAdapter
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource


@Configuration
//@ComponentScan("com.bittokazi.oauth2.auth.server.app.repositories.tenant")
@EnableConfigurationProperties(
    JpaProperties::class
)
@EnableJpaRepositories(
    entityManagerFactoryRef = "tenantEntityManager",
    transactionManagerRef = "tenantTransactionManager",
    basePackages = ["com.bittokazi.oauth2.auth.server.app.repositories.tenant"]
)
@EnableTransactionManagement
open class MultiTenancyJpaConfiguration {

    @Bean
    open fun jpaVendorAdapter(): JpaVendorAdapter {
        return HibernateJpaVendorAdapter()
    }

    @Bean(name = ["tenantEntityManager"])
    open fun entityManagerFactory(
        dataSource: DataSource?,
        connectionProvider: MultiTenantConnectionProvider<String>,
        tenantResolver: CurrentTenantIdentifierResolver<String>
    ): LocalContainerEntityManagerFactoryBean {
        val emfBean = LocalContainerEntityManagerFactoryBean()
        emfBean.dataSource = dataSource
        emfBean.setPackagesToScan("com.bittokazi.oauth2.auth.server.app.models.tenant")
        emfBean.jpaVendorAdapter = jpaVendorAdapter()

        val properties: MutableMap<String, Any?> = HashMap()
        properties[Environment.MULTI_TENANT_CONNECTION_PROVIDER] = connectionProvider
        properties[Environment.MULTI_TENANT_IDENTIFIER_RESOLVER] = tenantResolver
        properties["hibernate.ejb.naming_strategy"] = "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy"

        emfBean.jpaPropertyMap = properties
        return emfBean
    }

    @Bean(name = ["tenantTransactionManager"])
    open fun transactionManager(tenantEntityManager: EntityManagerFactory?): JpaTransactionManager {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = tenantEntityManager
        return transactionManager
    }
}

