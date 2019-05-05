package at.wuzeln.manager.job;

import assertk.assertions.isEqualTo
import at.wuzeln.manager.TestUtil
import at.wuzeln.manager.config.AppConfig
import at.wuzeln.manager.dao.*
import at.wuzeln.manager.dto.MatchCreationDto
import at.wuzeln.manager.dto.RegistrationUpdateDto
import at.wuzeln.manager.model.enums.MatchCreationMethod
import at.wuzeln.manager.service.MatchService
import at.wuzeln.manager.service.RegistrationService
import mu.KotlinLogging
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfig::class])
@DirtiesContext
open class RemoveStaleMatchesTest {

    private val log = KotlinLogging.logger {}

    @Autowired
    lateinit var cut: RemoveStaleMatches
    @Autowired
    lateinit var registrationService: RegistrationService
    @Autowired
    lateinit var matchService: MatchService
    @Autowired
    lateinit var matchRepository: MatchRepository
    @Autowired
    lateinit var teamRepository: TeamRepository
    @Autowired
    lateinit var playerRepository: PlayerRepository
    @Autowired
    lateinit var goalRepository: GoalRepository
    @Autowired
    lateinit var personRepository: PersonRepository
    @Autowired
    lateinit var registrationRepository: RegistrationRepository
    @Autowired
    lateinit var testUtil: TestUtil

    lateinit var matchDto: MatchCreationDto

    @BeforeEach
    fun onSetUp() {
        TestUtil.mockSecurityContext()
    }

    fun createRegistrationAndMatchStartAndKickOneGoal() {

        var people = testUtil.savePeopleWithAccounts(TestUtil.createAllPeople())

        val registrationId = registrationService.createRegistration("testReg")
        val registrationDto = RegistrationUpdateDto("testReg", people.map { RegistrationUpdateDto.RegPersonDto(it.id, true) })

        registrationService.updateRegistration(registrationId, registrationDto)
        matchDto = registrationService.generateMatchProposal(registrationId, MatchCreationMethod.BALANCED)

        val matchId = matchService.createMatch(matchDto)
        matchService.startMatch(matchId)
        matchService.kickGoal(matchId, matchDto.teamRed[2], false)
    }

    @Test
    fun testRemoveStaleMatches() {

        createRegistrationAndMatchStartAndKickOneGoal()

        assertk.assert(registrationRepository.count()).isEqualTo(1L)
        assertk.assert(matchRepository.count()).isEqualTo(1L)
        assertk.assert(teamRepository.count()).isEqualTo(2L)
        assertk.assert(playerRepository.count()).isEqualTo(8L)
        assertk.assert(personRepository.count()).isEqualTo(10L)
        assertk.assert(goalRepository.count()).isEqualTo(1L)

        val staleDate = LocalDateTime.now()

        cut.removeStaleMatches(staleDate)

        assertk.assert(registrationRepository.count()).isEqualTo(0L)
        assertk.assert(matchRepository.count()).isEqualTo(0L)
        assertk.assert(teamRepository.count()).isEqualTo(0L)
        assertk.assert(playerRepository.count()).isEqualTo(0L)
        assertk.assert(personRepository.count()).isEqualTo(10L)
        assertk.assert(goalRepository.count()).isEqualTo(0L)

    }
}
