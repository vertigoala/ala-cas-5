package au.org.ala.cas.pac4j

import au.org.ala.utils.logger
import org.apereo.cas.util.serialization.StringSerializer
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class Pac4jSessionStoreFilter(val pac4jCookieRetrievingCookieGenerator: CookieRetrievingCookieGenerator, val serializer: StringSerializer<Map<String, Any>>) : OncePerRequestFilter() {

    companion object {
        const val PAC4J_SESSION_SSTORE_KEY = "Pac4jSessionStoreFilter"

        val log = logger()
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val session = getSession(request)
        request.setAttribute(PAC4J_SESSION_SSTORE_KEY, session)

        filterChain.doFilter(request, response)

        val requestSession = getSession(request)
        val responseSession = request.getAttribute(PAC4J_SESSION_SSTORE_KEY)

        if (responseSession is Map<*, *>) {
            if (responseSession != requestSession) {
                setSession(request, response, responseSession as Map<String, Any>)
            } else {
                log.debug("session attribute unchanged, not generating a set-cookie header")
            }
        } else {
            log.warn("Session store retrieved from request attribute is not a map: {}", responseSession)
        }

    }

    fun getSession(request: HttpServletRequest) : MutableMap<String, Any> {
        val cookieValue = pac4jCookieRetrievingCookieGenerator.retrieveCookieValue(request)
        return if (cookieValue != null && cookieValue.isNotBlank()) {
            val session = serializer.from(cookieValue)
            session as? MutableMap ?: session.toMutableMap()
        } else {
            mutableMapOf()
        }
    }

    fun setSession(request: HttpServletRequest, response: HttpServletResponse, session: Map<String, Any>?) {
        if (session == null || session.isEmpty()) {
            pac4jCookieRetrievingCookieGenerator.removeCookie(response)
        } else {
            pac4jCookieRetrievingCookieGenerator.addCookie(request, response, serializer.toString(session))
        }
    }
}