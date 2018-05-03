package au.org.ala.cas.pac4j

import org.apereo.cas.util.serialization.StringSerializer
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator
import org.pac4j.core.context.J2EContext
import org.pac4j.core.context.session.SessionStore

class CookieSessionStore(val pac4jSessionStoreCookieGenerator: CookieRetrievingCookieGenerator,
                         val serializer: StringSerializer<Map<String, Any>>) : SessionStore<J2EContext> {

    private fun getSession(context: J2EContext): MutableMap<String, Any> {
        val cookieValue = pac4jSessionStoreCookieGenerator.retrieveCookieValue(context.request)
        return if (cookieValue != null && cookieValue.isNotBlank()) {
            val result = serializer.from(cookieValue)
            result as? MutableMap ?: result.toMutableMap()
        } else {
            mutableMapOf()
        }
    }

    private fun putSession(context: J2EContext, session: Map<String, Any>) {
        pac4jSessionStoreCookieGenerator.addCookie(context.request, context.response, serializer.toString(session))
    }

    override fun renewSession(context: J2EContext): Boolean {
        putSession(context, getSession(context))
        return true
    }

    override fun buildFromTrackableSession(context: J2EContext, trackableSession: Any?): SessionStore<J2EContext> {
        return this
    }

    override fun getTrackableSession(context: J2EContext): Any {
        return pac4jSessionStoreCookieGenerator.cookieName
    }

    override fun getOrCreateSessionId(context: J2EContext): String {
        return pac4jSessionStoreCookieGenerator.cookieName
    }

    override fun destroySession(context: J2EContext): Boolean {
        pac4jSessionStoreCookieGenerator.removeCookie(context.response)
        return true
    }

    override fun get(context: J2EContext, key: String): Any? {
        return getSession(context)[key]
    }

    override fun set(context: J2EContext, key: String, value: Any?) {
        val session = getSession(context)
        if (value == null) {
            session.remove(key)
        } else {
            session[key] = value
        }
        putSession(context, session)
    }
}