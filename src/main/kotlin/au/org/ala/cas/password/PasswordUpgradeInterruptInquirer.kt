package au.org.ala.cas.password

import au.org.ala.cas.SkinProperties
import org.apereo.cas.authentication.Authentication
import org.apereo.cas.authentication.principal.Service
import org.apereo.cas.authentication.principal.WebApplicationService
import org.apereo.cas.interrupt.InterruptInquirer
import org.apereo.cas.interrupt.InterruptResponse
import org.apereo.cas.services.RegisteredService

class PasswordUpgradeInterruptInquirer(val skinProperties: SkinProperties) :
    InterruptInquirer {
    override fun inquire(
        authentication: Authentication?,
        registeredService: RegisteredService?,
        service: Service?
    ): InterruptResponse {
        val legacyPasswordAttribute = authentication?.principal?.attributes?.get("legacyPassword")
        val legacyPassword = when(legacyPasswordAttribute) {
            is Array<*> -> legacyPasswordAttribute.contains("1")
            is Collection<*> -> legacyPasswordAttribute.contains("1")
            else -> legacyPasswordAttribute == "1"
        }

        return if (legacyPassword &&
                (service == null ||
                (service is WebApplicationService && service.originalUrl?.startsWith(skinProperties.resetPasswordUrl) == false))) {
            InterruptResponse(
                "ala.screen.interrupt.passwordupgrade",//"ALA has upgraded password security since you created your account.  Please reset your password to benefit from this upgrade."
                mapOf("ala.screen.interrupt.passwordreset" to skinProperties.resetPasswordUrl),
                false,
                true
            ).apply {
                this.isAutoRedirect = false
                this.autoRedirectAfterSeconds = -1
            }
        } else {
            InterruptResponse(false)
        }
    }

}