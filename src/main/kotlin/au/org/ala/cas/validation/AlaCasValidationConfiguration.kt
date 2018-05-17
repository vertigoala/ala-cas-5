package au.org.ala.cas.validation

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan
import org.apereo.cas.authentication.ProtocolAttributeEncoder
import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.services.ServicesManager
import org.apereo.cas.services.web.support.AuthenticationAttributeReleasePolicy
import org.apereo.cas.web.view.Cas30ResponseView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.View

/**
 * Reconfigure the CAS 2.0 service validate response view to look like the v3 response view (ie add attributes to the
 * response).
 */
@Configuration
@EnableConfigurationProperties(CasConfigurationProperties::class)
class AlaCasValidationConfiguration {

    @Autowired
    lateinit var casProperties: CasConfigurationProperties

    @Autowired
    @Qualifier("casAttributeEncoder")
    lateinit var protocolAttributeEncoder: ProtocolAttributeEncoder

    @Autowired
    @Qualifier("cas3SuccessView")
    lateinit var cas3SuccessView: View

    @Autowired
    @Qualifier("authenticationAttributeReleasePolicy")
    lateinit var authenticationAttributeReleasePolicy: AuthenticationAttributeReleasePolicy

    @Autowired
    @Qualifier("servicesManager")
    lateinit var servicesManager: ServicesManager

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    lateinit var selectionStrategies: AuthenticationServiceSelectionPlan

    @Bean
    fun cas2ServiceSuccessView(): View {
        val authenticationContextAttribute = casProperties.authn.mfa.authenticationContextAttribute
        val isReleaseProtocolAttributes = casProperties.authn.isReleaseProtocolAttributes
        return Cas30ResponseView(
            true,
            protocolAttributeEncoder,
            servicesManager,
            authenticationContextAttribute,
            cas3SuccessView,
            isReleaseProtocolAttributes,
            authenticationAttributeReleasePolicy,
            selectionStrategies
        )
    }
}