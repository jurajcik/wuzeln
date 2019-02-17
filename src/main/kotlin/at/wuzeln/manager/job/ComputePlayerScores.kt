package at.wuzeln.manager.job

import at.wuzeln.manager.dao.PlayerRepository
import at.wuzeln.manager.dao.PlayerStatsRepository
import at.wuzeln.manager.model.Player
import at.wuzeln.manager.model.stat.PlayerStats
import at.wuzeln.manager.service.CalculationService
import mu.KotlinLogging
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class ComputePlayerScores(
        private val playerRepository: PlayerRepository,
        private val calculationService: CalculationService,
        private val playerStatsRepository: PlayerStatsRepository
) {

    companion object {
        const val PAGE_SIZE: Int = 100
    }

    private val log = KotlinLogging.logger {}

    @EventListener
    fun onApplicationEvent( event: ContextRefreshedEvent) {

        log.info{"started ComputePlayerScores"}

        var playersSlice: Slice<Player>
        var count = 0

        do {
            playersSlice = playerRepository.findAllByStatsIsNullAndTeam_match_endDateIsNotNull(PageRequest.of(0, PAGE_SIZE))
            processPage(playersSlice.content)
            count += playersSlice.content.size

        } while (!playersSlice.isLast)

        log.info{"finished ComputePlayerScores. Processed $count players"}
    }


    @Transactional(Transactional.TxType.REQUIRES_NEW)
    fun processPage(players: List<Player>) {
        val stats = ArrayList<PlayerStats>()

        for (one in players) {
            stats.add(calculationService.calculatePersonalScore(one))
        }

        playerStatsRepository.saveAll(stats)
    }


}