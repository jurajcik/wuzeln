package at.wuzeln.manager.dao

import at.wuzeln.manager.model.Match
import at.wuzeln.manager.model.Person
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface MatchRepository : JpaRepository<Match, Long> {

    @Query("SELECT max(pl.team.match.endDate) FROM Person p JOIN p.played pl " +
            "WHERE p = :person AND pl.team.match.endDate is not null ")
    fun findDateOfLastPlayedMatchForPerson(person: Person): LocalDateTime?

    @Query("SELECT p.team.match.startDate FROM Person pe JOIN pe.played p WHERE pe.id = :personId " +
            "AND " + RepoConstants.MATCH_FINISHED_AND_RANGE_P)
    fun getPlayedMatchesForPersonInRange(personId: Long, startDate: LocalDateTime, endDate: LocalDateTime): List<LocalDateTime>

    @Query("SELECT COUNT(p) FROM Person pe JOIN pe.played p WHERE pe.id = :personId " +
            "AND " + RepoConstants.MATCH_FINISHED_AND_RANGE_P)
    fun countPlayedMatchesForPerson(personId: Long, startDate: LocalDateTime, endDate: LocalDateTime): Int

    @Query("SELECT COUNT(m) FROM Person pe JOIN pe.idleMatches m WHERE pe.id = :personId " +
            "AND " + RepoConstants.MATCH_FINISHED_AND_RANGE_M)
    fun countIdleMatchesForPerson(personId: Long, startDate: LocalDateTime, endDate: LocalDateTime): Int

    fun findByMetadata_CreatedDateBeforeAndEndDateIsNull(date: LocalDateTime): List<Match>
}