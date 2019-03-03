package at.wuzeln.manager.dao

import at.wuzeln.manager.model.stat.PlayerStats
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface PlayerStatsRepository : JpaRepository<PlayerStats, Long> {

    @Query("SELECT count(ps), SUM( ps.goalsSum ), SUM( ps.goalsOwnSum ), " +
            "SUM( ps.timeInGoalMillisSum ), SUM( ps.scoreOffensive ), SUM( ps.scoreDefensive ) " +
            "FROM PlayerStats ps JOIN ps.player p JOIN p.team t JOIN t.match m WHERE p.person.id = :personId " +
            "AND" + RepoConstants.MATCH_FINISHED_AND_RANGE_M)
    fun findByPersonIdAndRange(personId: Long, startDate: LocalDateTime, endDate: LocalDateTime): List<Array<Any>>
}