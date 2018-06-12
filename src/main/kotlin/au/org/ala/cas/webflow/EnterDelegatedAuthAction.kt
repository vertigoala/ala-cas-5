package au.org.ala.cas.webflow

import au.org.ala.cas.AlaCasProperties
import org.apereo.cas.web.support.WebUtils
import org.springframework.webflow.action.AbstractAction
import org.springframework.webflow.execution.Event
import org.springframework.webflow.execution.RequestContext

class EnterDelegatedAuthAction(val alaCasProperties: AlaCasProperties) : AbstractAction() {
    override fun doExecute(context: RequestContext): Event {
        val authentication = WebUtils.getAuthentication(context)
        val extraAttrs = ExtraAttrs.fromMap(authentication.principal.attributes)
        if (extraAttrs.country.isBlank()) extraAttrs.country = alaCasProperties.userCreator.defaultCountry
        context.flowScope.put(SaveExtraAttrsAction.EXTRA_ATTRS_FLOW_VAR, extraAttrs)
        return success()
    }

}