package au.org.ala.cas.thymeleaf

import au.org.ala.cas.AlaCasProperties
import org.apereo.cas.services.web.ThemeViewResolver
import org.apereo.cas.services.web.ThemeViewResolverFactory
import org.apereo.cas.services.web.config.CasThemesConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.util.MimeType
import org.thymeleaf.spring4.SpringTemplateEngine
import org.thymeleaf.spring4.view.ThymeleafViewResolver
import java.util.LinkedHashMap

/**
 * Inject all skin properties into all Thymeleaf templates as static variables
 */
@Configuration("alaCasThemesConfiguration")
@EnableConfigurationProperties(AlaCasProperties::class)
class AlaCasThemesConfiguration {

    companion object {
        const val BASE_URL = "baseUrl"
        const val TERMS_URL = "termsUrl"
        const val HEADER_FOOTER_URL = "headerFooterUrl"
        const val FAVION_BASE_URL = "favIconBaseUrl"
        const val BIE_BASE_URL = "bieBaseUrl"
        const val BIE_SEARCH_PATH = "bieSearchPath"
        const val ORG_SHORT_NAME = "orgShortName"
        const val ORG_LONG_NAME = "orgLongName"
        const val ORG_NAME_KEY = "orgNameKey"
        const val USERDETAILS_BASE_URL = "userDetailsUrl"

        @JvmStatic
        @Bean
        @Qualifier("thymeleafViewResolverPostProcessor")
        fun thymeleafViewResolverPostProcessor(alaCasProperties: AlaCasProperties): BeanPostProcessor {
            return object : BeanPostProcessor {
                override fun postProcessBeforeInitialization(bean: Any?, beanName: String?) = bean

                override fun postProcessAfterInitialization(bean: Any?, beanName: String?): Any? {
                    if (beanName == "thymeleafViewResolver" && bean is ThymeleafViewResolver) {
                        configureThymeleafViewResolver(bean, alaCasProperties)
                    }
                    return bean
                }

            }
        }

        fun configureThymeleafViewResolver(thymeleafViewResolver: ThymeleafViewResolver, alaCasProperties: AlaCasProperties) {
            mapOf(
                BASE_URL to alaCasProperties.skin.baseUrl,
                TERMS_URL to alaCasProperties.skin.termsUrl,
                HEADER_FOOTER_URL to alaCasProperties.skin.headerFooterUrl,
                FAVION_BASE_URL to alaCasProperties.skin.favIconBaseUrl,
                BIE_BASE_URL to alaCasProperties.skin.bieBaseUrl,
                BIE_SEARCH_PATH to alaCasProperties.skin.bieSearchPath,
                ORG_SHORT_NAME to alaCasProperties.skin.orgShortName,
                ORG_LONG_NAME to alaCasProperties.skin.orgLongName,
                ORG_NAME_KEY to alaCasProperties.skin.orgNameKey,
                USERDETAILS_BASE_URL to alaCasProperties.skin.userDetailsUrl
            ).forEach(thymeleafViewResolver::addStaticVariable)
        }
    }

}