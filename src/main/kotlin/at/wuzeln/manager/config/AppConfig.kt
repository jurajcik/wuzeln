package at.wuzeln.manager.config

import org.hibernate.cfg.AvailableSettings
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.Environment
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.util.*
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

@Configuration
@PropertySource(value = [
    "classpath:application.properties",
    "classpath:application-env.properties",
    "classpath:application-private.properties"
])
@ComponentScan(at.wuzeln.manager.config.AppConfig.BASE_PACKAGE)
@EnableJpaRepositories(basePackages = ["${at.wuzeln.manager.config.AppConfig.BASE_PACKAGE}.dao"])
// @EnableScheduling
@EnableTransactionManagement
class AppConfig {

    companion object {
        const val BASE_PACKAGE: String = "at.wuzeln.manager"
    }

    @Autowired
    private lateinit var env: Environment

    @Bean
    fun entityManagerFactory(): LocalContainerEntityManagerFactoryBean {
        val em = LocalContainerEntityManagerFactoryBean()
        em.dataSource = dataSource()
        em.setPackagesToScan("${at.wuzeln.manager.config.AppConfig.Companion.BASE_PACKAGE}.model")

        val vendorAdapter = HibernateJpaVendorAdapter()
        em.jpaVendorAdapter = vendorAdapter
        em.setJpaProperties(additionalProperties())

        return em
    }

    @Bean
    fun dataSource(): DataSource {
        val dataSource = DriverManagerDataSource()
        dataSource.setDriverClassName(env.getRequiredProperty("db.driver"))
        dataSource.url = env.getProperty("db.url")
        dataSource.username = env.getProperty("db.username")
        dataSource.password = env.getProperty("db.password")
        return dataSource
    }

    @Bean
    fun transactionManager(emf: EntityManagerFactory): PlatformTransactionManager {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = emf
        return transactionManager
    }

    @Bean
    fun exceptionTranslation(): PersistenceExceptionTranslationPostProcessor {
        return PersistenceExceptionTranslationPostProcessor()
    }

    fun additionalProperties(): Properties {
        val properties = Properties()
        properties.setProperty(AvailableSettings.SHOW_SQL, env.getProperty("hibernate.show_sql", "false"))
        properties.setProperty(AvailableSettings.HBM2DDL_AUTO, env.getRequiredProperty("hibernate.hbm2ddl.auto"))
        properties.setProperty(AvailableSettings.DIALECT, env.getRequiredProperty("hibernate.dialect"))
        return properties
    }


}