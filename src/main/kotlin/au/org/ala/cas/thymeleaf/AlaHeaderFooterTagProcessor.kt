package au.org.ala.cas.thymeleaf

import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.context.WebEngineContext
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractElementTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.templatemode.TemplateMode

class AlaHeaderFooterTagProcessor(
    dialectPrefix: String,
    val tagName: String,
    val alaTemplateClient: AlaTemplateClient
) :
    AbstractElementTagProcessor(
        TemplateMode.HTML,
        dialectPrefix,
        tagName,  // the tag name to match
        true,      // apply dialect prefix to tag name
        null,      // attribute name
        false,     // apply dialect prefix to attribute name
        PRECEDENCE // precedence inside dialect's precedence
    ) {

    companion object {
        const val PRECEDENCE = 1000
    }

    override fun doProcess(
        context: ITemplateContext,
        tag: IProcessableElementTag,
        structureHandler: IElementTagStructureHandler
    ) {
        val request = when (context) {
            is WebEngineContext -> context.request
            else -> null
        }
        val content = alaTemplateClient.load(tagName, request)
        if (content == null) {
            structureHandler.replaceWith("<div th:replace=\"fragments/$tagName\"></div>", true)
        } else {
            structureHandler.replaceWith(content, false)
        }
    }


}