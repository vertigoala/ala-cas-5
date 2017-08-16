package au.org.ala.cas.thymeleaf

import au.org.ala.cas.AlaCasProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AlaCasProperties::class)
open class AlaThymeleafConfiguration {

    @Autowired
    lateinit var alaCasProperties: AlaCasProperties

    @Bean
    open fun alaTemplateClient() = AlaTemplateClient(alaCasProperties.skin, alaCasProperties.cookie.name)

    @Bean
    open fun alaDialect(alaTemplateClient: AlaTemplateClient) = AlaDialect(alaTemplateClient)

//    @Autowired
//    lateinit var templateEngine: SpringTemplateEngine
//
//    @PostConstruct
//    fun extension() {
//        templateEngine.addDialect(alaDialect(alaTemplateClient()))
//        val resolver = UrlTemplateResolver()
//        resolver.prefix = alaCasProperties.skin.headerFooterUrl ?: "https://www.ala.org.au/commonui"
//        resolver.suffix = ".html"
//        resolver.setTemplateMode("HTML5")
//        resolver.order = templateEngine.templateResolvers.size
//        resolver.isCacheable = true
//        templateEngine.addTemplateResolver(resolver)
//    }
}