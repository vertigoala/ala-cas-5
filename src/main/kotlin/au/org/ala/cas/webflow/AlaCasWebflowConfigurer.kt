package au.org.ala.cas.webflow

import au.org.ala.cas.delegated.AccountNotActivatedException
import au.org.ala.utils.logger
import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.web.flow.CasWebflowConstants
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer
import org.springframework.context.ApplicationContext
import org.springframework.core.Ordered
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry
import org.springframework.webflow.engine.ActionState
import org.springframework.webflow.engine.Flow
import org.springframework.webflow.engine.builder.support.FlowBuilderServices

class AlaCasWebflowConfigurer(
    flowBuilderServices: FlowBuilderServices,
    loginFlowDefinitionRegistry: FlowDefinitionRegistry,
    logoutFlowDefinitionRegistry: FlowDefinitionRegistry,
    val generateAuthCookieAction: GenerateAuthCookieAction,
    val removeAuthCookieAction: RemoveAuthCookieAction,
    applicationContext: ApplicationContext,
    casConfigurationProperties: CasConfigurationProperties
) :
    AbstractCasWebflowConfigurer(
        flowBuilderServices,
        loginFlowDefinitionRegistry,
        applicationContext,
        casConfigurationProperties
    ) {

    init {
        this.logoutFlowDefinitionRegistry = logoutFlowDefinitionRegistry
    }

    companion object {
        val log = logger()

        const val VIEW_ID_ACCOUNT_NOT_ACTIVATED = "casAccountNotActivatedView"
    }

    override fun getOrder() = Ordered.LOWEST_PRECEDENCE

    override fun doInitialize() {
        log.info("doInitialize()")

        val inFlow = loginFlow
        val outFlow = logoutFlow

        if (inFlow != null) {
            doGenerateAuthCookieOnLoginAction(inFlow)
            createViewStates(inFlow)
            addAccountNotActivatedHandler(inFlow)
        }

        if (outFlow != null) {
            doRemoveAuthCookieOnLogoutAction(outFlow)
        }
    }

    private fun doGenerateAuthCookieOnLoginAction(flow: Flow) {
        val sendTicketGrantingTicketAction =
            flow.getState(CasWebflowConstants.STATE_ID_SEND_TICKET_GRANTING_TICKET) as ActionState
        log.debug("doGenerateAuthCookieOnLoginAction {}: {}", sendTicketGrantingTicketAction.javaClass.name, sendTicketGrantingTicketAction)
        sendTicketGrantingTicketAction.exitActionList.add(generateAuthCookieAction)
    }

    private fun doRemoveAuthCookieOnLogoutAction(flow: Flow) {
        val doLogoutAction = flow.getState(CasWebflowConstants.STATE_ID_DO_LOGOUT) as ActionState
        log.debug("doRemoveAuthCookieOnLogoutAction {}: {}", doLogoutAction.javaClass.name, doLogoutAction)
        doLogoutAction.entryActionList.add(removeAuthCookieAction)
    }


    private fun createViewStates(flow: Flow) {
        createViewState(flow, VIEW_ID_ACCOUNT_NOT_ACTIVATED, VIEW_ID_ACCOUNT_NOT_ACTIVATED)
    }

    private fun addAccountNotActivatedHandler(flow: Flow) {
        val handler = flow.getState(CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE) as ActionState
        createTransitionForState(handler, AccountNotActivatedException::class.java.simpleName, VIEW_ID_ACCOUNT_NOT_ACTIVATED)
    }
}