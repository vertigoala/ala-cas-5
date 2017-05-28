package au.org.ala.cas.password

import au.org.ala.utils.*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import java.io.File
import java.io.FileNotFoundException
import java.security.SecureRandom
import java.util.*

/**
 * Load the password encoding properties from a property source and use those properties to
 * configure a FallbackPasswordEncoder with Bcrypt for primary and MD5 for fallback password
 * encoding.
 *
 * This works around the CAS server only calling the no-args constructor for a custom password
 * encoder and not loading it from the Spring context or giving it access to the property source.
 */
class AlaPasswordEncoder private constructor(val delegate: FallbackPasswordEncoder) : PasswordEncoder by delegate {

    constructor() : this(delegate = loadFallback())

    companion object {
        private val log = logger<AlaPasswordEncoder>()

        const val DEFAULT_LOCATION = "/data/cas5/config/pwe.properties"
        const val LOCATION_SYSTEM_PROPERTY = "ala.password.properties"
        const val LOCATION_ENV_VARIABLE = "ALA_PASSWORD_PROPERTIES"

        const val BCRYPT_STRENGTH_PROPERTY = "bcrypt.strength"
        const val BCRYPT_SECRET_PROPERTY = "bcrypt.secret"
        const val MD5_SECRET_PROPERTY = "md5.secret"
        const val MD5_BASE64_ENCODE_PROPERTY = "md5.base64Encode"

        fun loadFallback(): FallbackPasswordEncoder {
            // TODO find a way to get to the Spring Boot properties so we don't need a second property source
            val location = System.getProperty(LOCATION_SYSTEM_PROPERTY) ?: System.getenv(LOCATION_ENV_VARIABLE) ?: DEFAULT_LOCATION
            log.info("Loading AlaPasswordEncoder properties from $location")

            val props = try {
                File(location).loadProperties()
            } catch (e: FileNotFoundException) {
                log.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                log.error("ALA Password Encoder properties were not found at $location!")
                log.error("You must provide a properties file with password encoder settings!")
                log.error("By default this file will be loaded from $DEFAULT_LOCATION but you can specify the location using the ALA_PASSWORD_PROPERTIES environment variable or ala.password.properties system property")
                log.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                throw e
            }

            return loadFallBack(props)
        }

        fun loadFallBack(props: Properties): FallbackPasswordEncoder {
            val bcryptStrength = props.getProperty(BCRYPT_STRENGTH_PROPERTY)?.orNull()?.toInt()
            val bcryptSecretSeed = props.getProperty(BCRYPT_SECRET_PROPERTY)?.orNull()
            log.debug("Bcrypt PWE: Using {} for strength, {} for SecureRandom seed", bcryptStrength, bcryptSecretSeed)

            val bcryptSecret = bcryptSecretSeed?.let { SecureRandom(it.toByteArray(Charsets.UTF_8)) }
            val bCryptEncoder =
                    if (bcryptSecret != null && bcryptStrength != null) BCryptPasswordEncoder(bcryptStrength, bcryptSecret)
                    else if (bcryptStrength != null) BCryptPasswordEncoder(bcryptStrength)
                    else BCryptPasswordEncoder()

            val md5Secret = props.getProperty(MD5_SECRET_PROPERTY)?.orNull() ?: throw IllegalArgumentException("md5.secret property must be set in ALA Password Encoder properties!")
            val md5Base64Encode = props.getProperty(MD5_BASE64_ENCODE_PROPERTY)?.toBoolean() ?: true
            log.debug("MD5 PWE: Using {} for secret, {} for base64Encode", md5Secret, md5Base64Encode)


            return FallbackPasswordEncoder(primaryEncoder = bCryptEncoder, secondaryEncoder = AlaLegacyEncoder(md5Secret, md5Base64Encode) )
        }
    }
}