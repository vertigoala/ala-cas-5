package au.org.ala.cas.jndi

import au.org.ala.utils.logger
import org.osjava.sj.SimpleJndi
import org.osjava.sj.jndi.MemoryContext
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.naming.Context
import javax.naming.spi.InitialContextFactory

/**
 * This is an InitialContextFactory that is essentially the same as the SimpleJNDI MemoryContextFactory except that it
 * returns memory contexts that don't remove all entries when they're closed.  This works around the Spring JndiTemplate
 * behaviour of closing its context after every lookup.
 *
 * This means that the contexts contents will be cleaned up only by garbage collection.  Since shared contexts are held
 * in a static ConcurrentHashMap<>, they won't be candidates for garbage collection until
 * NoCloseMemoryContextFactory.emptyCache is invoked.
 */
class NoCloseMemoryContextFactory : InitialContextFactory {

    companion object {
        val contextsByRoot = ConcurrentHashMap<String, NoCloseMemoryContextDecorator>()

        fun emptyCache() {
            val i = contextsByRoot.iterator()
            while (i.hasNext()) {
                val entry = i.next()
                i.remove()
                entry.value.destroy()
            }
        }
    }

    override fun getInitialContext(environment: Hashtable<*, *>): Context {
        val isShared = (environment[SimpleJndi.SHARED] as? String)?.toBoolean() ?: false
        return if (!isShared) {
            NoCloseMemoryContextDecorator("", MemoryContext(environment))
        } else {
            val root = environment[SimpleJndi.ROOT] as? String ?: ""
            contextsByRoot[root] ?: NoCloseMemoryContextDecorator(root, MemoryContext(environment)).apply { contextsByRoot.put(root, this) }
        }
    }
}

class NoCloseMemoryContextDecorator(val root: String, val context: MemoryContext) : Context by context {

    companion object {
        val log = logger<NoCloseMemoryContextDecorator>()
    }

    override fun close() {
        // no-op
        log.info("Attempted to close {}", root)
    }

    fun destroy() {
        context.close()
    }
}
