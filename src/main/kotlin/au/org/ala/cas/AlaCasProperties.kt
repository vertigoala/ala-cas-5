package au.org.ala.cas

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties
import org.apereo.cas.configuration.model.support.cookie.CookieProperties
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(value = "ala")
open class AlaCasProperties(
        var cookie: CookieProperties = CookieProperties(),
        var userCreator: UserCreatorProperties = UserCreatorProperties()
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

