package au.org.ala.cas.delegated

import au.org.ala.cas.AlaCasProperties
import org.apereo.cas.authentication.principal.PrincipalFactory
import org.apereo.cas.authentication.principal.PrincipalResolver
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils
import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.configuration.support.JpaBeans
import org.apereo.services.persondir.support.CachingPersonAttributeDaoImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import javax.sql.DataSource

@Configuration
@EnableConfigurationProperties(AlaCasProperties::class, CasConfigurationProperties::class)
class AlaPac4jAuthenticationConfiguration {

    @Autowired
    lateinit var alaCasProperties: AlaCasProperties

    @Autowired
    lateinit var casConfigurationProperties: CasConfigurationProperties

    /**
     * This @Bean (or at least a single DataSource @Bean) is required for FlywayAutoConfiguration to execute,
     * even if Flyway is configured to create its own DataSource.
     * As CAS doesn't expose its DataSources as @Beans, we get the monitoring jdbc connection (in ALA
     * CAS this is retrieved from JNDI so won't actually create additional db connections)
     */
    @Bean
    @FlywayDataSource
    @Qualifier("userCreatorDataSource")
    fun userCreatorDataSource() = JpaBeans.newDataSource(casConfigurationProperties.monitor.jdbc)

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

}