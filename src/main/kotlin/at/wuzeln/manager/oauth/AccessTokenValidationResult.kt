package at.wuzeln.manager.oauth

import java.util.*

/**
 * source https://github.com/galovics/google-oauth2-spring
 */
class AccessTokenValidationResult(
        val isValid: Boolean,
        private val tokenInfo: Map<String, *>) {

    fun getTokenInfo(): Map<String, *> {
        return Collections.unmodifiableMap<String, Any>(tokenInfo)
    }
}
