package at.wuzeln.manager.service

import at.wuzeln.manager.dao.*
import com.nhaarman.mockito_kotlin.mock
import org.springframework.beans.factory.annotation.Value
import java.time.LocalDateTime

open class AbstractServiceUnitTest {

    protected var goalRepository: GoalRepository = mock()
    protected var personRepository: PersonRepository = mock()
    protected var playerRepository: PlayerRepository = mock()
    protected var matchRepository: MatchRepository = mock()
    protected var registrationRepository: RegistrationRepository = mock()
    protected var teamRepository: TeamRepository = mock()
    protected val playerStatsRepository: PlayerStatsRepository = mock()

    protected var NOW = LocalDateTime.now()
    protected val MAX_PLAYERS_IN_TEAM = 4
    protected val MAX_GOALS_IN_MATCH = 10

}
