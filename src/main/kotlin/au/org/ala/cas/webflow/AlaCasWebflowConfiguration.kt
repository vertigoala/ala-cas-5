package au.org.ala.cas.webflow

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry
import org.springframework.webflow.engine.builder.support.FlowBuilderServices

@Configuration("alaCasWebflowConfiguration")
open class AlaCasWebflowConfiguration {

    @Autowired
    @Qualifier("loginFlowRegistry")
    lateinit var loginFlowDefinitionRegistry: FlowDefinitionRegistry

    @Autowired
    lateinit var flowBuilderServices: FlowBuilderServices

    @ConditionalOnMissingBean(name = arrayOf("authCookeWebflowConfigurer"))
    @Bean("authCookeWebflowConfigurer")
    open fun authCookeWebflowConfigurer(): AuthCookieWebflowConfigurer = AuthCookieWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry)
}