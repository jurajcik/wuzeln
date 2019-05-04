package at.wuzeln.manager

import at.wuzeln.manager.dao.*
import at.wuzeln.manager.model.Person
import at.wuzeln.manager.model.UserAccount
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class TestUtil {

    companion object{
        fun createAllPeople(): List<Person> {
            val players = ArrayList<Person>()
            for (i in 1L..10L) {
                players.add(Person(i, UserAccount(i, "person $i"), "person $i"))
            }
            return players
        }

        fun mockSecurityContext() {
            val auth = UsernamePasswordAuthenticationToken("someUsername", null)
            SecurityContextHolder.getContext().authentication = auth
        }

    }

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


    fun savePeopleWithAccounts(people: List<Person>): List<Person> {
        people.forEach { it.userAccount = userAccountRepository.save(it.userAccount!!) }
        return personRepository.saveAll(people)
    }

}
