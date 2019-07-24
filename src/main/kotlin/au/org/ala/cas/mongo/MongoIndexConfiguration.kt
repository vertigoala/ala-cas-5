package au.org.ala.cas.mongo

import au.org.ala.cas.jndi.JndiConfigurationProperties
import au.org.ala.utils.logger
import com.mongodb.BasicDBObject
import com.mongodb.DBCollection
import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.ticket.TicketCatalog
import org.apereo.cas.ticket.registry.TicketHolder
import org.apereo.cas.ticket.registry.TicketRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate
import javax.annotation.PostConstruct

@Configuration
@EnableConfigurationProperties(JndiConfigurationProperties::class, CasConfigurationProperties::class)
class MongoIndexConfiguration {

    companion object {
        private val log = logger()
        private val MONGO_INDEX_KEYS = setOf("v", "key", "name", "ns")
        private val EMPTY_OPTIONS = BasicDBObject()
    }

    @Autowired
    lateinit var mongoDbTicketRegistryTemplate: MongoTemplate

    @Autowired
    lateinit var ticketRegistry: TicketRegistry

    @Autowired
    lateinit var ticketCatalog: TicketCatalog

    @PostConstruct
    fun initMongoIndices() {
        val definitions = ticketCatalog.findAll()
        definitions.forEach { t ->
            val name = t.properties.storageName
            val c = mongoDbTicketRegistryTemplate.db.getCollection(name)
            c.ensureIndex(BasicDBObject(TicketHolder.FIELD_NAME_ID, 1), BasicDBObject("unique", 1))
            log.debug("Ensured MongoDb collection {} index for [{}]", TicketHolder.FIELD_NAME_ID, c.fullName)
        }
    }

    private fun DBCollection.ensureIndex(indexKey: BasicDBObject, options: BasicDBObject = EMPTY_OPTIONS) {
        // Mongo will throw an Exception if attempting to create an index whose key already exists
        // as an index but has different options.  No exception is thrown if the index is exactly the same.
        // So drop existing indices on the same keys but with different options before recreating them.
        val existingMismatch = indexInfo.any { existing ->
            val keyMatches = existing["key"] == indexKey
            val optionsMatch = options.entries.all { entry -> entry.value == existing[entry.key] }
            val noExtraOptions = existing.keySet().all { key -> MONGO_INDEX_KEYS.contains(key) || options.keys.contains(key) }

            keyMatches && !(optionsMatch && noExtraOptions)
        }
        if (existingMismatch) {
            log.debug("Removing MongoDb index [{}] from [{}] because it appears to already exist in a different form", indexKey, name)
            dropIndex(indexKey)
        }
        createIndex(indexKey, options)
    }
}