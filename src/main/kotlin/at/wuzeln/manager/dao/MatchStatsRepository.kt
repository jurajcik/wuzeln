package at.wuzeln.manager.dao

import at.wuzeln.manager.model.stat.MatchStats
import org.springframework.data.jpa.repository.JpaRepository

interface MatchStatsRepository : JpaRepository<MatchStats, Long> {

}
