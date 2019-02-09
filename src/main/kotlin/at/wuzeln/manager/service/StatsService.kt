package at.wuzeln.manager.service

import at.wuzeln.manager.dao.MatchRepository
import at.wuzeln.manager.dto.MatchBasicStatDto
import at.wuzeln.manager.model.Match
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*
import javax.transaction.Transactional

@Service
class StatsService(
        private val matchRepository: MatchRepository,
        private val calculationService: CalculationService
) {

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

}