package au.org.ala.cas.services

import au.org.ala.utils.logger
import org.apereo.cas.services.RegisteredService
import org.apereo.cas.services.ServiceRegistryDao
import org.apereo.cas.services.ServiceRegistryInitializer
import org.apereo.cas.services.ServicesManager

/**
 * Work around service registration issues that do a regex match
 */
class AlaServiceRegistryInitializer(
    val jsonServiceRegistryDao: ServiceRegistryDao,
    val serviceRegistryDao: ServiceRegistryDao,
    val servicesManager: ServicesManager,
    val initFromJson: Boolean
) : ServiceRegistryInitializer(
    jsonServiceRegistryDao, serviceRegistryDao, servicesManager, initFromJson
) {

    companion object {
        val log = logger()
    }

    override fun initServiceRegistryIfNecessary() {
        val size = this.serviceRegistryDao.size()
        log.debug("Service registry contains [{}] service definitions", size)

        if (!this.initFromJson) {
            log.info(
                "The service registry database backed by [{}] will not be initialized from JSON services. "
                        + "If the service registry database ends up empty, CAS will refuse to authenticate services "
                        + "until service definitions are added to the registry. To auto-initialize the service registry, "
                        + "set 'cas.serviceRegistry.initFromJson=true' in your CAS settings.",
                this.serviceRegistryDao.name
            )
            return
        }

        log.warn(
            ("Service registry [{}] will be auto-initialized from JSON service definitions. "
                    + "This behavior is only useful for testing purposes and MAY NOT be appropriate for production. "
                    + "Consider turning off this behavior via the setting [cas.serviceRegistry.initFromJson=false] "
                    + "and explicitly register definitions in the services registry."), this.serviceRegistryDao.name
        )

        val servicesLoaded = this.jsonServiceRegistryDao.load()
        log.debug("Loading JSON services are [{}]", servicesLoaded)

        for (r in servicesLoaded) {
            if (findExistingMatchForService(r)) {
                continue
            }
            log.debug("Initializing service registry with the [{}] JSON service definition...", r)
            this.serviceRegistryDao.save(r)
        }
        this.servicesManager.load()
        log.info(
            "Service registry [{}] contains [{}] service definitions",
            this.serviceRegistryDao.name,
            this.servicesManager.count()
        )

    }

    private fun findExistingMatchForService(r: RegisteredService): Boolean {
        var match = this.serviceRegistryDao.findServiceByExactServiceId(r.serviceId)
        if (match != null) {
            log.warn(
                "Skipping [{}] JSON service definition as a matching service [{}] is found in the registry",
                r.name,
                match.name
            )
            return true
        }
        match = this.serviceRegistryDao.findServiceById(r.id)
        if (match != null) {
            log.warn(
                "Skipping [{}] JSON service definition as a matching id [{}] is found in the registry",
                r.name,
                match.id
            )
            return true
        }
        return false
    }
}