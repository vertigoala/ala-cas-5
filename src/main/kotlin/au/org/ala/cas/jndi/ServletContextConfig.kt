package au.org.ala.cas.jndi

import au.org.ala.utils.logger
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.ServletContextInitializer
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import javax.naming.InitialContext
import javax.naming.NamingException
import javax.sql.DataSource

/**
 * This Configuration simply creates simple JNDI datasources based off the JndiConfigurationProperites.
 */
@Configuration
@EnableConfigurationProperties(JndiConfigurationProperties::class)
open class ServletContextConfig {

    companion object {
        val log = logger<ServletContextConfig>()
    }

    @Autowired
    lateinit var jndiConfigurationProperties: JndiConfigurationProperties

    @Bean
    @Scope("singleton")
//    @RefreshScope
    open fun simpleJndiInitializer(): ServletContextInitializer {
        return ServletContextInitializer { _ ->
            // Obtain our environment naming context
            val initCtx = InitialContext()
            log.info("Adding {} JNDI Hikari Datasources", jndiConfigurationProperties.hikari.size)
            jndiConfigurationProperties.hikari.filter {
                try {
                    initCtx.lookup(it.name) == null
                } catch (e: NamingException) {
                    true
                }
            }.map(this::hikariDataSource).forEach { (name, dataSource) ->
                log.debug("Inserting Hikari Datasource $dataSource at $name")
                initCtx.bind(name, dataSource)
            }
        }
    }

    private fun hikariDataSource(jndiJdbc: HikariDatasource): Pair<String, DataSource> {
        val config = HikariConfig().apply {
            // Hikari can determine the driver from the URL
            jndiJdbc.driverClass?.let { driverClassName = it }
            jdbcUrl = jndiJdbc.url
            username = jndiJdbc.user
            password = jndiJdbc.password
            jndiJdbc.poolName = jndiJdbc.name // TODO good idea?
            jndiJdbc.allowPoolSuspension?.let { isAllowPoolSuspension = it }
            jndiJdbc.autoCommit?.let { isAutoCommit = it }
            jndiJdbc.catalog?.let { catalog = it }
            jndiJdbc.connectionInitSql?.let { connectionInitSql = it }
            jndiJdbc.connectionTestQuery?.let { connectionTestQuery = it }
            jndiJdbc.connectionTimeout?.let { connectionTimeout = it }
            jndiJdbc.idleTimeout?.let { idleTimeout = it }
            jndiJdbc.initializationFailTimeout?.let { initializationFailTimeout = it }
            jndiJdbc.isolateInternalQueries?.let { isIsolateInternalQueries = it }
            jndiJdbc.leakDetectionThreshold?.let { leakDetectionThreshold = it }
            jndiJdbc.maxLifetime?.let { maxLifetime = it }
            jndiJdbc.minimumIdle?.let { minimumIdle = it }
            jndiJdbc.maximumPoolSize?.let { maximumPoolSize = it }
            jndiJdbc.poolName?.let { poolName = it }
            jndiJdbc.readOnly?.let { isReadOnly = it }
            jndiJdbc.registerMbeans?.let { isRegisterMbeans = it }
            jndiJdbc.transactionIsolation?.let { transactionIsolation = it }
            jndiJdbc.validationTimeout?.let { validationTimeout = it }
            jndiJdbc.dataSourceProperties.forEach { (name, value) -> dataSourceProperties[name] = value }
        }
        val dataSource = HikariDataSource(config)
        dataSource.validate()
        return jndiJdbc.name to dataSource
    }
}
