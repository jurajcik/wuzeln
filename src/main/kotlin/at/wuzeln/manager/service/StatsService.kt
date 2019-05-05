package at.wuzeln.manager.service

import at.wuzeln.manager.dao.MatchRepository
import at.wuzeln.manager.dao.MatchStatsRepository
import at.wuzeln.manager.dto.MatchBasicStatDto
import at.wuzeln.manager.model.Match
import at.wuzeln.manager.model.Team
import at.wuzeln.manager.model.stat.MatchStats
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*
import javax.transaction.Transactional

@Service
class StatsService(
        private val matchRepository: MatchRepository,
        private val matchStatsRepository: MatchStatsRepository,
        private val calculationService: CalculationService
) {

    private val log = KotlinLogging.logger {}

    @Transactional
    fun getBasicMatchStats(matchId: Long): MatchBasicStatDto {
        val match = matchRepository.getOne(matchId)
        return getBasicMatchStats(match)
    }

    private fun getBasicMatchStats(match: Match): MatchBasicStatDto {

        val allPlayers = match.teamBlue.players.union(match.teamRed.players)
        val allPlayerStatDto = ArrayList<MatchBasicStatDto.PlayerStatDto>()
        val duration = match.duration()

        for (player in allPlayers) {
            val playerStatDto = MatchBasicStatDto.PlayerStatDto(
                    player.person.id,
                    player.team.color,
                    player.goals.filter { !it.own }.count(),
                    player.goals.filter { it.own }.count(),
                    player.getMilisicondsInGoal()
            )
            allPlayerStatDto.add(playerStatDto)
        }

        val matchBasicStatDto = MatchBasicStatDto(
                match.name,
                at.wuzeln.manager.Utils.localDateTimeToLong(match.getCreatedDate()),
                if (match.startDate != null) at.wuzeln.manager.Utils.localDateTimeToLong(match.startDate as LocalDateTime) else null,
                if (match.endDate != null) at.wuzeln.manager.Utils.localDateTimeToLong(match.endDate as LocalDateTime) else null,
                duration,
                match.getWinner(),
                allPlayerStatDto,
                match.idlePersons.map { it.id }.sorted()
        )

        return matchBasicStatDto
    }

    @Transactional
    fun createMatchStats(matchId: Long) {

        val match = matchRepository.findById(matchId).orElseThrow { WuzelnException("There is no user match for id: $matchId") }

        if (match.endDate == null) {
            throw WuzelnException("Statistics can be calculated only for FINISHED matches. Match id=$matchId is not finished.")
        }

        val winner = match.getTeam(match.getWinner()!!)
        val looser = match.getTeam(match.getWinner()!!.otherColor())

        val winnerGoals = countGoals(winner, false)
        val winnerGoalsOwn = countGoals(winner, true)

        val looserGoals = countGoals(looser, false)
        val looserGoalsOwn = countGoals(looser, true)

        val matchStats = MatchStats(0L, match, winner, looser,
                winnerGoals + looserGoalsOwn,
                winnerGoals,
                winnerGoalsOwn,
                looserGoals + winnerGoalsOwn,
                looserGoals,
                looserGoalsOwn)

        log.info("createMatchStats $matchStats")

        matchStatsRepository.save(matchStats)
    }

    private fun countGoals(team: Team, ownGoals: Boolean): Int {
        return team.players.sumBy { it.goals.filter { goal -> goal.own == ownGoals }.count() }
    }

}
