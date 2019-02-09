package at.wuzeln.manager.service

import assertk.assertions.*
import at.wuzeln.manager.config.AppConfig
import at.wuzeln.manager.dao.*
import at.wuzeln.manager.dto.MatchCreationDto
import at.wuzeln.manager.model.enums.MatchCreationMethod
import mu.KotlinLogging
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional


@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfig::class])
@DirtiesContext
open class MatchServiceTest {

    private val log = KotlinLogging.logger {}
    private val SLEEP_CONSTANT = 500L

    @Autowired
    lateinit var cut: MatchService

    @Autowired
    lateinit var userAccountRepository: UserAccountRepository
    @Autowired
    lateinit var playerRepository: PlayerRepository
    @Autowired
    lateinit var personRepository: PersonRepository
    @Autowired
    lateinit var matchRepository: MatchRepository
    @Autowired
    lateinit var goalRepository: GoalRepository

    lateinit var teamBlue: List<Long>
    lateinit var teamRed: List<Long>
    lateinit var matchDto: MatchCreationDto

    @BeforeEach
    fun onSetUp() {
        val auth = UsernamePasswordAuthenticationToken("someUsername", null)
        SecurityContextHolder.getContext().authentication = auth
    }

    fun setup() {

        var people = createAllPeople()
        people.forEach { it.userAccount = userAccountRepository.save(it.userAccount) }
        people = personRepository.saveAll(people)

        teamBlue = arrayListOf(people[0].id, people[2].id, people[4].id, people[6].id)
        teamRed = arrayListOf(people[1].id, people[3].id, people[5].id)
        matchDto = MatchCreationDto("", teamBlue, teamRed, HashSet(), MatchCreationMethod.MANUAL, null)
    }

    @Test
    @Transactional
    open fun testGame() {
        setup()
        val matchId = cut.createMatch(matchDto)

        val players = playerRepository.findAll()
        assertk.assert(players).hasSize(7)

        cut.startMatch(matchId)

        sleep()
        cut.kickGoal(matchId, teamRed[2], false)
        sleep()
        cut.kickGoal(matchId, teamRed[2], false)
        sleep()
        cut.kickGoal(matchId, teamBlue[2], false)
        sleep()
        cut.kickGoal(matchId, teamRed[1], false)

        val match = matchRepository.getOne(matchId)

        assertk.assert(match.endDate).isNotNull()
        assertk.assert(match.teamRed.winner).isTrue()
        assertk.assert(match.teamBlue.winner).isFalse()

        assertPlayer(matchId, teamBlue[0], 0, 0, SLEEP_CONSTANT)
        assertPlayer(matchId, teamBlue[1], 0, 0, 0)
        assertPlayer(matchId, teamBlue[2], 1, 0, 2 * SLEEP_CONSTANT)
        assertPlayer(matchId, teamBlue[3], 0, 0, SLEEP_CONSTANT)

        assertPlayer(matchId, teamRed[0], 0, 0, 3 * SLEEP_CONSTANT)
        assertPlayer(matchId, teamRed[1], 1, 0, 0)
        assertPlayer(matchId, teamRed[2], 2, 0, SLEEP_CONSTANT)
    }

    @Test
    @Transactional
    open fun testGameOwnGoal() {
        setup()

        val matchId = cut.createMatch(matchDto)
        cut.startMatch(matchId)

        sleep()
        cut.kickGoal(matchId, teamRed[2], false)
        sleep()
        cut.kickGoal(matchId, teamRed[2], false)
        sleep()
        cut.kickGoal(matchId, teamBlue[2], false)
        sleep()
        cut.kickGoal(matchId, teamBlue[1], true)

        val match = matchRepository.getOne(matchId)

        assertk.assert(match.endDate).isNotNull()
        assertk.assert(match.teamRed.winner).isTrue()
        assertk.assert(match.teamBlue.winner).isFalse()

        assertPlayer(matchId, teamBlue[0], 0, 0, SLEEP_CONSTANT)
        assertPlayer(matchId, teamBlue[1], 0, 1, 0)
        assertPlayer(matchId, teamBlue[2], 1, 0, 2 * SLEEP_CONSTANT)
        assertPlayer(matchId, teamBlue[3], 0, 0, SLEEP_CONSTANT)

        assertPlayer(matchId, teamRed[0], 0, 0, 3 * SLEEP_CONSTANT)
        assertPlayer(matchId, teamRed[1], 0, 0, 0)
        assertPlayer(matchId, teamRed[2], 2, 0, SLEEP_CONSTANT)
    }


    @Test
    @Transactional
    open fun testRevertGoal_firstInMatch() {
        setup()
        val matchId = cut.createMatch(matchDto)
        cut.startMatch(matchId)

        sleep()
        cut.kickGoal(matchId, teamRed[2], false)

        assertPlayer(matchId, teamBlue[0], 0, 0, SLEEP_CONSTANT)
        assertPlayer(matchId, teamRed[2], 1, 0, 0)

        cut.revertLastGoal(matchId)

        assertPlayer(matchId, teamBlue[0], 0, 0, 0)
        assertPlayer(matchId, teamRed[2], 0, 0, 0)

        val goalie = playerRepository.findByTeam_Match_IdAndPerson_Id(matchId, teamBlue[0])
        assertk.assert(goalie.getMilisicondsInGoal()).isEqualTo(0L)
    }


    @Test
    @Transactional()
    open fun testRevertGoal() {
        setup()
        val matchId = cut.createMatch(matchDto)
        cut.startMatch(matchId)

        sleep()
        cut.kickGoal(matchId, teamRed[2], false)
        sleep()
        cut.kickGoal(matchId, teamBlue[2], false)
        sleep()
        cut.kickGoal(matchId, teamRed[1], false)

        assertPlayer(matchId, teamBlue[0], 0, 0, SLEEP_CONSTANT)
        assertPlayer(matchId, teamBlue[1], 0, 0, 0)
        assertPlayer(matchId, teamBlue[2], 1, 0, 0)
        assertPlayer(matchId, teamBlue[3], 0, 0, 2 * SLEEP_CONSTANT)

        assertPlayer(matchId, teamRed[0], 0, 0, 2 * SLEEP_CONSTANT)
        assertPlayer(matchId, teamRed[1], 1, 0, 0)
        assertPlayer(matchId, teamRed[2], 1, 0, 0)

        cut.revertLastGoal(matchId)

        assertPlayer(matchId, teamBlue[0], 0, 0, SLEEP_CONSTANT)
        assertPlayer(matchId, teamBlue[1], 0, 0, 0)
        assertPlayer(matchId, teamBlue[2], 1, 0, 0)
        assertPlayer(matchId, teamBlue[3], 0, 0, 0)

        assertPlayer(matchId, teamRed[0], 0, 0, 2 * SLEEP_CONSTANT)
        assertPlayer(matchId, teamRed[1], 0, 0, 0)
        assertPlayer(matchId, teamRed[2], 1, 0, 0)

        val goalie = playerRepository.findByTeam_Match_IdAndPerson_Id(matchId, teamBlue[3])
        assertk.assert(goalie.getMilisicondsInGoal()).isEqualTo(0L)
    }

    private fun sleep() {
        Thread.sleep(SLEEP_CONSTANT)
    }

    private fun assertPlayer(matchId: Long, personId: Long, expectedGoalsToOther: Int, expectedGoalsOwn: Int, milisecondsMin: Long) {
        val player = playerRepository.findByTeam_Match_IdAndPerson_Id(matchId, personId)
        val goals = goalRepository.countByPlayerAndOwn(player, false)
        val goalsOwn = goalRepository.countByPlayerAndOwn(player, true)
        assertk.assert(goals, "goals for player[${player.id}]").isEqualTo(expectedGoalsToOther)
        assertk.assert(goalsOwn, "goalsOwn for player[${player.id}]").isEqualTo(expectedGoalsOwn)
        assertk.assert(player.getMilisicondsInGoal(), "milisInGoal for player[${player.id}]").isGreaterThanOrEqualTo(milisecondsMin)
        log.info("player personId=$personId was ${player.getMilisicondsInGoal()}ms in goal, expected min $milisecondsMin")
    }

}