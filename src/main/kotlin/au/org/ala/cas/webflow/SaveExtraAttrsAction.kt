package au.org.ala.cas.webflow

import au.org.ala.cas.AlaCasProperties
import au.org.ala.cas.alaUserId
import au.org.ala.cas.delegated.AlaPrincipalFactory
import au.org.ala.utils.logger
import org.apereo.cas.authentication.Authentication
import org.apereo.cas.authentication.principal.ClientCredential
import org.apereo.cas.web.support.WebUtils
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.webflow.action.AbstractAction
import org.springframework.webflow.engine.FlowVariable
import org.springframework.webflow.engine.VariableValueFactory
import org.springframework.webflow.execution.Event
import org.springframework.webflow.execution.RequestContext
import javax.sql.DataSource

open class SaveExtraAttrsAction(val alaCasProperties: AlaCasProperties, val dataSource: DataSource, transactionManager: DataSourceTransactionManager) : AbstractAction() {


    companion object {
        val log = logger()

        const val EXTRA_ATTRS_FLOW_VAR = "attrs"

        const val ORGANISATION = "organisation"
        const val CITY = "city"
        const val STATE = "state"
        const val COUNTRY = "country"
//        const val TELEPHONE = "telephone"
//        const val PRIMARY_USER_TYPE = "primaryUserType"
//        const val SECONDARY_USER_TYPE = "secondaryUserType"
    }

    val transactionTemplate: TransactionTemplate = TransactionTemplate(transactionManager)

    override fun doExecute(context: RequestContext): Event {

//        val credential = WebUtils.getCredential(context, ClientCredential::class.java)
        val authentication = WebUtils.getAuthentication(context)
        val userid: Long? = authentication.alaUserId()

        if (userid == null) {
            log.warn("Couldn't extract userid from {}, aborting", authentication)
            return success()
        }

        val extraAttrs =
            context.flowScope[EXTRA_ATTRS_FLOW_VAR] as? ExtraAttrs

        if (extraAttrs == null) {
            log.warn("Couldn't find extraAttrs in flow scope, aborting")
            return success()
        }

        transactionTemplate.execute { status ->
            try {
                val template = NamedParameterJdbcTemplate(dataSource)
                listOf(ORGANISATION to extraAttrs.organisation,
                    CITY to extraAttrs.city,
                    STATE to extraAttrs.state,
                    COUNTRY to extraAttrs.country
                ).forEach { (name, value) ->
                    updateField(template, userid, authentication, name, value)
                }
            } catch (e: Exception) {
                // If we can't set the properties, just log and move on
                // because none of these properties are required.
                log.warn("Rolling back transaction because of exception", e)
                status.setRollbackOnly()
//                throw e
            }
        }

        context.flowScope.remove(EXTRA_ATTRS_FLOW_VAR)
        return success()
    }

    private fun updateField(template: NamedParameterJdbcTemplate, userid: Long, authentication: Authentication, name: String, value: String) {
        if (value != getAttribute(authentication, name)) {
            updateField(template, userid, name, value)
            authentication.principal.attributes[name] = value
            if (authentication.attributes.containsKey(name)) authentication.attributes[name] = value
        }
    }

    private fun updateField(template: NamedParameterJdbcTemplate, userid: Long, name: String, value: String) {
        val params = mapOf("userid" to userid, "name" to name, "value" to value)
        val result = template.queryForObject(alaCasProperties.userCreator.jdbc.countExtraAttributeSql, params, Integer::class.java)
        val updateCount = if (result > 0) {
            template.update(alaCasProperties.userCreator.jdbc.updateExtraAttributeSql, params)
        } else {
            template.update(alaCasProperties.userCreator.jdbc.insertExtraAttributeSql, params)
        }
        if (updateCount != 1) {
            log.warn("Insert / update field for {}, {}, {} returned {} updates", userid, name, value, updateCount)
        }
    }

    private fun getAttribute(authentication: Authentication, name: String) = authentication.principal.attributes[name] as? String

}
