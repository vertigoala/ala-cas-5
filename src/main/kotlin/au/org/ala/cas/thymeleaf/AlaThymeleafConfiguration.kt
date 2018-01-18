package au.org.ala.cas.thymeleaf

import au.org.ala.cas.AlaCasProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AlaCasProperties::class)
class AlaThymeleafConfiguration {

    @Autowired
    lateinit var alaCasProperties: AlaCasProperties

    @Bean
    fun alaTemplateClient() = AlaTemplateClient(alaCasProperties.skin, alaCasProperties.cookie.name)

    @Bean
    fun alaDialect(alaTemplateClient: AlaTemplateClient) = AlaDialect(alaTemplateClient)

}