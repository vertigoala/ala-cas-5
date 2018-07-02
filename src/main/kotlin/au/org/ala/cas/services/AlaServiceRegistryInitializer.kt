package au.org.ala.cas.services

import au.org.ala.utils.logger
import org.apereo.cas.services.RegisteredService
import org.apereo.cas.services.ServiceRegistry
import org.apereo.cas.services.ServiceRegistryInitializer
import org.apereo.cas.services.ServicesManager

/**
 * Work around service registration issues that do a regex match
 */
class AlaServiceRegistryInitializer(
    val jsonServiceRegistry: ServiceRegistry,
    val serviceRegistry: ServiceRegistry,
    val servicesManager: ServicesManager
) : ServiceRegistryInitializer(
    jsonServiceRegistry, serviceRegistry, servicesManager
) {

    companion object {
        val log = logger()
    }

    override fun initServiceRegistryIfNecessary() {
        val size = this.serviceRegistry.size()
        log.debug("Service registry contains [{}] service definition(s)", size)

        log.warn("Service registry [{}] will be auto-initialized from JSON service definitions. "
                + "This behavior is only useful for testing purposes and MAY NOT be appropriate for production. "
                + "Consider turning off this behavior via the setting [cas.serviceRegistry.initFromJson=false] "
                + "and explicitly register definitions in the services registry.", this.serviceRegistry.name)

        val servicesLoaded = this.jsonServiceRegistry.load()
        log.debug("Loading JSON services are [{}]", servicesLoaded)

        servicesLoaded.stream()
            .filter { s -> !findExistingMatchForService(s) }
            .forEach { r ->
                log.debug("Initializing service registry with the [{}] JSON service definition...", r)
                this.serviceRegistry.save(r)
            }
        this.servicesManager.load()
        log.info("Service registry [{}] contains [{}] service definitions", this.serviceRegistry.name, this.servicesManager.count())

    }

    private fun findExistingMatchForService(r: RegisteredService): Boolean {
        var match = this.serviceRegistry.findServiceByExactServiceId(r.serviceId)
        if (match != null) {
            log.warn("Skipping [{}] JSON service definition as a matching service [{}] is found in the registry", r.name, match.name)
            return true
        }
        match = this.serviceRegistry.findServiceById(r.id)
        if (match != null) {
            log.warn("Skipping [{}] JSON service definition as a matching id [{}] is found in the registry", r.name, match.id)
            return true
        }
        return false
    }
}