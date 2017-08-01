package au.org.ala.cas.webflow

import org.apereo.cas.configuration.model.support.cookie.CookieProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(value = "auth")
open class AuthCookieProperties(
        var cookie: CookieProperties = CookieProperties()
)