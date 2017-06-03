package au.org.ala.cas

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(value = "jndi")
class JndiConfigurationProperties(var hikari: List<HikariDatasource> = mutableListOf<HikariDatasource>())

open class HikariDatasource(
        var name: String = "jdbc/ds",
        var driverClass: String? = null,
        var url: String = "jdbc:hsqldb:mem:cas-hsql-database",
        var user: String = "sa",
        var password: String = "",
        var autoCommit: Boolean? = null,
        var connectionTimeout: Long? = null,
        var idleTimeout: Long? = null,
        var maxLifetime: Long? = null,
        var connectionTestQuery: String? = null,
        var minimumIdle: Int? = null,
        var maximumPoolSize: Int? = null,
        var poolName: String? = null,
        var initializationFailTimeout: Long? = null,
        var isolateInternalQueries: Boolean? = null,
        var allowPoolSuspension: Boolean? = null,
        var readOnly: Boolean? = null,
        var registerMbeans: Boolean? = null,
        var catalog: String? = null,
        var connectionInitSql: String? = null,
        var transactionIsolation: String? = null,
        var validationTimeout: Long? = null,
        var leakDetectionThreshold: Long? = null,
        var dataSourceProperties: MutableMap<String, String> = mutableMapOf()
)