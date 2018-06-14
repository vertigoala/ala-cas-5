package au.org.ala.cas.webflow

import au.org.ala.utils.logger
import org.apache.commons.lang3.StringUtils
import org.springframework.webflow.action.AbstractAction
import org.springframework.webflow.execution.Event
import org.springframework.webflow.execution.RequestContext
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator
import org.apereo.cas.web.support.WebUtils
import org.springframework.webflow.execution.FlowSession
// TODO remove once CAS terminateSessionAction in login-flow.xml is fixed.
class LoginTerminateSessionAction(val ticketGrantingTicketCookieGenerator: CookieRetrievingCookieGenerator) : AbstractAction() {

    companion object {
        val log = logger()
    }

    override fun doExecute(context: RequestContext): Event {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context)
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context)

        var tgtId = WebUtils.getTicketGrantingTicketId(context)
        if (StringUtils.isBlank(tgtId)) {
            tgtId = this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request)
        }
        if (StringUtils.isNotBlank(tgtId)) {
            log.debug("Destroying SSO session linked to ticket-granting ticket [{}]", tgtId)
            removeTicketGrantingTicketInScopes(context)
//            val logoutRequests = this.centralAuthenticationService.destroyTicketGrantingTicket(tgtId)
//            WebUtils.putLogoutRequests(context, logoutRequests)
        }
        log.debug("Removing CAS cookies")
        this.ticketGrantingTicketCookieGenerator.removeCookie(response)
//        this.warnCookieGenerator.removeCookie(response)

//        destroyApplicationSession(request, response)
        log.debug("Terminated all CAS sessions successfully.")

//        if (StringUtils.isNotBlank(logoutProperties.getRedirectUrl())) {
//            WebUtils.putLogoutRedirectUrl(context, logoutProperties.getRedirectUrl())
//            return this.eventFactorySupport.event(this, CasWebflowConstants.STATE_ID_REDIRECT)
//        }

        return this.eventFactorySupport.success(this)
    }


    /**
     * Put ticket granting ticket in request and flow scopes.
     *
     * @param context     the context
     * @param ticketValue the ticket value
     */
    fun removeTicketGrantingTicketInScopes(context: RequestContext) {
        context.requestScope.remove(WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID)
        context.flowScope.remove(WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID)
        var session: FlowSession? = context.flowExecutionContext.activeSession.parent
        while (session != null) {
            session.scope.remove(WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID)
            session = session.parent
        }
    }
}
