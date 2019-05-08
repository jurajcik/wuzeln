package at.wuzeln.manager.service

import at.wuzeln.manager.dao.MatchRepository
import at.wuzeln.manager.dao.PersonRepository
import at.wuzeln.manager.dao.RegistrationRepository
import at.wuzeln.manager.dto.MatchCreationDto
import at.wuzeln.manager.dto.PersonalScoreDto
import at.wuzeln.manager.dto.RegistrationDto
import at.wuzeln.manager.dto.RegistrationUpdateDto
import at.wuzeln.manager.model.Match
import at.wuzeln.manager.model.Person
import at.wuzeln.manager.model.Registration
import at.wuzeln.manager.model.enums.MatchCreationMethod
import at.wuzeln.manager.model.enums.SecurityRole
import mu.KotlinLogging
import org.apache.commons.collections4.ListUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.*
import javax.transaction.Transactional
import kotlin.collections.ArrayList


@Service
class RegistrationService(
        private val personRepository: PersonRepository,
        private val registrationRepository: RegistrationRepository,
        private val matchRepository: MatchRepository,
        private val calculationService: CalculationService,
        @Value("\${match.players.min}")
        private val playersMin: Int,
        @Value("\${match.players.max}")
        private val playersMax: Int,
        @Value("\${registration.proposal_calculation.months}")
        private val proposalCalculationMonths: Long
) {

    private val log = KotlinLogging.logger {}

    @Transactional
    fun findAllOpened(): List<RegistrationDto> {
        return registrationRepository.findOpened().map { toDto(it) }
    }

    @Transactional
    fun getRegistration(regId: Long): RegistrationDto {
        val reg = registrationRepository.findById(regId).orElseThrow { WuzelnException("The registration $regId does not exist") }
        return toDto(reg)
    }

    @Transactional
    fun createRegistration(name: String): Long {

        if (!registrationRepository.findOpened().isEmpty()) {
            throw WuzelnException("There is already another opened registration. You can not open a new one.")
        }

        val reg = Registration(0, name)
        return registrationRepository.save(reg).id
    }


    @Transactional
    fun closeRegistration(regId: Long, match: Match) {

        val reg = getRegistrationOpened(regId)

        reg.match = match
        registrationRepository.save(reg)
    }


    @Transactional
    fun updateRegistration(regId: Long, regDto: RegistrationUpdateDto): RegistrationDto {

        var reg = getRegistrationOpened(regId)
        reg.persons.clear()

        for (one in regDto.persons) {
            val person = personRepository.findById(one.id).orElseThrow { WuzelnException("The person ${one.id} does not exist") } as Person
            if (one.register) {
                reg.persons.add(person)
            }
        }

        if (regDto.name != null) {
            reg.name = regDto.name
        }

        reg = registrationRepository.save(reg)
        return toDto(reg)
    }

    @Transactional
    fun generateMatchProposal(regId: Long, method: MatchCreationMethod): MatchCreationDto {
        val reg = getRegistrationOpened(regId)

        val activePersons = reg.persons.filter { it.userAccount.hasRole(SecurityRole.ACTIVE_USER) }

        if (activePersons.size < playersMin) {
            throw WuzelnException("The registration $regId contains ${activePersons.size} players that is less than the minimal number of players $playersMin")
        }

        val persons = ArrayList<Person>(activePersons)
        persons.sortByDescending { it.idleMatches.size }

        val playingPersons = determinPlayingPersons(persons)
        val idlePersonsIds = separateIdlePersonsIds(persons, playingPersons)
        var teams = when (method) {

            MatchCreationMethod.MANUAL ->
                throw WuzelnException("MANUAL method can not be used to generate a match proposal")

            MatchCreationMethod.RANDOM ->
                generateRandomTeams(playingPersons)

            MatchCreationMethod.BALANCED ->
                generateBalancedTeams(
                        sortByOffensiveAndDefensive(
                                calculateScoresForProposal(playingPersons)))

            MatchCreationMethod.BALANCED_RANDOMIZED ->
                generateBalancedRandomizedTeams(
                        sortByOffensiveAndDefensive(
                                calculateScoresForProposal(playingPersons)))

            MatchCreationMethod.BAL_RAN_WITH_VICTORIES ->
                generateBalancedRandomizedTeams(
                        sortByOffensiveAndDefensiveAndVictory(
                                calculateScoresForProposal(playingPersons)))
        }

        val dto = MatchCreationDto(reg.name, teams[0], teams[1], idlePersonsIds, method, regId)

        log.info("generateMatchProposal(reg=$reg, method=$method): $dto")

        return dto
    }

    fun determinPlayingPersons(persons: List<Person>): MutableList<Person> {

        val tempMap = persons.map { it.id to it }.toMap()
        val endDate = LocalDateTime.now()
        val startDate = endDate.minusMonths(proposalCalculationMonths)

        return calculationService.calculateIdleScore(tempMap.keys.toList(), startDate, endDate)
                .sortedBy { it.score }
                .take(playersMax)
                .map {
                    log.debug("active score for person ${it.id} is ${it.score}")
                    tempMap.getValue(it.id)
                }.toMutableList()
    }


    fun determinPlayingPersonsByIdleMatches(persons: List<Person>): MutableList<Person> {

        val countPersonMap = persons.groupBy { it.idleMatches.size }
        val sortedCounts = countPersonMap.keys.sortedDescending()
        val result = ArrayList<Person>()

        for (oneCount in sortedCounts) {
            if (result.size == playersMax) {
                break

            } else if (countPersonMap[oneCount]!!.size + result.size <= playersMax) {
                result.addAll(countPersonMap[oneCount]!!)

            } else {
                val personDatesMap = HashMap<Person, LocalDateTime>()
                for (one in countPersonMap[oneCount]!!) {
                    val date = matchRepository.findDateOfLastPlayedMatchForPerson(one) ?: LocalDateTime.MIN;
                    personDatesMap.put(one, date!!)
                }
                val sortedPersons = countPersonMap[oneCount]!!.sortedBy { personDatesMap[it] }
                val lastPersonIndex = (playersMax - result.size)
                result.addAll(sortedPersons.subList(0, lastPersonIndex))
            }
        }
        return result
    }

    fun separateIdlePersonsIds(persons: List<Person>, playingPersons: List<Person>): Set<Long> {
        val ids = playingPersons.map { it.id }.toSet()
        return persons.filterNot { ids.contains(it.id) }.map { it.id }.toSet()
    }

    fun generateRandomTeams(playing: MutableList<Person>): List<List<Long>> {

        playing.shuffle(SecureRandom())
        val halfCount = playing.size / 2
        val teamBlue = playing.subList(0, halfCount).map { it.id }
        val teamRed = playing.subList(halfCount, playing.size).map { it.id }
        return arrayListOf(teamBlue, teamRed)
    }

    private fun calculateScoresForProposal(playing: MutableList<Person>): List<PersonalScoreDto> {
        val endDate = LocalDateTime.now()
        val startDate = endDate.minusMonths(proposalCalculationMonths)
        return calculationService.getPersonalScoreNormalized(playing.map { it.id }, startDate, endDate)
    }

    private fun sortByOffensiveAndDefensive(personalScores: List<PersonalScoreDto>): List<PersonalScoreDto> {
        return personalScores.sortedByDescending { it.scoreNormalized as Double }
    }

    private fun sortByOffensiveAndDefensiveAndVictory(personalScores: List<PersonalScoreDto>): List<PersonalScoreDto> {
        personalScores.forEach { it.scoreNormalized = it.scoreNormalized!! + it.scoreVictoryNormalized!! }
        return personalScores.sortedByDescending { it.scoreNormalized as Double }
    }

    fun generateBalancedTeams(sortedScores: List<PersonalScoreDto>): List<List<Long>> {

        val teamA = LinkedList<PersonalScoreDto>()
        val teamB = LinkedList<PersonalScoreDto>()

        teamA.add(sortedScores[0])
        teamB.add(sortedScores[1])
        teamB.add(sortedScores[2])
        teamA.add(sortedScores[3])

        if (sortedScores.size >= 5) {
            teamA.add(sortedScores[4])

            if (sortedScores.size >= 6) {
                teamB.add(sortedScores[5])

                if (sortedScores.size >= 7) {
                    teamB.add(sortedScores[6])

                    if (sortedScores.size == 8) {
                        teamA.add(sortedScores[7])
                    }
                }
            }
        }

        reorderTeam(teamA)
        reorderTeam(teamB)

        val result = arrayListOf<List<PersonalScoreDto>>(teamA, teamB)
        result.shuffle(SecureRandom())
        return result.map { it.map { it.id } }
    }

    private fun reorderTeam(team: LinkedList<PersonalScoreDto>) {
        val maxOffensive = team.maxBy { it.scoreOffensiveNormalized as Double }
        team.remove(maxOffensive)
        val maxDefensive = team.maxBy { it.scoreDefensiveNormalized as Double }
        team.remove(maxDefensive)

        team.addFirst(maxDefensive)
        team.addLast(maxOffensive)
    }

    fun generateBalancedRandomizedTeams(sortedScores: List<PersonalScoreDto>): List<List<Long>> {

        val teamA = LinkedList<PersonalScoreDto>()
        val teamB = LinkedList<PersonalScoreDto>()

        val partitions = ListUtils.partition(sortedScores, 2)

        for (pair in partitions) {
            val firstToTeamA = SecureRandom().nextBoolean()

            if (pair.size == 2) {
                teamA.add(pair[if (firstToTeamA) 0 else 1])
                teamB.add(pair[if (firstToTeamA) 1 else 0])
            } else {
                if (firstToTeamA) {
                    teamA.add(pair[0])
                } else {
                    teamB.add(pair[0])
                }
            }
        }

        teamA.shuffle(SecureRandom())
        teamB.shuffle(SecureRandom())

        val result = arrayListOf<List<PersonalScoreDto>>(teamA, teamB)
        result.shuffle(SecureRandom())

        log.info("team blue sum=${result[0].sumByDouble { it.scoreNormalized as Double }}, " +
                "offensive=${result[0].sumByDouble { it.scoreOffensiveNormalized as Double }}, " +
                "defensive=${result[0].sumByDouble { it.scoreDefensiveNormalized as Double }}")
        log.info("team red sum=${result[1].sumByDouble { it.scoreNormalized as Double }}, " +
                "offensive=${result[1].sumByDouble { it.scoreOffensiveNormalized as Double }}, " +
                "defensive=${result[1].sumByDouble { it.scoreDefensiveNormalized as Double }}")

        return result.map { it.map { it.id } }
    }

    private fun getRegistrationOpened(regId: Long): Registration {

        val reg = registrationRepository.findById(regId)
                .orElseThrow { WuzelnException("The registration $regId does not exist") } as Registration

        if (reg.isClosed()) {
            throw WuzelnException("The registration $regId is already closed")
        }

        return reg
    }

    private fun toDto(reg: Registration): RegistrationDto {
        return RegistrationDto(
                reg.id,
                reg.name,
                at.wuzeln.manager.Utils.localDateTimeToLong(reg.getCreatedDate()),
                reg.isOpened(),
                reg.persons.map { it.id }
        )
    }

}
