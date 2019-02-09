package at.wuzeln.manager.dao

import at.wuzeln.manager.model.Goal
import at.wuzeln.manager.model.Match
import at.wuzeln.manager.model.Player
import at.wuzeln.manager.model.Team
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface GoalRepository : JpaRepository<Goal, Long> {

    @Query("SELECT count(g) FROM Goal g WHERE g.goalie.team = :team")
    fun countReceivedGoals(team: Team): Int

    @Query("SELECT g FROM Goal g WHERE g.date = (SELECT max(g.date) FROM Goal g WHERE g.goalie.team = :team)")
    fun lastReceivedGoal(team: Team): Goal?

    @Query("SELECT g FROM Goal g WHERE g.date = (SELECT max(g.date) FROM Goal g WHERE g.goalie.team.match = :match)")
    fun lastReceivedGoal(match: Match): Goal?

    fun findByGoalie_Team(team: Team): List<Goal>

    fun countByPlayerAndOwn(player: Player, own: Boolean): Int

    @Query("SELECT COUNT(g) FROM Player p JOIN p.goals g WHERE p.person.id = :personId " +
            "AND " + RepoConstants.MATCH_FINISHED_AND_RANGE_P )
    fun countGoalsByPersonInRange(personId: Long, startDate: LocalDateTime, endDate: LocalDateTime): Int

    @Query("SELECT COUNT(g) FROM Player p JOIN p.goals g WHERE p.person.id = :personId " +
            "AND g.own = TRUE " +
            "AND " + RepoConstants.MATCH_FINISHED_AND_RANGE_P)
    fun countGoalsOwnByPersonInRange(personId: Long, startDate: LocalDateTime, endDate: LocalDateTime): Int

}