package au.org.ala.cas.events

import au.org.ala.cas.AlaCasProperties
import org.apereo.cas.configuration.support.JpaBeans
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Configuration
@EnableConfigurationProperties(AlaCasProperties::class)
class AlaEventsConfiguration {

    @Autowired
    lateinit var alaCasProperties: AlaCasProperties

    @Bean
    fun lastLoginExecutor(): ExecutorService = Executors.newSingleThreadExecutor()

    @Bean
    @ConditionalOnProperty(prefix="ala.userCreator.jdbc", name=["enableUpdateLastLoginTime"])
    fun alaCasEventListener(): AlaCasEventListener {
        return AlaCasEventListener(JpaBeans.newDataSource(alaCasProperties.userCreator.jdbc), alaCasProperties.userCreator.jdbc.updateLastLoginTimeSql, lastLoginExecutor())
    }
}

