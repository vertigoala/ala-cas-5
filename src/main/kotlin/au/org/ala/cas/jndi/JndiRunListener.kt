package au.org.ala.cas.jndi

import au.org.ala.utils.logger
import org.springframework.boot.SpringApplication
import org.springframework.boot.SpringApplicationRunListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.ConfigurableEnvironment
import javax.naming.InitialContext
import org.osjava.sj.SimpleJndi
import javax.naming.Context


open class JndiRunListener(val application: SpringApplication, args: Array<String>) : SpringApplicationRunListener {

    companion object {
        val log = logger<JndiRunListener>()

        const val JNDI_PATH_PROPERTY = "jndi.path"
        const val JNDI_PATH_ENV_VARIABLE = "JNDI_PATH"

        var initialised: Boolean = false
    }

    override fun starting() {
        println("JNDI Run Listener - starting")
        Class.forName("com.mysql.jdbc.Driver").newInstance() // TODO???
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.SimpleContextFactory") // override the Tomcat apache naming factory
    }


    override fun environmentPrepared(environment: ConfigurableEnvironment?) {
        log.debug("JNDI Run Listener - environment prepared")
    }

    override fun contextPrepared(context: ConfigurableApplicationContext?) {
        if (!initialised) {
            log.debug("JNDI Run Listener - context prepared")
            val location = System.getProperty(JNDI_PATH_PROPERTY) ?: System.getenv(JNDI_PATH_ENV_VARIABLE) ?: context?.environment?.getProperty(JNDI_PATH_PROPERTY)
            if (location != null) {
                log.info("Using {} for JNDI root directory", location)
                System.setProperty(SimpleJndi.ROOT, location) // SimpleJNDI will use this as an override to the value in jndi.properties
                val ctx = InitialContext()
                log.debug("Created initial context {}", ctx)
                log.info("JNDI config complete")
                initialised = true
            }
        }
    }

    override fun contextLoaded(context: ConfigurableApplicationContext?) {
        log.debug("JNDI Run Listener - context loaded")
    }

    override fun finished(context: ConfigurableApplicationContext?, exception: Throwable?) {
        log.debug("JNDI Run Listener - finished")
    }

}