package au.org.ala.cas.session

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.data.redis.config.ConfigureRedisAction

@Configuration
class AlaSpringSessionConfiguration {

    @ConditionalOnProperty("spring.session.disable-redis-config-action")
    @Bean
    fun configureRedisAction(): ConfigureRedisAction {
        return ConfigureRedisAction.NO_OP
    }
}