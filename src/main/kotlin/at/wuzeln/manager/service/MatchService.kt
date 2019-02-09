package at.wuzeln.manager.service

import at.wuzeln.manager.dao.GoalRepository
import at.wuzeln.manager.dao.MatchRepository
import at.wuzeln.manager.dao.PersonRepository
import at.wuzeln.manager.dao.PlayerRepository
import at.wuzeln.manager.dto.GoalDto
import at.wuzeln.manager.dto.MatchCreationDto
import at.wuzeln.manager.dto.MatchDetailedDto
import at.wuzeln.manager.dto.MatchDto
import at.wuzeln.manager.dto.enums.MatchState
import at.wuzeln.manager.model.*
import at.wuzeln.manager.model.enums.Position
import at.wuzeln.manager.model.enums.TeamColor
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.transaction.Transactional

@Service
class MatchService(
        private val goalRepository: GoalRepository,
        private val personRepository: PersonRepository,
        private val playerRepository: PlayerRepository,
        private val matchRepository: MatchRepository,
        private val registrationService: RegistrationService,
        @Value("\${match.goals.max}")
        private val maxGoalsInMatch: Int
) {

    @Transactional
    fun findAll(): List<MatchDto> {
        val matches = matchRepository.findAll()
        return matches.map { toDto(it) }
    }

    @Transactional
    fun getMatch(matchId: Long): MatchDetailedDto {
        val match = matchRepository.getOne(matchId)
        return toDetailedDto(match)
    }

    @Transactional
    fun createMatch(matchDto: MatchCreationDto): Long {

        val allPersonIds = matchDto.idlePersons.union(matchDto.teamBlue).union(matchDto.teamRed)
        val persons = personRepository.findAllById(allPersonIds)
        val personMap = persons.map { it.id to it }.toMap()

        val playersBlue = createPlayers(matchDto.teamBlue, personMap)
        val playersRed = createPlayers(matchDto.teamRed, personMap)

        val teamBlue = Team(0, TeamColor.BLUE, playersBlue)
        val teamRed = Team(0, TeamColor.RED, playersRed)
        teamBlue.otherTeam = teamRed
        teamRed.otherTeam = teamBlue

        val idlePersons = persons.filter { matchDto.idlePersons.contains(it.id) }

        val match = Match(0, matchDto.name, teamBlue, teamRed, idlePersons, matchDto.method)

        if (matchDto.registrationId != null) {
            registrationService.closeRegistration(matchDto.registrationId, match)
        }

        return matchRepository.save(match).id
    }

    @Transactional
    fun startMatch(matchId: Long) {
        val match = matchRepository.getOne(matchId)

        if (match.hasStarted()) {
            throw WuzelnException("The match $matchId can not be started, because it has already been started before")
        }

        match.startDate = LocalDateTime.now()
        matchRepository.save(match)
    }

    @Transactional
    fun kickGoal(matchId: Long, personId: Long, own: Boolean) {

        val player = playerRepository.findByTeam_Match_IdAndPerson_Id(matchId, personId)
        val match = player.team.match

        verifyMatchIsRunning(match)

        val goalDate = LocalDateTime.now()
        val currentPosition = getCurrentPosition(player)
        val goalieTeam = if (own) player.team else player.team.otherTeam
        val goalie = getGoalie(goalieTeam)

        val goal = Goal(0, goalDate, own, currentPosition, player, goalie)

        goalie.addMilisecondsInGoal(milisecondsInGoal(goalie, goalDate) as Long)
        player.goals.add(goal)

        playerRepository.save(player)
        playerRepository.save(goalie)

        val nrReceivedGoals = goalRepository.countReceivedGoals(goalie.team)

        if (maxGoalsInMatch == nrReceivedGoals) {
            match.endDate = goalDate

            val winningTeam = if (own) player.team.otherTeam else player.team
            winningTeam.winner = true

            matchRepository.save(match)
            playerRepository.save(player)

            val otherGoalie = getGoalie(winningTeam)
            otherGoalie.addMilisecondsInGoal(milisecondsInGoal(otherGoalie, goalDate) as Long)
            playerRepository.save(otherGoalie)
        }
    }

    @Transactional
    fun revertLastGoal(matchId: Long) {
        val match = matchRepository.findById(matchId).orElseThrow { throw WuzelnException("The match $matchId does not exist") }

        verifyMatchIsRunning(match)

        val lastGoal = goalRepository.lastReceivedGoal(match)
                ?: throw WuzelnException("There are no goals to revert for the match $matchId")

        val goals = goalRepository.findByGoalie_Team(lastGoal.goalie.team)
        val previousGoal = goals.filter { it.date.isBefore(lastGoal.date) }.maxBy { it.date }
        val previousDate = previousGoal?.date ?: match.startDate as LocalDateTime

        val milliseconds = previousDate.until(lastGoal.date, ChronoUnit.MILLIS)

        lastGoal.goalie.subtractMilisecondsInGoal(milliseconds)
        lastGoal.player.goals.remove(lastGoal)

        playerRepository.save(lastGoal.goalie)
        playerRepository.save(lastGoal.player)
    }

    private fun verifyMatchIsRunning(match: Match) {

        if (!match.hasStarted()) {
            throw WuzelnException("The match ${match.id} has not started yet")
        }

        if (match.hasFinished()) {
            throw WuzelnException("The match ${match.id} has already finished")
        }
    }

    private fun toDto(match: Match): MatchDto {
        return MatchDto(
                match.id,
                match.name,
                at.wuzeln.manager.Utils.localDateTimeToLong(match.getCreatedDate()),
                MatchState.ofMatch(match))
    }

    private fun toDetailedDto(match: Match): MatchDetailedDto {
        val blueTeam = toDto(match.teamBlue)
        val redTeam = toDto(match.teamRed)
        val lastGoal = goalRepository.lastReceivedGoal(match)
        return MatchDetailedDto(
                match.id,
                match.name,
                at.wuzeln.manager.Utils.localDateTimeToLong(match.getCreatedDate()),
                MatchState.ofMatch(match),
                blueTeam,
                redTeam,
                if (lastGoal != null) toDto(lastGoal) else null)
    }

    private fun toDto(team: Team): MatchDetailedDto.TeamDto {
        val players = team.players.map {
            MatchDetailedDto.PlayerDto(it.person.id, getCurrentPosition(it))
        }
        val goalsSum = goalRepository.countReceivedGoals(team.otherTeam)

        return MatchDetailedDto.TeamDto(players, goalsSum)
    }

    private fun toDto(goal: Goal): GoalDto {
        return GoalDto(
                goal.id,
                goal.date,
                goal.own,
                goal.position,
                goal.player.person.id,
                goal.goalie.person.id)
    }

    private fun createPlayers(initialPositions: List<Long>, personsMap: Map<Long, Person>): MutableList<Player> {

        val personsInMatchMap = HashMap<Long, Player>()

        for ((index, value) in initialPositions.withIndex()) {

            personsInMatchMap[value] = Player(0,
                    Position.toPosition(index, initialPositions.size),
                    personsMap.getValue(value))
        }

        for ((index, value) in initialPositions.withIndex()) {
            val playerIdRight = if (index == initialPositions.lastIndex) initialPositions.first() else initialPositions[index + 1]
            val playerIdLeft = if (index == 0) initialPositions.last() else initialPositions[index - 1]

            val onePlayerInMatch = personsInMatchMap.getValue(value)
            onePlayerInMatch.playerLeft = personsInMatchMap.getValue(playerIdLeft)
            onePlayerInMatch.playerRight = personsInMatchMap.getValue(playerIdRight)
        }

        return personsInMatchMap
                .map { it.value }
                .toMutableList()
    }


    private fun getCurrentPosition(player: Player): Position {
        val nrGoals = goalRepository.countReceivedGoals(player.team)

        var nextPlayer = player
        for (i in 1..nrGoals) {
            nextPlayer = nextPlayer.playerRight
        }
        return nextPlayer.startingPosition
    }

    private fun getGoalie(team: Team): Player {
        val nrGoals = goalRepository.countReceivedGoals(team)

        var nextGoalie: Player = team.players.first { it.startingPosition == Position.GOALKEEPER }
        for (i in 1..nrGoals) {
            nextGoalie = nextGoalie.playerLeft
        }
        return nextGoalie
    }

    private fun milisecondsInGoal(goalie: Player, goalDate: LocalDateTime): Long? {

        val lastGoal = goalRepository.lastReceivedGoal(goalie.team)
        val lastGoalDate = lastGoal?.date ?: goalie.team.match.startDate

        return lastGoalDate?.until(goalDate, ChronoUnit.MILLIS)
    }
}