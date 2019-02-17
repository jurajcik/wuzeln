package at.wuzeln.manager.service

import assertk.assertions.isEqualTo
import at.wuzeln.manager.model.Person
import at.wuzeln.manager.model.UserAccount
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class RegistrationServiceUnitTest : AbstractServiceUnitTest() {

    var calculationService= CalculationService(goalRepository, personRepository, teamRepository, matchRepository, playerStatsRepository)
    var cut: RegistrationService = RegistrationService(personRepository, registrationRepository, matchRepository, calculationService,
            1, 1, 1)

    lateinit var persons: List<Person>

    @BeforeEach
    fun setup() {
        persons = listOf<Person>(
                Person(1, UserAccount(1, ""), ""),
                Person(2, UserAccount(2, ""), ""),
                Person(3, UserAccount(3, ""), ""))

        whenever(personRepository.findAllById(any())).thenReturn(persons)
    }

    @Test
    fun whenNewPlayer_thenMustPlay() {

        setPersonMatches(1, past(1))
        setPersonMatches(2, past(1))
        setPersonMatches(3)

        val result = cut.determinPlayingPersons(persons)

        assertk.assert(result[0].id).isEqualTo(3L)
    }

    @Test
    fun whenDidNotPlayYesterday_thenMustPlay1() {

        setPersonMatches(1, past(1))
        setPersonMatches(2, past(1))
        setPersonMatches(3, past(2), past(3))

        val result = cut.determinPlayingPersons(persons)

        assertk.assert(result[0].id).isEqualTo(3L)
    }

    @Test
    fun whenDidNotPlayYesterday_thenMustPlay2() {

        setPersonMatches(1, past(1))
        setPersonMatches(2, past(1))
        setPersonMatches(3, past(3), past(4), past(5), past(6))

        val result = cut.determinPlayingPersons(persons)

        assertk.assert(result[0].id).isEqualTo(3L)
    }

    private fun past(days: Long): LocalDateTime {
        return NOW.minusDays(days)
    }

    private fun setPersonMatches(personId: Long, vararg dates: LocalDateTime) {
        whenever(matchRepository.getPlayedMatchesForPersonInRange(eq(personId), any(), any())).thenReturn(dates.asList())
    }
}