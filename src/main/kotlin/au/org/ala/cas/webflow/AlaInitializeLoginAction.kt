package au.org.ala.cas.webflow

import au.org.ala.cas.AlaCasProperties
import org.springframework.webflow.action.AbstractAction
import org.springframework.webflow.execution.Event
import org.springframework.webflow.execution.RequestContext

/**
 * Add config variables to flow scope at the start of a flow.  Added as a flow start action.
 */
open class AlaInitializeLoginAction(val alaCasProperties: AlaCasProperties) : AbstractAction() {

    companion object {
        const val BASE_URL = "baseUrl"
        const val HEADER_FOOTER_URL = "headerFooterUrl"
        const val FAVION_BASE_URL = "favIconBaseUrl"
        const val BIE_BASE_URL = "bieBaseUrl"
        const val BIE_SEARCH_PATH = "bieSearchPath"
        const val ORG_SHORT_NAME = "orgShortName"
        const val ORG_LONG_NAME = "orgLongName"
        const val ORG_NAME_KEY = "orgNameKey"
        const val USERDETAILS_BASE_URL = "userDetailsUrl"
    }

    override fun doExecute(requestContext: RequestContext): Event {
        requestContext.flowScope.put(BASE_URL, alaCasProperties.skin.baseUrl)
        requestContext.flowScope.put(HEADER_FOOTER_URL, alaCasProperties.skin.headerFooterUrl)
        requestContext.flowScope.put(FAVION_BASE_URL, alaCasProperties.skin.favIconBaseUrl)
        requestContext.flowScope.put(BIE_BASE_URL, alaCasProperties.skin.bieBaseUrl)
        requestContext.flowScope.put(BIE_SEARCH_PATH, alaCasProperties.skin.bieSearchPath)
        requestContext.flowScope.put(ORG_SHORT_NAME, alaCasProperties.skin.orgShortName)
        requestContext.flowScope.put(ORG_LONG_NAME, alaCasProperties.skin.orgLongName)
        requestContext.flowScope.put(ORG_NAME_KEY, alaCasProperties.skin.orgNameKey)
        requestContext.flowScope.put(USERDETAILS_BASE_URL, alaCasProperties.skin.userDetailsUrl)
        return success()
    }
}