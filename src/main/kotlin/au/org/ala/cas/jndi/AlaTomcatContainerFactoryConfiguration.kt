package au.org.ala.cas.jndi

import au.org.ala.utils.logger
import com.zaxxer.hikari.HikariJNDIFactory
import org.apache.catalina.startup.Tomcat
import org.apache.tomcat.util.descriptor.web.ContextResource
import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.configuration.support.JpaBeans
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory
import org.springframework.core.Ordered
import javax.servlet.Servlet

/**
 * This Configuration simply creates simple JNDI datasources based off the JndiConfigurationProperites.
 */
@Configuration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnClass(Servlet::class, Tomcat::class)
@EnableConfigurationProperties(JndiConfigurationProperties::class, CasConfigurationProperties::class)
class AlaTomcatContainerFactoryConfiguration {

    companion object {
        val log = logger()
    }

    @Autowired
    lateinit var jndiConfigurationProperties: JndiConfigurationProperties

    @Autowired
    lateinit var casConfigurationProperties: CasConfigurationProperties

    /**
     * This @Bean (or at least a single DataSource @Bean) is required for FlywayAutoConfiguration to execute,
     * even if Flyway is configured to create its own DataSource.
     * As CAS doesn't expose its DataSources as @Beans, we get the monitoring jdbc connection (in ALA
     * CAS this is retrieved from JNDI so won't actually create additional db connections)
     */
    @Bean
    @ConditionalOnMissingBean(DataSource::class)
    fun dummyDataSourceForFlywayAutoConfiguration() = JpaBeans.newDataSource(casConfigurationProperties.monitor.jdbc)

    // TODO 5.3.0+ convert to a CasTomcatEmbeddedServletContainerFactory
//    @Bean
//    @Qualifier("casServletContainerFactory")
//    fun casServletContainerFactory(): CasTomcatEmbeddedServletContainerFactory {
//        return object : CasTomcatEmbeddedServletContainerFactory(casConfigurationProperties.server.clustering) {
    @Bean
    fun casServletContainerFactory(): TomcatEmbeddedServletContainerFactory {
        return object : TomcatEmbeddedServletContainerFactory() {

            override fun getTomcatEmbeddedServletContainer(
                tomcat: Tomcat
            ): TomcatEmbeddedServletContainer {
                tomcat.enableNaming()
                return super.getTomcatEmbeddedServletContainer(tomcat)
            }
        }.apply {
            addContextCustomizers(TomcatContextCustomizer { context ->
                jndiConfigurationProperties.hikari.map { (configName, config) ->
                    ContextResource().apply {
                        name = configName
                        type = DataSource::class.java.name
                        setProperty("factory", HikariJNDIFactory::class.java.name)
                        config.driverClass?.let { setProperty("driverClassName", it) }
                        setProperty("jdbcUrl", config.url)
                        setProperty("url", config.url)
                        setProperty("username", config.user)
                        setProperty("password", config.password)
                        config.allowPoolSuspension?.let { setProperty("allowPoolSuspension", it) }
                        config.autoCommit?.let { setProperty("autoCommit", it) }
                        config.catalog?.let { setProperty("catalog", it) }
                        config.connectionInitSql?.let { setProperty("connectionInitSql", it) }
                        config.connectionTestQuery?.let { setProperty("connectionTestQuery", it) }
                        config.connectionTimeout?.let { setProperty("connectionTimeout", it) }
                        config.idleTimeout?.let { setProperty("idleTimeout", it) }
                        config.initializationFailTimeout?.let { setProperty("initializationFailTimeout", it) }
                        config.isolateInternalQueries?.let { setProperty("isolateInternalQueries", it) }
                        config.leakDetectionThreshold?.let { setProperty("leakDetectionThreshold", it) }
                        config.maximumPoolSize?.let { setProperty("maximumPoolSize", it) }
                        config.maxLifetime?.let { setProperty("maxLifetime", it) }
                        config.minimumIdle?.let { setProperty("minimumIdle", it) }
                        config.poolName?.let { setProperty("poolName", it) }
                        config.readOnly?.let { setProperty("readOnly", it) }
                        config.registerMbeans?.let { setProperty("registerMbeans", it) }
                        config.schema?.let { setProperty("schema", it) }
                        config.transactionIsolation?.let { setProperty("transactionIsolation", it) }
                        config.validationTimeout?.let { setProperty("validationTimeout", it) }
                        config.dataSourceProperties.forEach { (name, value) -> setProperty("dataSource.$name", value) }
                    }
                }.forEach(context.namingResources::addResource)
            })
        }
    }

}
