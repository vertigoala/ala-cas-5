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
    open fun userCreator(): UserCreator = UserCreatorALA(
            dataSource = Beans.newDataSource(alaCasProperties.userCreator.jdbc),
            userCreatePassword = alaCasProperties.userCreator.userCreatePassword,
            createUserProcedure = alaCasProperties.userCreator.jdbc.createUserProcedure,
            passwordEncoder = Beans.newPasswordEncoder(alaCasProperties.userCreator.passwordEncoder)
    )

    @Bean(name = arrayOf("clientPrincipalFactory"))
    open fun clientPrincipalFactory(
            @Autowired personDirectoryPrincipalResolver: PrincipalResolver,
            @Autowired userCreator: UserCreator
    ): PrincipalFactory = AlaPrincipalFactory(personDirectoryPrincipalResolver, userCreator)
}
