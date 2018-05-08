package au.org.ala.cas

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties
import org.apereo.cas.configuration.model.support.cookie.CookieProperties
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(value = "ala")
open class AlaCasProperties(
    @field:NestedConfigurationProperty var cookie: CookieProperties = CookieProperties(),
    @field:NestedConfigurationProperty var userCreator: UserCreatorProperties = UserCreatorProperties(),
    @field:NestedConfigurationProperty val skin: SkinProperties = SkinProperties()
)

open class UserCreatorProperties(
    @field:NestedConfigurationProperty var passwordEncoder: PasswordEncoderProperties = PasswordEncoderProperties(),
    @field:NestedConfigurationProperty var jdbc: JDBCUserCreatorProperties = JDBCUserCreatorProperties(),
    var userCreatePassword: String = ""
)

open class JDBCUserCreatorProperties : AbstractJpaProperties() {
    var createUserProcedure: String = "sp_create_user"
}

open class SkinProperties {
    lateinit var baseUrl: String
    lateinit var termsUrl: String
    lateinit var headerFooterUrl: String
    lateinit var favIconBaseUrl: String
    lateinit var bieBaseUrl: String
    lateinit var bieSearchPath: String
    lateinit var userDetailsUrl: String
    lateinit var orgShortName: String
    lateinit var orgLongName: String
    lateinit var orgNameKey: String
    var cacheDuration: String = "PT30m"
}

