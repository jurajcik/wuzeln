package at.wuzeln.manager.dao

import at.wuzeln.manager.model.Person
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface PersonRepository: JpaRepository<Person, Long> {

    @Query("SELECT SUM(p.milisecondsInGoal) FROM  Player p WHERE p.person.id = :personId " +
            "AND " + RepoConstants.MATCH_FINISHED_AND_RANGE_P)
    fun sumMilisecondsInGoal(personId: Long, startDate: LocalDateTime, endDate: LocalDateTime) : Int
}