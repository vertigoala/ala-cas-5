package au.org.ala.cas.password

import au.org.ala.cas.AlaCasProperties
import org.apereo.cas.configuration.CasConfigurationProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.webflow.execution.FlowExecutionListener

@Configuration
@EnableConfigurationProperties(AlaCasProperties::class)
class PasswordInterruptConfiguration {

    class NoopFlowExectionListener : FlowExecutionListener

    @Autowired
    lateinit var alaCasProperties: AlaCasProperties

    /**
     * Overrides the CAS default FlowExecutionListener because we don't need [CasConfigurationProperties] in our views.
     */
    @Bean
    fun casFlowExecutionListener(): FlowExecutionListener {
        return NoopFlowExectionListener()
    }

    @Bean
    fun interruptInquirer() = PasswordUpgradeInterruptInquirer(alaCasProperties.skin)

}