package at.wuzeln.manager.service

import at.wuzeln.manager.dao.*
import com.nhaarman.mockito_kotlin.mock
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

}
