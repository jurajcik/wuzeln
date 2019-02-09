package at.wuzeln.manager.oauth

import com.google.common.collect.ImmutableMap
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.InitializingBean
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.client.ClientHttpResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException
import org.springframework.stereotype.Component
import org.springframework.util.Assert
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate

import java.io.IOException

/**
 * source https://github.com/galovics/google-oauth2-spring
 */
@Component
class GoogleAccessTokenValidator(
        var clientId: String,
        var checkTokenUrl: String
) : InitializingBean {

    private val restTemplate = RestTemplate()

    init {
        restTemplate.errorHandler = object : DefaultResponseErrorHandler() {
            @Throws(IOException::class)
            override fun handleError(response: ClientHttpResponse) {
                if (response.rawStatusCode == 400) {
                    throw InvalidTokenException("The provided token is invalid")
                }
            }
        }
    }

    @Throws(Exception::class)
    override fun afterPropertiesSet() {
        Assert.hasText(clientId, "clientId must not be blank")
        Assert.hasText(checkTokenUrl, "checkTokenUrl must not be blank")
    }

    fun validate(accessToken: String): AccessTokenValidationResult {
        val response = getGoogleResponse(accessToken)
        val validationResult = validateResponse(response)
        return AccessTokenValidationResult(validationResult, response)
    }

    @Throws(AuthenticationException::class)
    private fun validateResponse(response: Map<String, *>): Boolean {
        val aud = response["aud"] as String
        return StringUtils.equals(aud, clientId)
    }

    private fun getGoogleResponse(accessToken: String): Map<String, *> {
        val requestEntity = HttpEntity<Any>(HttpHeaders())
        val variables = ImmutableMap.of("accessToken", accessToken)
        val map = restTemplate.exchange(checkTokenUrl!!, HttpMethod.GET, requestEntity, Map::class.java, variables).body
        return map as Map<String, *>
    }

}
