package at.wuzeln.manager.config

import at.wuzeln.manager.model.enums.SecurityRole
import at.wuzeln.manager.oauth.GoogleAccessTokenValidator
import at.wuzeln.manager.oauth.GoogleTokenServices
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices



@Configuration
@EnableWebSecurity
@EnableResourceServer
class ResourceServerConfiguration(
        @Value("\${oauth.clientId}")
        private val clientId: String,
        @Value("\${oauth.checkTokenUrl}")
        private val checkTokenUrl: String,
        @Value("\${oauth.userInfoUrl}")
        private val userInfoUrl: String
) : ResourceServerConfigurerAdapter() {


    override fun configure(resources: ResourceServerSecurityConfigurer?) {
        resources!!
                .resourceId(clientId)
                .stateless(true)
    }

    @Bean
    fun tokenServices(tokenValidator: GoogleAccessTokenValidator): ResourceServerTokenServices {
        return GoogleTokenServices(tokenValidator, userInfoUrl)
    }

    @Bean
    fun googleAccessTokenValidator(): GoogleAccessTokenValidator {
        return GoogleAccessTokenValidator(clientId, checkTokenUrl)
    }

    override fun configure(http: HttpSecurity) {
        // @formatter:off
        http
                .requestMatchers()
                    .antMatchers("/**" )
        .and()
                .authorizeRequests()
                    .antMatchers( HttpMethod.POST, "/services/administration/users/current").permitAll()
                    .antMatchers( HttpMethod.GET,  "/services/administration/users/current").permitAll()

                    .antMatchers(
                            "/services/persons/**",
                            "/services/matches/**",
                            "/services/registrations/**").hasRole(SecurityRole.REGISTERED_USER.name)

                    .antMatchers( HttpMethod.POST, "/services/administration/users").hasRole(SecurityRole.ADMIN.name)
                    .antMatchers( HttpMethod.GET, "/services/administration/users").hasRole(SecurityRole.ADMIN.name)
                    .antMatchers( HttpMethod.PUT, "/services/administration/users/**").hasRole(SecurityRole.ADMIN.name)
        .and()
                .exceptionHandling()
                    .accessDeniedHandler(OAuth2AccessDeniedHandler())
        // @formatter:on
    }

}
