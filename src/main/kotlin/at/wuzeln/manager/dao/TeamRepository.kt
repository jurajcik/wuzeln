package at.wuzeln.manager.dao

import at.wuzeln.manager.model.Team
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface TeamRepository : JpaRepository<Team, Long> {

    @Query("SELECT p.team FROM Player p WHERE p.person.id = :personId " +
            "AND " + RepoConstants.MATCH_FINISHED_AND_RANGE_P)
    fun findByPersonInRange(personId: Long, startDate: LocalDateTime, endDate: LocalDateTime): List<Team>
}