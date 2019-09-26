package au.org.ala.cas.webflow

import au.org.ala.utils.logger
import org.apache.commons.lang3.StringUtils
import org.apereo.cas.CasProtocolConstants
import org.apereo.cas.authentication.principal.ServiceFactory
import org.apereo.cas.authentication.principal.WebApplicationService
import org.apereo.cas.configuration.model.core.logout.LogoutProperties
import org.apereo.cas.logout.LogoutRequestStatus
import org.apereo.cas.services.ServicesManager
import org.apereo.cas.web.flow.CasWebflowConstants
import org.apereo.cas.web.flow.logout.AbstractLogoutAction
import org.apereo.cas.web.support.WebUtils
import org.springframework.webflow.execution.Event
import org.springframework.webflow.execution.RequestContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Copy of the CAS [org.apereo.cas.web.flow.actions.LogoutAction] that checks the default logout redirect parameter if the configured redirect parameter
 * is empty.
 *
 * TODO Ensure this is up to date when upgrading the base CAS version.
 */
class AlaLogoutAction(
        private val webApplicationServiceFactory: ServiceFactory<WebApplicationService>,
        private val servicesManager: ServicesManager,
        private val logoutProperties: LogoutProperties) : AbstractLogoutAction() {

    companion object {
        val LOGGER = logger()
    }

    override fun doInternalExecute(request: HttpServletRequest, response: HttpServletResponse, context: RequestContext): Event {
        var needFrontSlo = false
        val logoutRequests = WebUtils.getLogoutRequests(context)
        if (logoutRequests != null) {
            needFrontSlo = logoutRequests
                    .stream()
                    .anyMatch { logoutRequest -> logoutRequest.status == LogoutRequestStatus.NOT_ATTEMPTED }
        }

//        val paramName = StringUtils.defaultIfEmpty(logoutProperties.redirectParameter, CasProtocolConstants.PARAMETER_SERVICE) // CHANGED TO
        val paramNames = sequenceOf(logoutProperties.redirectParameter, CasProtocolConstants.PARAMETER_SERVICE)
        LOGGER.debug("Using parameter names [{}] to detect destination service, if any", paramNames)
//        val service = request.getParameter(paramName) // CHANGED TO
        val service = paramNames.map(request::getParameter).firstOrNull { !it.isNullOrBlank() }
        LOGGER.debug("Located target service [{}] for redirection after logout", service)

        if (logoutProperties.isFollowServiceRedirects && StringUtils.isNotBlank(service)) {
            val webAppService = webApplicationServiceFactory.createService(service)
            val rService = this.servicesManager.findServiceBy(webAppService)

            if (rService != null && rService.getAccessStrategy().isServiceAccessAllowed()) {
                LOGGER.debug("Redirecting to service [{}]", service)
                WebUtils.putLogoutRedirectUrl(context, service)
            } else {
                LOGGER.warn("Cannot redirect to [{}] given the service is unauthorized to use CAS. " + "Ensure the service is registered with CAS and is enabled to allowed access", service)
            }
        } else {
            LOGGER.debug("No target service is located for redirection after logout, or CAS is not allowed to follow redirects after logout")
        }

        // there are some front services to logout, perform front SLO
        if (needFrontSlo) {
            LOGGER.debug("Proceeding forward with front-channel single logout")
            return Event(this, CasWebflowConstants.TRANSITION_ID_FRONT)
        }
        LOGGER.debug("Moving forward to finish the logout process")
        return Event(this, CasWebflowConstants.TRANSITION_ID_FINISH)
    }
}