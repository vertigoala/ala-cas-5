package au.org.ala.cas.password

import org.springframework.security.crypto.password.PasswordEncoder

/**
 * Password encoder that delegates encoding to a primary encoder and matching to the primary encoder and
 * then to a series of fallback encoders if the primary match fails.  This allows legacy passwords to be used to login
 * but not for generating new passwords.
 */
class FallbackPasswordEncoder(val primaryEncoder : PasswordEncoder,
                              val secondaryEncoders: List<PasswordEncoder> = emptyList()) : PasswordEncoder {

    constructor(primaryEncoder: PasswordEncoder, secondaryEncoder: PasswordEncoder): this(primaryEncoder, listOf(secondaryEncoder))
    constructor(primaryEncoder: PasswordEncoder, vararg secondaryEncoders: PasswordEncoder): this(primaryEncoder, secondaryEncoders.asList())

    override fun encode(rawPassword: CharSequence) = primaryEncoder.encode(rawPassword)

    override fun matches(rawPassword: CharSequence, encodedPassword: String?) =
        primaryEncoder.matches(rawPassword, encodedPassword) or secondaryEncoders.any { secondary -> secondary.matches(rawPassword, encodedPassword) }
}