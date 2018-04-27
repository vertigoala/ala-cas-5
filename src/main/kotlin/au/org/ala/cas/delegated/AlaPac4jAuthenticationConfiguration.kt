package au.org.ala.cas.delegated

import au.org.ala.cas.AlaCasProperties
import org.apereo.cas.authentication.principal.PrincipalFactory
import org.apereo.cas.authentication.principal.PrincipalResolver
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils
import org.apereo.cas.configuration.support.JpaBeans
import org.apereo.cas.web.flow.Pac4jErrorViewResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.web.ErrorViewResolver
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AlaCasProperties::class)
class AlaPac4jAuthenticationConfiguration {

    companion object {
        @Bean
        @Qualifier("pac4jErrorViewResolver")
        @JvmStatic
        fun pac4jErrorViewResolver(): ErrorViewResolver {
            return Pac4jErrorViewResolver()
        }
    }

    @Autowired
    lateinit var alaCasProperties: AlaCasProperties

    @Bean
    fun userCreator(): UserCreator = UserCreatorALA(
        dataSource = JpaBeans.newDataSource(alaCasProperties.userCreator.jdbc),
        userCreatePassword = alaCasProperties.userCreator.userCreatePassword,
        createUserProcedure = alaCasProperties.userCreator.jdbc.createUserProcedure,
        passwordEncoder = PasswordEncoderUtils.newPasswordEncoder(alaCasProperties.userCreator.passwordEncoder)
    )

    @Bean(name = ["clientPrincipalFactory"])
    @RefreshScope
    fun clientPrincipalFactory(
        @Autowired personDirectoryPrincipalResolver: PrincipalResolver,
        @Autowired userCreator: UserCreator
    ): PrincipalFactory = AlaPrincipalFactory(personDirectoryPrincipalResolver, userCreator)
}
