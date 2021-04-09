package au.org.ala.cas.webflow

import au.org.ala.cas.singleStringAttributeValue
import au.org.ala.cas.webflow.SaveExtraAttrsAction.Companion.CITY
import au.org.ala.cas.webflow.SaveExtraAttrsAction.Companion.COUNTRY
import au.org.ala.cas.webflow.SaveExtraAttrsAction.Companion.ORGANISATION
import au.org.ala.cas.webflow.SaveExtraAttrsAction.Companion.STATE
import au.org.ala.utils.logger
import org.springframework.binding.message.MessageBuilder
import org.springframework.binding.validation.ValidationContext
import java.io.Serializable

data class ExtraAttrs @JvmOverloads constructor(
    var organisation: String = "",
    var city: String = "",
    var state: String = "",
    var country: String = ""
) : Serializable {

    companion object {
        const val serialVersionUID: Long = 42

        val log = logger()

        fun fromMap(map: Map<String, Any?>): ExtraAttrs =
            ExtraAttrs(
                organisation = singleStringAttributeValue(map[ORGANISATION]) ?: "",
                city = singleStringAttributeValue(map[CITY]) ?: "",
                state = singleStringAttributeValue(map[STATE]) ?: "",
                country = singleStringAttributeValue(map[COUNTRY]) ?: ""
            )
    }

    private val validators: Map<String, Map<String, () -> Boolean>> = mapOf()

    val invalid get() = validators.any { (k, v) -> v.any { (n, f) -> f() } }

    fun validate(context: ValidationContext) {
        log.debug("validate {}", this)
        val messages = context.messageContext

        validators.forEach { (fieldName, v) ->
            v.forEach { (name, f) ->
                if (f()) {
                    messages.addMessage(
                        MessageBuilder().error().code("ala.user.attributes.$fieldName.$name").source(
                            fieldName
                        ).defaultText("$fieldName must not be $name.").build()
                    )
                }
            }
        }
    }
}