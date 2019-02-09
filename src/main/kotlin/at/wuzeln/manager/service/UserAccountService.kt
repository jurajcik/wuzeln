package at.wuzeln.manager.service

import at.wuzeln.manager.dao.RoalRepository
import at.wuzeln.manager.dao.UserAccountRepository
import at.wuzeln.manager.dto.ConfigurationDto
import at.wuzeln.manager.dto.UserAccountDto
import at.wuzeln.manager.dto.UserAccountUpdateAdminDto
import at.wuzeln.manager.model.UserAccount
import at.wuzeln.manager.model.enums.SecurityRole
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.transaction.Transactional

@Service
class UserAccountService(
        private val userAccountRepository: UserAccountRepository,
        private val roleRepository: RoalRepository,
        private val configuration: ConfigurationDto
) {

    @Transactional
    fun getUsernamePasswordAuthenticationTokenByGoogleAccountId(accountId: String): UsernamePasswordAuthenticationToken? {

        val userAccount = userAccountRepository.findByGoogleAccountId(accountId);

        if (userAccount != null) {
            val authorities = userAccount.roles.map { SimpleGrantedAuthority(it.name.nameWithPrefix()) }

            return UsernamePasswordAuthenticationToken(userAccount.username, null, authorities)
        } else {
            return null
        }
    }

    @Transactional
    fun register(username: String, googleAccountId: String): Long {

        if (username == null || !username.matches(Regex(configuration.validationUsernamePattern))) {
            throw WuzelnException(configuration.validationUsernameText);
        }

        if (userAccountRepository.findByUsername(username) != null) {
            throw WuzelnException("The username $username is already taken")
        }

        val userAccount = UserAccount(0, username)
        userAccount.googleAccountId = googleAccountId

        return userAccountRepository.save(userAccount).id
    }

    @Transactional
    fun getUserAccount(username: String): UserAccountDto {

        val userAccount = userAccountRepository.findByUsername(username)
                ?: throw WuzelnException("There is no account for the username $username")

        return mapUserAccount(userAccount)
    }

    @Transactional
    fun updateUserAccount(username: String, userAccountDto: UserAccountUpdateAdminDto) {

        val userAccount = userAccountRepository.findByUsername(username)
                ?: throw WuzelnException("There is no account for the username $username")

        val roleRegistered = roleRepository.findByName(SecurityRole.REGISTERED_USER);
        val roleAdmin = roleRepository.findByName(SecurityRole.ADMIN);

        if (userAccountDto.registered) {
            userAccount.roles.add(roleRegistered)
        } else {
            userAccount.roles.remove(roleRegistered)
        }

        if (userAccountDto.admin) {
            userAccount.roles.add(roleAdmin)
        } else {
            userAccount.roles.remove(roleAdmin)
        }

        userAccountRepository.save(userAccount)
    }

    @Transactional
    fun getUserAccounts(): List<UserAccountDto> {

        val userAccounts = userAccountRepository.findAll()

        return userAccounts.map { mapUserAccount(it) }
    }

    @Transactional
    fun getUserAccountsByRole(securityRole: SecurityRole): List<UserAccountDto> {

        val userAccounts = userAccountRepository.findByRoles_name(securityRole)

        return userAccounts.map { mapUserAccount(it) }
    }

    fun mapUserAccount(userAccount: UserAccount): UserAccountDto {
        return UserAccountDto(
                userAccount.username,
                userAccount.googleAccountId,
                userAccount.hasRole(SecurityRole.REGISTERED_USER),
                userAccount.hasRole(SecurityRole.ADMIN),
                if (userAccount.metadata?.createdDate != null) {
                    at.wuzeln.manager.Utils.localDateTimeToLong(userAccount.metadata?.createdDate as LocalDateTime)
                } else {
                    null
                }
        )
    }

}


