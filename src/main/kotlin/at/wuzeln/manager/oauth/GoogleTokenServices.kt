package at.wuzeln.manager.oauth

import at.wuzeln.manager.model.enums.SecurityRole
import at.wuzeln.manager.service.UserAccountService
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException
import org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices
import org.springframework.util.Assert
import org.springframework.web.client.RestTemplate

/**
 * source https://github.com/galovics/google-oauth2-spring
 */
class GoogleTokenServices(
        private val tokenValidator: GoogleAccessTokenValidator,
        var userInfoUrl: String
) : ResourceServerTokenServices, InitializingBean {

    private val restTemplate = RestTemplate()
    private val tokenConverter = DefaultAccessTokenConverter()
    @Autowired
    private lateinit var userAccountService: UserAccountService;

    @Throws(Exception::class)
    override fun afterPropertiesSet() {
        Assert.hasText(userInfoUrl, "userInfoUrl must not be blank")
    }

    @Throws(AuthenticationException::class, InvalidTokenException::class)
    override fun loadAuthentication(accessToken: String): OAuth2Authentication {
        val validationResult = tokenValidator.validate(accessToken)
        if (!validationResult.isValid) {
            throw UnapprovedClientAuthenticationException("The token is not intended to be used for this application.")
        }
        val tokenInfo = validationResult.getTokenInfo()
        return getAuthentication(tokenInfo, accessToken)
    }

    private fun getAuthentication(tokenInfo: Map<String, *>, accessToken: String): OAuth2Authentication {
        val request = tokenConverter.extractAuthentication(tokenInfo).oAuth2Request
        val authentication = getAuthenticationToken(accessToken)
        return OAuth2Authentication(request, authentication)
    }

    private fun getAuthenticationToken(accessToken: String): Authentication {
        val userInfo = getUserInfo(accessToken)
        val idStr = userInfo["id"] as String
                ?: throw InternalAuthenticationServiceException("Cannot get id from user info")
        val user = userAccountService.getUsernamePasswordAuthenticationTokenByGoogleAccountId(idStr)

        return user
                ?: AnonymousAuthenticationToken("anonymous", idStr, listOf(SimpleGrantedAuthority(SecurityRole.UNKNOWN.nameWithPrefix())))

    }

    private fun getUserInfo(accessToken: String): Map<String, *> {
        val headers = getHttpHeaders(accessToken)
        val map = restTemplate.exchange(userInfoUrl, HttpMethod.GET, HttpEntity<Any>(headers), Map::class.java).body
        return map as Map<String, Any>
    }

    private fun getHttpHeaders(accessToken: String): HttpHeaders {
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $accessToken")
        return headers
    }

    override fun readAccessToken(accessToken: String): OAuth2AccessToken {
        throw UnsupportedOperationException("Not supported: read access token")
    }

}
