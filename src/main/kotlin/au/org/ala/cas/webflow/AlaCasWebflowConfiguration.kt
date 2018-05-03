package au.org.ala.cas.webflow

import au.org.ala.cas.AlaCasProperties
import au.org.ala.utils.logger
import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.ticket.registry.TicketRegistrySupport
import org.apereo.cas.web.flow.CasWebflowExecutionPlan
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry
import org.springframework.webflow.engine.builder.support.FlowBuilderServices


@Configuration("alaCasWebflowConfiguration")
@EnableConfigurationProperties(AlaCasProperties::class, CasConfigurationProperties::class)
class AlaCasWebflowConfiguration : CasWebflowExecutionPlanConfigurer {

    companion object {
        val log = logger()
    }

    @Autowired
    lateinit var alaCasProperties: AlaCasProperties

    @Autowired
    lateinit var casConfigurationProperties: CasConfigurationProperties

    @Autowired
    lateinit var applicationContext: ApplicationContext

    @Autowired
    @Qualifier("loginFlowRegistry")
    lateinit var loginFlowDefinitionRegistry: FlowDefinitionRegistry

    @Autowired
    @Qualifier("logoutFlowRegistry")
    lateinit var logoutFlowDefinitionRegistry: FlowDefinitionRegistry

    @Autowired
    lateinit var flowBuilderServices: FlowBuilderServices

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    lateinit var ticketRegistrySupport: TicketRegistrySupport

    @Bean
    @RefreshScope
    @Qualifier("alaProxyAuthenticationCookieGenerator")
    fun alaProxyAuthenticationCookieGenerator(): CookieRetrievingCookieGenerator =
        alaCasProperties.cookie.run {
            CookieRetrievingCookieGenerator(
                name,
                path,
                maxAge,
                isSecure,
                domain,
                isHttpOnly
            )
        }

    @Bean
    @RefreshScope
    fun generateAuthCookieAction(): GenerateAuthCookieAction =
        GenerateAuthCookieAction(ticketRegistrySupport, alaProxyAuthenticationCookieGenerator())

    @Bean
    @RefreshScope
    fun removeAuthCookieAction(): RemoveAuthCookieAction =
        RemoveAuthCookieAction(alaProxyAuthenticationCookieGenerator())

    @ConditionalOnMissingBean(name = ["alaAuthCookieWebflowConfigurer"])
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    fun alaAuthCookieWebflowConfigurer(): AlaCasWebflowConfigurer =
        AlaCasWebflowConfigurer(
            flowBuilderServices,
            loginFlowDefinitionRegistry,
            logoutFlowDefinitionRegistry,
            generateAuthCookieAction(),
            removeAuthCookieAction(),
            applicationContext,
            casConfigurationProperties
        )

    override fun configureWebflowExecutionPlan(plan: CasWebflowExecutionPlan) {
        log.debug("Registering alaAuthCookieWebflowConfigurer")
        plan.registerWebflowConfigurer(alaAuthCookieWebflowConfigurer())
    }

}

