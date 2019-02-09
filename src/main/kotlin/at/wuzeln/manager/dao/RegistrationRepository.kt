package at.wuzeln.manager.dao

import at.wuzeln.manager.model.Registration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface RegistrationRepository : JpaRepository<Registration, Long> {

    @Query("SELECT r FROM Registration r WHERE r.match IS NULL")
    fun findOpened(): List<Registration>
}