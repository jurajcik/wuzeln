package at.wuzeln.manager.dao

import at.wuzeln.manager.model.Player
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface PlayerRepository : JpaRepository<Player, Long> {

    fun findByTeam_Match_IdAndPerson_Id(matchId: Long, personId: Long): Player

    fun findAllByStatsIsNullAndTeam_match_endDateIsNotNull(pageable: Pageable) : Slice<Player>
}