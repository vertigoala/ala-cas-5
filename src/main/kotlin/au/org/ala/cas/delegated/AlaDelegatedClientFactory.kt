package au.org.ala.cas.delegated

import au.org.ala.utils.logger
import org.apache.commons.lang3.StringUtils
import org.apereo.cas.configuration.model.support.pac4j.Pac4jDelegatedAuthenticationProperties
import org.apereo.cas.support.pac4j.authentication.DelegatedClientFactory
import org.pac4j.core.client.BaseClient
import org.pac4j.core.credentials.Credentials
import org.pac4j.core.profile.CommonProfile
import org.pac4j.oauth.client.TwitterClient

/**
 * TODO remove this after submitting twitter-email patch to CAS project
 */
class AlaDelegatedClientFactory(val pac4jProps: Pac4jDelegatedAuthenticationProperties) : DelegatedClientFactory(pac4jProps) {

    companion object {
        val log = logger()
    }

    override fun configureTwitterClient(properties: MutableCollection<BaseClient<out Credentials, out CommonProfile>>) {
        val twitter = pac4jProps.twitter
        if (StringUtils.isNotBlank(twitter.id) && StringUtils.isNotBlank(twitter.secret)) {
            val client = TwitterClient(twitter.id, twitter.secret, true)
            configureClient(client, twitter)

            log.debug("Created client [{}] with identifier [{}]", client.name, client.key)
            properties.add(client)
        }
    }
}