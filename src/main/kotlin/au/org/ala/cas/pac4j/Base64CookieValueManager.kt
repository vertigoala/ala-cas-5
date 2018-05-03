package au.org.ala.cas.pac4j

import com.google.common.io.BaseEncoding
import org.apereo.cas.web.support.CookieValueManager
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest

class Base64CookieValueManager : CookieValueManager {

    companion object {
        @JvmStatic val base64 = BaseEncoding.base64Url()
    }

    override fun buildCookieValue(givenCookieValue: String?, request: HttpServletRequest): String? {
        return if (givenCookieValue != null && givenCookieValue.isNotEmpty()) {
            base64.encode(givenCookieValue.toByteArray(Charsets.UTF_8))
        } else {
            ""
        }
    }

    override fun obtainCookieValue(cookie: Cookie?, request: HttpServletRequest?): String? {
        return if (cookie != null && cookie.value != null && cookie.value.isNotEmpty()) {
            base64.decode(cookie.value).toString(Charsets.UTF_8)
        } else {
            ""
        }
    }

}