package at.wuzeln.manager.dao

import at.wuzeln.manager.model.UserAccount
import at.wuzeln.manager.model.enums.SecurityRole
import org.springframework.data.jpa.repository.JpaRepository

interface UserAccountRepository : JpaRepository<UserAccount, Long> {

    fun findByGoogleAccountId(accountId: String): UserAccount?

    fun findByUsername(username: String): UserAccount?

    fun findByRoles_name(securiyRole: SecurityRole): List<UserAccount>
}