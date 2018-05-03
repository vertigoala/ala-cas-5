package au.org.ala.cas.pac4j

import org.pac4j.core.context.J2EContext
import org.pac4j.core.context.session.SessionStore
import java.util.*
import javax.servlet.ServletContext
import javax.servlet.http.HttpSession
import javax.servlet.http.HttpSessionContext

class FilterSessionStore : SessionStore<J2EContext> {

    private fun getSession(context: J2EContext): MutableMap<String, Any> {
        return context.request.getAttribute(Pac4jSessionStoreFilter.PAC4J_SESSION_SSTORE_KEY) as? MutableMap<String, Any> ?: throw IllegalStateException()
    }

    override fun renewSession(context: J2EContext) = false

    override fun buildFromTrackableSession(context: J2EContext?, trackableSession: Any?): SessionStore<J2EContext> {
        return this
    }

    override fun getTrackableSession(context: J2EContext?): Any {
        return Pac4jSessionStoreFilter.PAC4J_SESSION_SSTORE_KEY
    }

    override fun getOrCreateSessionId(context: J2EContext?): String {
        return Pac4jSessionStoreFilter.PAC4J_SESSION_SSTORE_KEY
    }

    override fun destroySession(context: J2EContext): Boolean {
        getSession(context).clear()
        return true
    }

    override fun get(context: J2EContext, key: String): Any? = getSession(context)[key]

    override fun set(context: J2EContext, key: String, value: Any?) {
        val session = getSession(context)
        if (value != null) {
            session[key] = value
        } else {
            session.remove(key)
        }
    }

    class SessionFacade(val session: MutableMap<String, Any>, val context: J2EContext) : HttpSession {
        override fun getLastAccessedTime(): Long = System.currentTimeMillis()

        override fun removeValue(name: String) {
            removeAttribute(name)
        }

        override fun setMaxInactiveInterval(interval: Int) {
            // noop
        }

        override fun getSessionContext(): HttpSessionContext {
            TODO("not implemented")
        }

        override fun getValueNames(): Array<String> {
            return session.keys.toTypedArray()
        }

        override fun getId(): String {
            return Pac4jSessionStoreFilter.PAC4J_SESSION_SSTORE_KEY
        }

        override fun removeAttribute(name: String) {
            session.remove(name)
        }

        override fun putValue(name: String, value: Any?) {
            setAttribute(name, value)
        }

        override fun getAttributeNames(): Enumeration<String> = Collections.enumeration(session.keys)

        override fun isNew(): Boolean {
            return false
        }

        override fun getServletContext(): ServletContext = context.request.servletContext

        override fun invalidate() {
            session.clear()
        }

        override fun getCreationTime(): Long {
            return System.currentTimeMillis()
        }

        override fun getAttribute(name: String): Any? = session[name]

        override fun setAttribute(name: String, value: Any?) {
            if (value == null) {
                session.remove(name)
            } else {
                session[name] = value
            }
        }

        override fun getValue(name: String): Any? = getAttribute(name)

        override fun getMaxInactiveInterval(): Int {
            return Int.MAX_VALUE
        }
    }
}