package au.org.ala.cas.events

import au.org.ala.cas.alaUserId
import au.org.ala.utils.logger
import org.apereo.cas.authentication.UsernamePasswordCredential
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionSuccessfulEvent
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent
import org.springframework.context.event.EventListener
import org.springframework.jdbc.core.JdbcTemplate
import java.util.concurrent.ExecutorService
import javax.sql.DataSource

open class AlaCasEventListener(val dataSource: DataSource, val updateSql: String, val executorService: ExecutorService) {

    companion object {
        val log = logger()
    }

    @EventListener
    open fun handleCasAuthenticationTransactionSuccessfulEvent(casAuthenticationTransactionSuccessfulEvent: CasAuthenticationTransactionSuccessfulEvent) {
        log.debug("GOT HANDLE CAS AUTHENTICATION TRANSACTION SUCCESSFUL EVENT: {}", casAuthenticationTransactionSuccessfulEvent.credential)
    }

    @EventListener
    open fun handleCasTicketGrantingTicketCreatedEvent(casTicketGrantingTicketCreatedEvent: CasTicketGrantingTicketCreatedEvent) {
        log.debug("GOT HANDLE CAS TICKET GRANTING TICKET CREATED EVENT: {}", casTicketGrantingTicketCreatedEvent.ticketGrantingTicket?.authentication?.principal)
        val userid = casTicketGrantingTicketCreatedEvent.ticketGrantingTicket?.authentication?.alaUserId()
        if (userid != null) {
            executorService.execute {
                try {
                    val template = JdbcTemplate(dataSource)
                    template.update(updateSql, userid)
                } catch (e: Exception) {
                    log.error("Couldn't update last login time for {} using SQL {}", userid, updateSql, e)
                }
            }
        }
    }

}