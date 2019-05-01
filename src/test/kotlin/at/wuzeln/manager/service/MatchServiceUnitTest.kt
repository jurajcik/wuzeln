package at.wuzeln.manager.service

import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import at.wuzeln.manager.TestUtil
import at.wuzeln.manager.dto.MatchCreationDto
import at.wuzeln.manager.model.Match
import at.wuzeln.manager.model.Player
import at.wuzeln.manager.model.Team
import at.wuzeln.manager.model.enums.MatchCreationMethod
import at.wuzeln.manager.model.enums.Position
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.security.SecureRandom

class MatchServiceUnitTest : AbstractServiceUnitTest() {

    var registrationService: RegistrationService = mock()
    var cut: MatchService = MatchService(
            goalRepository, personRepository, playerRepository, matchRepository, registrationService,  mock(),10)

    @BeforeEach
    fun setup() {

        val players = TestUtil.createAllPeople()

        whenever(personRepository.findAllById(any())).thenAnswer { invocationOnMock ->
            val ids: Collection<Long> = invocationOnMock.getArgument(0)
            players
                    .filter { player -> ids.contains(player.id) }
                    .shuffled(SecureRandom())
        }
    }

    @Test
    fun testCreatePlayersInMatch() {

        val matchName = "something"

        whenever(matchRepository.save(any<Match>())).thenAnswer {

            val match: Match = it.getArgument(0)

            assertk.assert(match.teamBlue.players).hasSize(4)
            assertk.assert(match.teamRed.players).hasSize(4)
            assertk.assert(match.name).isEqualTo(matchName)
            assertk.assert(match.teamBlue.otherTeam).isEqualTo(match.teamRed)
            assertk.assert(match.teamRed.otherTeam).isEqualTo(match.teamBlue)

            assertk.assert(getPlayerOnStartingPosition(match.teamBlue, Position.GOALKEEPER).person.id).isEqualTo(1L)
            assertk.assert(getPlayerOnStartingPosition(match.teamBlue, Position.DEFENDER).person.id).isEqualTo(3L)
            assertk.assert(getPlayerOnStartingPosition(match.teamBlue, Position.MIDFIELDER).person.id).isEqualTo(5L)
            assertk.assert(getPlayerOnStartingPosition(match.teamBlue, Position.FORWARD).person.id).isEqualTo(7L)

            assertk.assert(getPlayerOnStartingPosition(match.teamRed, Position.GOALKEEPER).person.id).isEqualTo(2L)
            assertk.assert(getPlayerOnStartingPosition(match.teamRed, Position.DEFENDER).person.id).isEqualTo(4L)
            assertk.assert(getPlayerOnStartingPosition(match.teamRed, Position.MIDFIELDER).person.id).isEqualTo(6L)
            assertk.assert(getPlayerOnStartingPosition(match.teamRed, Position.FORWARD).person.id).isEqualTo(8L)

            match
        }

        val teamBlue = arrayListOf(1L, 3L, 5L, 7L)
        val teamRed = arrayListOf(2L, 4L, 6L, 8L)
        val matchDto = MatchCreationDto(matchName, teamBlue, teamRed, HashSet(), MatchCreationMethod.MANUAL, null)
        cut.createMatch(matchDto)
    }

}


fun getPlayerOnStartingPosition(team: Team, position: Position): Player {
    return team.players.first { it.startingPosition == position }
}