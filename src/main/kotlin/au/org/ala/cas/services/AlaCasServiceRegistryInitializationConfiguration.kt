package au.org.ala.cas.services

import au.org.ala.utils.logger
import org.apache.commons.lang3.ObjectUtils
import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.services.ServiceRegistryDao
import org.apereo.cas.services.ServicesManager
import org.apereo.cas.services.ServiceRegistryInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource

/**
 * This is [AlaCasServiceRegistryInitializationConfiguration].
 *
 * TODO This works around the oauth callback service preventing the default ALA services being
 * inserted using initFromJson.  Remove for 5.3.x
 */
@Configuration("alaCasServiceRegistryInitializationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties::class)
//@ConditionalOnMissingClass(
//    value = ["org.apereo.cas.services.JsonServiceRegistryDao", "org.apereo.cas.services.YamlServiceRegistryDao"]
//)
class AlaCasServiceRegistryInitializationConfiguration {

    companion object {
        private val LOGGER = logger<AlaCasServiceRegistryInitializationConfiguration>()
    }

    @Autowired
    lateinit var casProperties: CasConfigurationProperties

    private val serviceRegistryInitializerServicesDirectoryResource: Resource
        get() {
            val registry = casProperties.serviceRegistry.json
            return ObjectUtils.defaultIfNull(registry.location, ClassPathResource("services"))
        }

    @RefreshScope
    @Autowired
    @Bean
    fun alaServiceRegistryInitializer(
        @Qualifier("servicesManager") servicesManager: ServicesManager,
        @Qualifier("serviceRegistryDao") serviceRegistryDao: ServiceRegistryDao,
        @Qualifier("embeddedJsonServiceRegistry") embeddedJsonServiceRegistry: ServiceRegistryDao
    ): ServiceRegistryInitializer {
        val serviceRegistry = casProperties.serviceRegistry
        val initializer = AlaServiceRegistryInitializer(
            embeddedJsonServiceRegistry,
            serviceRegistryDao,
            servicesManager,
            serviceRegistry.isInitFromJson
        )

        if (serviceRegistry.isInitFromJson) {
            LOGGER.info(
                "Attempting to initialize the service registry [{}] from service definition resources found at [{}]",
                serviceRegistryDao.toString(),
                serviceRegistryInitializerServicesDirectoryResource
            )
        }
        initializer.initServiceRegistryIfNecessary()
        return initializer
    }

}
