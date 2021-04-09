package au.org.ala.cas.delegated

import au.org.ala.cas.AlaCasProperties
import au.org.ala.cas.jndi.HikariDatasource
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.apereo.cas.authentication.principal.PrincipalFactory
import org.apereo.cas.authentication.principal.PrincipalResolver
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils
import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.configuration.support.JpaBeans
import org.apereo.services.persondir.support.CachingPersonAttributeDaoImpl
import org.flywaydb.core.Flyway
import org.pac4j.core.client.Clients
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource
import org.springframework.boot.autoconfigure.flyway.FlywayProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import javax.annotation.PostConstruct
import javax.sql.DataSource

@Configuration
@EnableConfigurationProperties(AlaCasProperties::class, CasConfigurationProperties::class, FlywayProperties::class)
class AlaPac4jAuthenticationConfiguration {

    @Autowired
    lateinit var alaCasProperties: AlaCasProperties

    @Autowired
    lateinit var casConfigurationProperties: CasConfigurationProperties

    @Autowired
    lateinit var flywayProperties: FlywayProperties

    @Autowired
    lateinit var clients: Clients

    /**
     * This @Bean (or at least a single DataSource @Bean) is required for FlywayAutoConfiguration to execute,
     * even if Flyway is configured to create its own DataSource.
     */
    @FlywayDataSource
    @Qualifier("alaCasFlywayDataSource")
    fun alaCasFlywayDataSource(): DataSource {
        val hc = HikariConfig()
        hc.jdbcUrl = flywayProperties.url
        hc.username = flywayProperties.user
        hc.password = flywayProperties.password
        return HikariDataSource(hc)
    }

    @Bean
    @Qualifier("userCreatorDataSource")
    fun userCreatorDataSource() = JpaBeans.newDataSource(alaCasProperties.userCreator.jdbc)

    @Bean
    fun userCreatorTransactionManager() = DataSourceTransactionManager(userCreatorDataSource())

    @Bean
    fun userCreator(): UserCreator = UserCreatorALA(
        dataSource = userCreatorDataSource(),
        userCreatePassword = alaCasProperties.userCreator.userCreatePassword,
        createUserProcedure = alaCasProperties.userCreator.jdbc.createUserProcedure,
        passwordEncoder = PasswordEncoderUtils.newPasswordEncoder(alaCasProperties.userCreator.passwordEncoder)
    )

    @Bean(name = ["clientPrincipalFactory"])
    @RefreshScope
    fun clientPrincipalFactory(
        @Autowired personDirectoryPrincipalResolver: PrincipalResolver,
        @Autowired @Qualifier("cachingAttributeRepository") cachingAttributeRepository: CachingPersonAttributeDaoImpl,
        @Autowired userCreator: UserCreator
    ): PrincipalFactory = AlaPrincipalFactory(personDirectoryPrincipalResolver, cachingAttributeRepository, userCreator)

    @PostConstruct
    fun sortClients() {
        this.clients.clients.sortBy { client ->
            val idx = alaCasProperties.clientSortOrder.indexOf(client.name)
            if (idx == -1) alaCasProperties.clientSortOrder.size else idx
        }
    }

}