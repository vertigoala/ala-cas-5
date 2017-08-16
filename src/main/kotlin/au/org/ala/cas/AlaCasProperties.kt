package au.org.ala.cas

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties
import org.apereo.cas.configuration.model.support.cookie.CookieProperties
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(value = "ala")
open class AlaCasProperties(
        var cookie: CookieProperties = CookieProperties(),
        var userCreator: UserCreatorProperties = UserCreatorProperties(),
        val skin: SkinProperties = SkinProperties()
)

open class UserCreatorProperties(
        var passwordEncoder: PasswordEncoderProperties = PasswordEncoderProperties(),
        var jdbc: JDBCUserCreatorProperties = UserCreatorProperties.JDBCUserCreatorProperties(),
        var userCreatePassword: String = ""
) {
    open class JDBCUserCreatorProperties : AbstractJpaProperties() {
        var createUserProcedure: String = "sp_create_user"
    }

}

open class SkinProperties {
    lateinit var baseUrl: String
    lateinit var headerFooterUrl: String
    lateinit var favIconBaseUrl: String
    lateinit var bieBaseUrl: String
    lateinit var bieSearchPath: String
    lateinit var userDetailsUrl: String
    lateinit var orgShortName: String
    lateinit var orgLongName: String
    lateinit var orgNameKey: String
    var cacheDuration: Long = 1800_000
}

