package at.wuzeln.manager.service

import assertk.assertions.*
import assertk.fail
import at.wuzeln.manager.TestUtil
import at.wuzeln.manager.config.AppConfig
import at.wuzeln.manager.dao.RoalRepository
import at.wuzeln.manager.dao.UserAccountRepository
import at.wuzeln.manager.dto.UserAccountUpdateAdminDto
import at.wuzeln.manager.model.Role
import at.wuzeln.manager.model.UserAccount
import at.wuzeln.manager.model.enums.SecurityRole
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional


@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfig::class])
@DirtiesContext
open class UserAccountServiceTest {

    @Autowired
    lateinit var cut: UserAccountService
    @Autowired
    lateinit var userAccountRepository: UserAccountRepository
    @Autowired
    lateinit var roalRepository: RoalRepository

    @BeforeEach
    fun onSetUp() {
        TestUtil.mockSecurityContext()
    }

    @Test
    open fun testRegister_notValid() {

        try {
            cut.register("  ", "123", true)
            fail("the username patter should not match")
        } catch (e: WuzelnException) {
            assertk.assert(e.message).isEqualTo("Username must contain only letters, digits or underscores. The length must be between 2 and 20 characters")
        }
    }

    @Test
    @Transactional
    open fun testRegister_alreadyExists() {

        userAccountRepository.save(UserAccount(0, "username"))

        try {
            cut.register("username", "123", true)
            fail("the username patter should not match")
        } catch (e: WuzelnException) {
            assertk.assert(e.message).isEqualTo("The username username is already taken")
        }
    }

    @Test
    @Transactional
    open fun testRegister_success() {

        roalRepository.save(Role(0, SecurityRole.ACTIVE_USER))

        var accountId = cut.register("username", "123", true)

        var account = userAccountRepository.getOne(accountId);
        assertk.assert(account.googleAccountId).isEqualTo("123")
        assertk.assert(account.username).isEqualTo("username")
        assertk.assert(account.roles).hasSize(1)
        account.roles.forEach { assertk.assert(it.name).isEqualTo(SecurityRole.ACTIVE_USER) }
    }


    @Test
    @Transactional
    open fun testGetUsernamePasswordAuthenticationTokenByGoogleAccountId() {

        saveActiveUser()

        var auth = cut.getUsernamePasswordAuthenticationTokenByGoogleAccountId("123456")

        assertk.assert(auth!!.principal).isEqualTo("username")
        assertk.assert(auth.credentials).isNull()
        assertk.assert(auth.authorities).hasSize(1)
        auth.authorities.forEach { assertk.assert(it.authority).isEqualTo(SecurityRole.ACTIVE_USER.nameWithPrefix()) }
    }

    @Test
    @Transactional
    open fun testUpdateUserAccount() {

        roalRepository.save(Role(0, SecurityRole.ACTIVE_USER))
        roalRepository.save(Role(0, SecurityRole.ADMIN))
        roalRepository.save(Role(0, SecurityRole.REGISTERED_USER))
        userAccountRepository.save(UserAccount(0, "username"))

        cut.updateUserAccount("username", UserAccountUpdateAdminDto(true, true, true))

        var result = userAccountRepository.findByUsername("username")

        assertk.assert(result!!.roles).hasSize(3)
        assertk.assert(result.hasRole(SecurityRole.ACTIVE_USER)).isTrue()
        assertk.assert(result.hasRole(SecurityRole.ADMIN)).isTrue()
        assertk.assert(result.hasRole(SecurityRole.REGISTERED_USER)).isTrue()
    }

    @Test
    @Transactional
    open fun testGetUserAccount() {

        saveActiveUser()

        var result = cut.getUserAccount("username")

        assertk.assert(result.active).isTrue()
        assertk.assert(result.admin).isFalse()
        assertk.assert(result.createdDate).isNotNull()
        assertk.assert(result.googleAccountId).isEqualTo("123456")
        assertk.assert(result.registered).isFalse()
        assertk.assert(result.username).isEqualTo("username")
    }

    @Test
    @Transactional
    open fun testGetUserAccounts() {

        saveActiveUser()

        var result = cut.getUserAccounts()

        assertk.assert(result).hasSize(1)
        assertk.assert(result[0].active).isTrue()
        assertk.assert(result[0].admin).isFalse()
        assertk.assert(result[0].createdDate).isNotNull()
        assertk.assert(result[0].googleAccountId).isEqualTo("123456")
        assertk.assert(result[0].registered).isFalse()
        assertk.assert(result[0].username).isEqualTo("username")
    }

    fun saveActiveUser() {
        var role = roalRepository.save(Role(0, SecurityRole.ACTIVE_USER))

        var account = UserAccount(0, "username")
        account.googleAccountId = "123456"
        account.roles.add(role)
        userAccountRepository.save(account)
    }

}
