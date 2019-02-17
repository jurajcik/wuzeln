package at.wuzeln.manager.service

import at.wuzeln.manager.dao.*
import at.wuzeln.manager.dto.IdleScoreDto
import at.wuzeln.manager.dto.PersonalScoreDto
import at.wuzeln.manager.model.Player
import at.wuzeln.manager.model.stat.PlayerStats
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.transaction.Transactional

@Service
class CalculationService(
        private val goalRepository: GoalRepository,
        private val personRepository: PersonRepository,
        private val teamRepository: TeamRepository,
        private val matchRepository: MatchRepository,
        private val playerStatsRepository: PlayerStatsRepository
) {

    companion object {
        const val MAX_GOALS: Double = 10.0
    }

    private val log = KotlinLogging.logger {}


    @Transactional
    fun calculatePersonalScoreNormalized(personIds: List<Long>, startDate: LocalDateTime, endDate: LocalDateTime): List<PersonalScoreDto> {

        val persons = personRepository.findAllById(personIds)
        val scores = persons.map { calculatePersonalScore(it.id, startDate, endDate) }
        normalizeScores(scores)
        return scores
    }

    @Transactional
    fun normalizeScores(scores: List<PersonalScoreDto>) {

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
    fun calculatePersonalScore(player: Player): PlayerStats {

        val match = player.team.match;

        val goalsAllAvg = MAX_GOALS / player.team.players.size

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
            scoreDefensive = player.getMilisicondsInGoal() / (timeInGoalMillisAvg.toDouble())
        }

        val personalScore = PlayerStats(
                0,
                player,
                goalsSum,
                goalsOwnSum,
                goalsAllAvg,
                timeInGoalMillisSum,
                timeInGoalMillisAvg,
                scoreOffensive,
                scoreDefensive)

        log.info("calculatePersonalScore(player=$player): $personalScore")
        return personalScore
    }

    @Transactional
    fun calculatePersonalScore_new(personId: Long, startDate: LocalDateTime, endDate: LocalDateTime): PersonalScoreDto {

        val statsList = playerStatsRepository.findByPersonIdAndRange(personId, startDate, endDate)
        val stats = statsList[0]

        return PersonalScoreDto(
                personId,
                stats[0] as Int,
                stats[1] as Int,
                stats[2] as Int,
                stats[3] as Double,
                stats[4] as Int,
                stats[5] as Int,
                stats[6] as Double,
                stats[7] as Double)
    }

    @Transactional
    fun calculatePersonalScore(personId: Long, startDate: LocalDateTime, endDate: LocalDateTime): PersonalScoreDto {

        val person = personRepository.findById(personId).orElseThrow { WuzelnException("No person with id $personId exists") }
        val teams = teamRepository.findByPersonInRange(personId, startDate, endDate)

        val goalsAllAvg = teams.sumByDouble { MAX_GOALS / it.players.size }

        val goalsSum = goalRepository.countGoalsByPersonInRange(personId, startDate, endDate)
        val goalsOwnSum = goalRepository.countGoalsOwnByPersonInRange(personId, startDate, endDate)
        var scoreOffensive = 0.0

        if (goalsAllAvg != 0.0) {
            scoreOffensive = (goalsSum - goalsOwnSum) / goalsAllAvg
        }

        val timeInGoalMillisAvg = teams.sumBy { (it.match.startDate?.until(it.match.endDate, ChronoUnit.MILLIS) as Long).toInt() / (it.players.size) }
        var timeInGoalMillisSum = 0
        var scoreDefensive = 0.0

        if (timeInGoalMillisAvg != 0) {
            timeInGoalMillisSum = personRepository.sumMilisecondsInGoal(personId, startDate, endDate)
            scoreDefensive = timeInGoalMillisSum / (timeInGoalMillisAvg.toDouble())
        }

        val personalScore = PersonalScoreDto(
                person.id,
                teams.size,
                goalsSum,
                goalsOwnSum,
                goalsAllAvg,
                timeInGoalMillisSum,
                timeInGoalMillisAvg,
                scoreOffensive,
                scoreDefensive)

        log.info("calculatePersonalScore(personId=$personId, startDate=$startDate, endDate=$endDate): $personalScore")
        return personalScore;
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