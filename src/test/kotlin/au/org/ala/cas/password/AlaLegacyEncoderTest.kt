package au.org.ala.cas.password

import org.junit.Test
import kotlin.test.*

const val PRE_ENCODED = "ix7dE9NCi4MvygreOZtrTA=="
const val SALT = "abc123"
const val SALT2 = "def123"
const val RAW_PASSWORD = "password"

class AlaLegacyEncoderTest {

    @Test
    fun testEncode() {
        val encoder = AlaLegacyEncoder(SALT)
        val encoded = encoder.encode(RAW_PASSWORD)
        assertEquals(PRE_ENCODED, encoded)

        val encoder2 = AlaLegacyEncoder(SALT2)
        val encoded2 = encoder2.encode(RAW_PASSWORD)
        assertNotEquals(PRE_ENCODED, encoded2)
    }

    @Test fun testMatch() {
        val encoder = AlaLegacyEncoder(SALT)
        val matches = encoder.matches(RAW_PASSWORD, PRE_ENCODED)
        assertTrue(matches)

        val noMatch = encoder.matches("notpassword", PRE_ENCODED)
        assertFalse(noMatch)
    }
}