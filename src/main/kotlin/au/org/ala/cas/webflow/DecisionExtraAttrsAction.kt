package au.org.ala.cas.webflow

import au.org.ala.utils.logger
import org.apereo.cas.web.support.WebUtils
import org.springframework.webflow.action.AbstractAction
import org.springframework.webflow.execution.Event
import org.springframework.webflow.execution.RequestContext

class DecisionExtraAttrsAction : AbstractAction() {

    companion object {
        val log = logger()
        const val NULL_ATTRIBUTE_VALUE = "null"
    }

    override fun doExecute(context: RequestContext?): Event {

        val authentication = WebUtils.getAuthentication(context)
        if (authentication?.principal != null) {
            val principal = authentication.principal
            val attributes = principal.attributes

            // all attributes are strings, including "null" and dates
            // date format appears to be yyyy-MM-dd hh:mm:ss local time (db?  server?) eg 2018-05-30 17:07:46
            log.error("EXTRA ATTRS DECISION ATTRS {}", principal.attributes)
            val lastLogin = attributes["lastLogin"] as? String
            if (lastLogin.isNullOrBlank() || lastLogin == NULL_ATTRIBUTE_VALUE) {
                log.info("Last login for {} is blank or null: {}", attributes["email"], lastLogin)
                return yes()
            }
        }
        return no()
    }
}