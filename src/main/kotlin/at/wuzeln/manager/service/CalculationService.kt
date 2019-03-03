package at.wuzeln.manager.service

import at.wuzeln.manager.dao.*
import at.wuzeln.manager.dto.IdleScoreDto
import at.wuzeln.manager.dto.PersonalScoreDto
import at.wuzeln.manager.model.Player
import at.wuzeln.manager.model.stat.PlayerStats
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.annotation.PostConstruct
import javax.transaction.Transactional

@Service
class CalculationService(
        private val personRepository: PersonRepository,
        private val matchRepository: MatchRepository,
        private val playerStatsRepository: PlayerStatsRepository,
        @Value("\${match.goals.max}")
        private val maxGoalsInMatch: Double,
        @Value("\${match.players.team.max}")
        private val maxPlayersInTeam: Double

) {

    var SCORE_OFFENSIVE_MIN: Double = 0.0
    var SCORE_OFFENSIVE_MAX: Double = 0.0
    var SCORE_DEFENSIVE_MIN: Double = 0.0
    var SCORE_DEFENSIVE_MAX: Double = 0.0

    @PostConstruct
    fun setup() {
        SCORE_OFFENSIVE_MIN = (0 - maxGoalsInMatch) / (maxGoalsInMatch / maxPlayersInTeam)
        SCORE_OFFENSIVE_MAX = (maxGoalsInMatch - 0) / (maxGoalsInMatch / maxPlayersInTeam)
        SCORE_DEFENSIVE_MIN = 0.0
        SCORE_DEFENSIVE_MAX = 1 / (1 / maxPlayersInTeam)
    }

    private val log = KotlinLogging.logger {}


    @Transactional
    fun getPersonalScoreNormalized(personIds: List<Long>, startDate: LocalDateTime, endDate: LocalDateTime): List<PersonalScoreDto> {

        val persons = personRepository.findAllById(personIds)
        val scores = persons.map { getPersonalScore(it.id, startDate, endDate) }
        normalizeScores(scores)
        return scores
    }

    @Transactional
    protected fun normalizeScores(scores: List<PersonalScoreDto>) {

        val count = scores.size
        val avgOffensive = scores.sumByDouble { it.scoreOffensive } / count
        val avgDefensive = scores.sumByDouble { it.scoreDefensive } / count

        scores.forEach {
            it.scoreOffensiveNormalized = if (avgOffensive != 0.0) it.scoreOffensive / avgOffensive else 0.0
            it.scoreDefensiveNormalized = if (avgDefensive != 0.0) it.scoreDefensive / avgDefensive else 0.0
            it.scoreNormalized = it.scoreOffensiveNormalized as Double + it.scoreDefensiveNormalized as Double
        }
    }

    @Transactional
    fun getPersonalScore(personId: Long, startDate: LocalDateTime, endDate: LocalDateTime): PersonalScoreDto {

        val multipleStats = playerStatsRepository.findByPersonIdAndRange(personId, startDate, endDate)
        val stats = multipleStats[0]
        val matchesPlayed = (stats[0] as Long).toInt()

        return if (matchesPlayed > 0) {
            PersonalScoreDto(
                    personId,
                    matchesPlayed,
                    (stats[1] as Long).toInt(),
                    (stats[2] as Long).toInt(),
                    (stats[3] as Long).toInt(),
                    round(stats[4] as Double / matchesPlayed),
                    round(stats[5] as Double / matchesPlayed)
            )
        } else {
            PersonalScoreDto(personId, matchesPlayed, 0, 0, 0, 0.0, 0.0)
        }
    }

    @Transactional
    fun calculatePersonalScore(players: Iterable<Player>) {
        val stats = ArrayList<PlayerStats>()

        for (one in players) {
            stats.add(calculatePersonalScore(one))
        }

        playerStatsRepository.saveAll(stats)
    }


    protected fun calculatePersonalScore(player: Player): PlayerStats {

        val match = player.team.match

        val goalsAllAvg = maxGoalsInMatch / player.team.players.size

        val goalsSum = player.goals.filter { !it.own }.size
        val goalsOwnSum = player.goals.filter { it.own }.size
        var scoreOffensive = 0.0

        if (goalsAllAvg != 0.0) {
            scoreOffensive = (goalsSum - goalsOwnSum) / goalsAllAvg
        }

        val timeInGoalMillisAvg = (match.startDate?.until(match.endDate, ChronoUnit.MILLIS) as Long).toInt() / (player.team.players.size)
        var timeInGoalMillisSum = 0
        var scoreDefensive = 0.0

        if (timeInGoalMillisAvg != 0) {
            timeInGoalMillisSum = player.getMilisicondsInGoal().toInt()
            scoreDefensive = player.getMilisicondsInGoal() / (timeInGoalMillisAvg.toDouble())
        }

        val scoreOffensiveNormalized = round(normalizeByRange(scoreOffensive, SCORE_OFFENSIVE_MIN, SCORE_OFFENSIVE_MAX))
        val scoreDefensiveNormalized = round(normalizeByRange(scoreDefensive, SCORE_DEFENSIVE_MIN, SCORE_DEFENSIVE_MAX))

        val personalScore = PlayerStats(
                0,
                player,
                goalsSum,
                goalsOwnSum,
                timeInGoalMillisSum,
                scoreOffensive,
                scoreDefensive,
                scoreOffensiveNormalized,
                scoreDefensiveNormalized)

        log.info("calculatePersonalScore(player=$player): $personalScore")
        return personalScore
    }

    private fun round(number: Double): Double {
        return Math.round(number * 100000.0) / 100000.0
    }

    private fun normalizeByRange(x: Double, min: Double, max: Double): Double {
        return (x - min) / (max - min)
    }

    @Transactional
    fun calculateIdleScore(personIds: List<Long>, startDate: LocalDateTime, endDate: LocalDateTime): MutableList<IdleScoreDto> {

        val persons = personRepository.findAllById(personIds)
        val personScores = ArrayList<IdleScoreDto>()

        for (person in persons) {
            var matchDates = matchRepository.getPlayedMatchesForPersonInRange(person.id, startDate, endDate)

            var score = matchDates.sumByDouble {
                var daysDistance = ChronoUnit.DAYS.between(it.toLocalDate(), endDate.toLocalDate())
                if (daysDistance == 0L) {
                    2.0
                } else {
                    1.0 / daysDistance
                }
            }
            personScores.add(IdleScoreDto(person.id, score))
        }

        return personScores
    }


}