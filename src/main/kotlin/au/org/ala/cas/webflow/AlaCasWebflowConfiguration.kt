package au.org.ala.cas.webflow

import au.org.ala.cas.AlaCasProperties
import au.org.ala.cas.webflow.SaveExtraAttrsAction.Companion.EXTRA_ATTRS_FLOW_VAR
import au.org.ala.utils.logger
import au.org.ala.utils.urlParameterSafe
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.benmanes.caffeine.cache.Caffeine
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils
import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.ticket.registry.TicketRegistrySupport
import org.apereo.cas.web.flow.CasWebflowExecutionPlan
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator
import org.apereo.cas.web.support.WebUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.webflow.action.AbstractAction
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry
import org.springframework.webflow.engine.builder.support.FlowBuilderServices
import org.springframework.webflow.execution.Action
import org.springframework.webflow.execution.Event
import org.springframework.webflow.execution.RequestContext
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.sql.DataSource


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

    @Autowired
    @Qualifier("userCreatorDataSource")
    lateinit var userCreatorDataSource: DataSource

    @Autowired
    @Qualifier("userCreatorTransactionManager")
    lateinit var userCreatorTransactionManager: DataSourceTransactionManager

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
    fun generateAuthCookieAction(): GenerateAuthCookieAction =
        GenerateAuthCookieAction(ticketRegistrySupport, alaProxyAuthenticationCookieGenerator())

    @Bean
    fun removeAuthCookieAction(): RemoveAuthCookieAction =
        RemoveAuthCookieAction(alaProxyAuthenticationCookieGenerator())

    @Bean
    @Qualifier(AlaCasWebflowConfigurer.ACTION_ENTER_DELEGATED_AUTH_EXTRA_ATTRS)
    fun enterDelegatedAuthAction() : Action {
        return object : AbstractAction() {
            override fun doExecute(context: RequestContext): Event {
                val authentication = WebUtils.getAuthentication(context)
                val extraAttrs = ExtraAttrs.fromMap(authentication.principal.attributes)
                if (extraAttrs.country.isBlank()) extraAttrs.country = alaCasProperties.userCreator.defaultCountry
                context.flowScope.put(SaveExtraAttrsAction.EXTRA_ATTRS_FLOW_VAR, extraAttrs)
                return success()
            }

        }
    }

    @Bean
    @Qualifier(AlaCasWebflowConfigurer.ACTION_RENDER_DELEGATED_AUTH_EXTRA_ATTRS)
    fun renderDelegatedAuthAction(): Action {
        val typeReference = TypeFactory.defaultInstance().constructCollectionType(List::class.java, CodedName::class.java)
        val mapper = jacksonObjectMapper()
        return object: AbstractAction() {
            val COUNTRIES = "countries"
            val stateCache = Caffeine.newBuilder().refreshAfterWrite(1, TimeUnit.HOURS).build { country: String ->
                val url = if (country == COUNTRIES) {
                    URL(alaCasProperties.userCreator.countriesListUrl)
                } else {
                    URL("${alaCasProperties.userCreator.statesListUrl}?country=${country.urlParameterSafe()}")
                }
                return@build mapper.readValue<List<CodedName>>(url.openConnection().apply { this.setRequestProperty("Accept", "application/json") }.getInputStream().reader(Charsets.UTF_8), typeReference)
            }

            override fun doExecute(context: RequestContext): Event {
                context.viewScope.put("countries", stateCache[COUNTRIES])
                context.viewScope.put("states", stateCache[(context.flowScope[EXTRA_ATTRS_FLOW_VAR] as ExtraAttrs).country])
                return success()
            }

        }
    }

    @Bean
    @Qualifier(AlaCasWebflowConfigurer.ACTION_UPDATE_PASSWORD)
    fun updatePasswordAction() = UpdatePasswordAction(
        alaCasProperties, PasswordEncoderUtils.newPasswordEncoder(alaCasProperties.userCreator.passwordEncoder),
        userCreatorDataSource, userCreatorTransactionManager
    )

    @Bean
    @Qualifier(AlaCasWebflowConfigurer.DECISION_ID_EXTRA_ATTRS)
    fun decisionExtraAttrsAction() = DecisionExtraAttrsAction()

    @Bean
    @Qualifier(AlaCasWebflowConfigurer.STATE_ID_SAVE_EXTRA_ATTRS_ACTION)
    fun saveExtraAttrsAction() = SaveExtraAttrsAction(alaCasProperties, userCreatorDataSource, userCreatorTransactionManager)

    @ConditionalOnMissingBean(name = ["alaAuthCookieWebflowConfigurer"])
    @Bean
    @DependsOn("defaultWebflowConfigurer", "delegatedAuthenticationWebflowConfigurer")
    fun alaAuthCookieWebflowConfigurer(): AlaCasWebflowConfigurer =
        AlaCasWebflowConfigurer(
            flowBuilderServices,
            loginFlowDefinitionRegistry,
            logoutFlowDefinitionRegistry,
            generateAuthCookieAction(),
            removeAuthCookieAction(),
            applicationContext,
            alaCasProperties,
            casConfigurationProperties
        )

    override fun configureWebflowExecutionPlan(plan: CasWebflowExecutionPlan) {
        log.debug("Registering alaAuthCookieWebflowConfigurer")
        plan.registerWebflowConfigurer(alaAuthCookieWebflowConfigurer())
    }

}


data class CodedName @JsonCreator constructor(
    @param:JsonProperty val isoCode: String,
    @param:JsonProperty val name: String
)