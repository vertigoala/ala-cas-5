package au.org.ala.cas.tickets

import au.org.ala.utils.logger
import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.logout.LogoutManager
import org.apereo.cas.ticket.registry.NoOpTicketRegistryCleaner
import org.apereo.cas.ticket.registry.TicketRegistry
import org.apereo.cas.ticket.registry.TicketRegistryCleaner
import org.apereo.cas.ticket.registry.support.LockingStrategy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration("alaTicketCleanerConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties::class)
class AlaTicketCleanerConfiguration {

    companion object {
        val log = logger()
    }

    @Autowired
    lateinit var casProperties: CasConfigurationProperties

    @Autowired
    @Qualifier("ticketRegistry")
    lateinit var ticketRegistry: TicketRegistry

    @Bean("oauthTgtDeleteVetoer")
    fun oAuthTgtDeleteVetoer(): TgtDeleteVetoer {
        return OAuthTgtDeleteVetoer(ticketRegistry)
    }

    @Autowired
    @Bean("ticketRegistryCleaner")
    fun ticketRegistryCleaner(
        @Qualifier("lockingStrategy") lockingStrategy: LockingStrategy,
        @Qualifier("logoutManager") logoutManager: LogoutManager,
        tgtDeleteVetoer: Optional<List<TgtDeleteVetoer>>
    ): TicketRegistryCleaner {
        return if (casProperties.ticket.registry.cleaner.schedule.isEnabled) {
            log.debug("ALA Ticket registry cleaner is enabled.")
            AlaTicketRegistryCleaner(
                lockingStrategy,
                logoutManager,
                ticketRegistry,
                tgtDeleteVetoer.orElse(emptyList())
            )
        } else {
            log.debug(
                """
                Ticket registry cleaner is not enabled.
                Expired tickets are not forcefully collected and cleaned by CAS. It is up to the ticket registry itself to
                clean up tickets based on expiration and eviction policies.
                """.trimIndent()
            )
            NoOpTicketRegistryCleaner.getInstance()
        }
    }
}