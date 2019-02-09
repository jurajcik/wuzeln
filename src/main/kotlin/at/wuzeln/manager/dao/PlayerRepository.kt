package at.wuzeln.manager.dao

import at.wuzeln.manager.model.Player
import org.springframework.data.jpa.repository.JpaRepository

interface PlayerRepository : JpaRepository<Player, Long> {

    fun findByTeam_Match_IdAndPerson_Id(matchId: Long, personId: Long): Player
}