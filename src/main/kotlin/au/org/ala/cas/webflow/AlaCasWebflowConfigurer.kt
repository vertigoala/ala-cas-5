package au.org.ala.cas.webflow

import au.org.ala.cas.delegated.AccountNotActivatedException
import au.org.ala.utils.logger
import org.apereo.cas.authentication.AuthenticationException
import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.support.pac4j.web.flow.DelegatedClientAuthenticationAction
import org.apereo.cas.ticket.AbstractTicketException
import org.apereo.cas.web.flow.CasWebflowConstants
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer
import org.springframework.context.ApplicationContext
import org.springframework.webflow.action.EventFactorySupport
import org.springframework.webflow.core.collection.LocalAttributeMap
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry
import org.springframework.webflow.engine.ActionState
import org.springframework.webflow.engine.Flow
import org.springframework.webflow.engine.FlowExecutionExceptionHandler
import org.springframework.webflow.engine.RequestControlContext
import org.springframework.webflow.engine.builder.support.FlowBuilderServices
import org.springframework.webflow.execution.FlowExecutionException
import kotlin.reflect.KClass

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

    override fun doInitialize() {
        log.info("doInitialize()")

        val inFlow = loginFlow
        val outFlow = logoutFlow

        if (inFlow != null) {
            doGenerateAuthCookieOnLoginAction(inFlow)
            createViewStates(inFlow)
            addAccountNotActivatedHandler(inFlow)
            patchDelegatedAuthAction(inFlow)
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

    private fun patchDelegatedAuthAction(flow: Flow) {
        val clientAction = flow.getState(DelegatedClientAuthenticationAction.CLIENT_ACTION) as ActionState
        createTransitionForState(clientAction, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE)
        val eventExecutingExceptionHandler = object : FlowExecutionExceptionHandler {
            val classes = setOf(AuthenticationException::class.java, AbstractTicketException::class.java)

            override fun canHandle(exception: FlowExecutionException) = findException(exception) != null

            override fun handle(exception: FlowExecutionException, context: RequestControlContext) {
                val authException = findException(exception) ?: throw IllegalStateException("$exception was not caused by any of $classes")
                val event = EventFactorySupport().event(
                    (context.currentState as? ActionState)?.actionList?.firstOrNull(),
                    CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE,
                    LocalAttributeMap(CasWebflowConstants.TRANSITION_ID_ERROR, authException)
                )
                context.handleEvent(event)
            }

            fun findException(exception: Throwable): Throwable? =
                if (classes.any { it.isInstance(exception) }) exception else exception.cause?.let { findException(it) }
        }
        clientAction.exceptionHandlerSet.add(eventExecutingExceptionHandler)
    }
}