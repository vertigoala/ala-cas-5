package au.org.ala.cas.thymeleaf

import au.org.ala.cas.AlaCasProperties
import org.apereo.cas.services.web.RegisteredServiceThemeBasedViewResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.ViewResolver
import javax.annotation.PostConstruct

@Configuration
@EnableConfigurationProperties(AlaCasProperties::class)
open class AlaStaticThymeleafPropertiesConfiguration {

    companion object {
        const val BASE_URL = "baseUrl"
        const val HEADER_FOOTER_URL = "headerFooterUrl"
        const val FAVION_BASE_URL = "favIconBaseUrl"
        const val BIE_BASE_URL = "bieBaseUrl"
        const val BIE_SEARCH_PATH = "bieSearchPath"
        const val ORG_SHORT_NAME = "orgShortName"
        const val ORG_LONG_NAME = "orgLongName"
        const val ORG_NAME_KEY = "orgNameKey"
        const val USERDETAILS_BASE_URL = "userDetailsUrl"
    }

    @Autowired
    lateinit var alaCasProperties: AlaCasProperties

    @Autowired
    lateinit var registeredServiceViewResolver: ViewResolver

    @PostConstruct
    open fun addStaticVariables() {
        (registeredServiceViewResolver as? RegisteredServiceThemeBasedViewResolver)?.run {
            mapOf(BASE_URL to  alaCasProperties.skin.baseUrl,
                    HEADER_FOOTER_URL to alaCasProperties.skin.headerFooterUrl,
                    FAVION_BASE_URL to alaCasProperties.skin.favIconBaseUrl,
                    BIE_BASE_URL to alaCasProperties.skin.bieBaseUrl,
                    BIE_SEARCH_PATH to alaCasProperties.skin.bieSearchPath,
                    ORG_SHORT_NAME to alaCasProperties.skin.orgShortName,
                    ORG_LONG_NAME to alaCasProperties.skin.orgLongName,
                    ORG_NAME_KEY to alaCasProperties.skin.orgNameKey,
                    USERDETAILS_BASE_URL to alaCasProperties.skin.userDetailsUrl
            ).forEach { (k, v) -> addStaticVariable(k, v) }
        }
    }
}