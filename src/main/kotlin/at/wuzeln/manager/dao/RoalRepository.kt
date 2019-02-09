package at.wuzeln.manager.dao

import at.wuzeln.manager.model.Role
import at.wuzeln.manager.model.enums.SecurityRole
import org.springframework.data.jpa.repository.JpaRepository

interface RoalRepository : JpaRepository<Role, Long> {

    fun findByName(role: SecurityRole): Role

}