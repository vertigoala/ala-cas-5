package au.org.ala.cas.tickets

import au.org.ala.utils.logger
import org.apereo.cas.logout.LogoutManager
import org.apereo.cas.ticket.AbstractTicket
import org.apereo.cas.ticket.ServiceTicket
import org.apereo.cas.ticket.Ticket
import org.apereo.cas.ticket.TicketGrantingTicket
import org.apereo.cas.ticket.code.OAuthCodeImpl
import org.apereo.cas.ticket.registry.DefaultTicketRegistryCleaner
import org.apereo.cas.ticket.registry.TicketRegistry
import org.apereo.cas.ticket.registry.support.LockingStrategy

class AlaTicketRegistryCleaner(
    lockingStrategy: LockingStrategy,
    private val logoutManager: LogoutManager,
    private val ticketRegistry: TicketRegistry,
    private val tgtDeleteVetoers: List<TgtDeleteVetoer> = emptyList()
) : DefaultTicketRegistryCleaner(lockingStrategy, logoutManager, ticketRegistry) {

    companion object {
        val log = logger()
    }

    override fun cleanTicket(ticket: Ticket?) =
        when (ticket) {
            is TicketGrantingTicket -> {
                log.debug("Cleaning up expired ticket-granting ticket [{}]", ticket.id)
                this.logoutManager.performLogout(ticket)
                if (tgtDeleteVetoers.all { it.canDeleteTgt(ticket) }) {
                    ticketRegistry.deleteTicket(ticket.id)
                } else {
                    log.error("Clean up expired ticket-granting ticket [{}] was vetoed", ticket)
                    0
                }
            }
            is ServiceTicket -> {
                log.debug("Cleaning up expired service ticket [{}]", ticket.id)
                ticketRegistry.deleteTicket(ticket.id)
            }
            else -> {
                log.warn("Unknown ticket type [{}] found to clean", ticket?.javaClass?.simpleName)
                0
            }
        }

}

interface TgtDeleteVetoer {

    fun canDeleteTgt(tgt: TicketGrantingTicket): Boolean
}

class OAuthTgtDeleteVetoer(val registry: TicketRegistry) : TgtDeleteVetoer {
    override fun canDeleteTgt(tgt: TicketGrantingTicket) =
        tgt.descendantTickets.map(registry::getTicket).all { ticket ->
            when (ticket) {
                is OAuthCodeImpl -> ticket.expirationPolicy.isExpired(ticket)
                else -> ticket?.isExpired ?: true
            }
        }
}
