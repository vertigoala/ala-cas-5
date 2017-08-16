package au.org.ala.cas.delegated;

import au.org.ala.cas.AlaCasProperties
import au.org.ala.utils.logger
import org.apereo.cas.authentication.principal.PrincipalFactory
import org.apereo.cas.authentication.principal.PrincipalResolver
import org.apereo.cas.configuration.support.Beans
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AlaCasProperties::class)
open class AlaPac4jAuthenticationConfiguration {

    companion object {
        private val logger = logger<AlaPac4jAuthenticationConfiguration>()
    }

    @Autowired
    lateinit var alaCasProperties: AlaCasProperties

    @Bean
    open fun userCreator(): UserCreator = UserCreatorALA().apply {
        logger.debug("Creating UserCreatorALA with {}, {}, {}, {}", alaCasProperties.userCreator.jdbc.dataSourceName, alaCasProperties.userCreator.userCreatePassword, alaCasProperties.userCreator.jdbc.createUserProcedure)
        dataSource = Beans.newDataSource(alaCasProperties.userCreator.jdbc)
        passwordEncoder = Beans.newPasswordEncoder(alaCasProperties.userCreator.passwordEncoder)
        userCreatePassword = alaCasProperties.userCreator.userCreatePassword
        createUserProcedure = alaCasProperties.userCreator.jdbc.createUserProcedure
    }

    @Bean(name = arrayOf("clientPrincipalFactory"))
    open fun clientPrincipalFactory(
            @Autowired personDirectoryPrincipalResolver: PrincipalResolver,
            @Autowired userCreator: UserCreator
    ): PrincipalFactory = AlaPrincipalFactory(personDirectoryPrincipalResolver, userCreator)
}
