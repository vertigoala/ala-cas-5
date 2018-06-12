package au.org.ala.cas.webflow

import au.org.ala.cas.AlaCasProperties
import au.org.ala.utils.urlParameterSafe
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.webflow.action.AbstractAction
import org.springframework.webflow.execution.Event
import org.springframework.webflow.execution.RequestContext
import java.net.URL
import java.util.concurrent.TimeUnit

class RenderDelegatedAuthAction(val alaCasProperties: AlaCasProperties) : AbstractAction() {
    val typeReference = TypeFactory.defaultInstance()
        .constructCollectionType(List::class.java, CodedName::class.java)
    val mapper = jacksonObjectMapper()
    val COUNTRIES = "countries"
    val stateCache = Caffeine.newBuilder()
        .refreshAfterWrite(1, TimeUnit.HOURS).build { country: String ->
        val url = if (country == COUNTRIES) {
            URL(alaCasProperties.userCreator.countriesListUrl)
        } else {
            URL("${alaCasProperties.userCreator.statesListUrl}?country=${country.urlParameterSafe()}")
        }
        return@build mapper.readValue<List<CodedName>>(url.openConnection().apply { this.setRequestProperty("Accept", "application/json") }.getInputStream().reader(Charsets.UTF_8), typeReference)
    }

    override fun doExecute(context: RequestContext): Event {
        context.viewScope.put("countries", stateCache[COUNTRIES])
        context.viewScope.put("states", stateCache[(context.flowScope[SaveExtraAttrsAction.EXTRA_ATTRS_FLOW_VAR] as ExtraAttrs).country])
        return success()
    }

}