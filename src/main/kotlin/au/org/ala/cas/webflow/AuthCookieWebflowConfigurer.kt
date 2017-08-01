package au.org.ala.cas.webflow

import au.org.ala.utils.logger
import org.apereo.cas.authentication.principal.Response
import org.apereo.cas.web.flow.CasWebflowConstants
import org.apereo.cas.web.flow.DefaultWebflowConfigurer
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry
import org.springframework.webflow.engine.Flow
import org.springframework.webflow.engine.builder.support.FlowBuilderServices

class AuthCookieWebflowConfigurer(
        flowBuilderServices: FlowBuilderServices,
        loginFlowDefinitionRegistry: FlowDefinitionRegistry) :
        DefaultWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry) {

    companion object {
        val log = logger<AuthCookieWebflowConfigurer>()
    }

    override fun doInitialize() {
        log.info("doInitialize()")
//        createRedirectToServiceActionState(loginFlow)
    }

    /**
     * Create redirect to service action state.

     * @param flow the flow
     */
    override protected fun createRedirectToServiceActionState(flow: Flow) {
        val redirectToView = createActionState(flow,
                CasWebflowConstants.STATE_ID_REDIRECT,
                createEvaluateAction("redirectToServiceAction"))
        createTransitionForState(redirectToView, Response.ResponseType.POST.name.toLowerCase(), CasWebflowConstants.STATE_ID_POST_VIEW)
        createTransitionForState(redirectToView, Response.ResponseType.REDIRECT.name.toLowerCase(), CasWebflowConstants.STATE_ID_REDIR_VIEW)
    }

}