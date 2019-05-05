package at.wuzeln.manager.job

import at.wuzeln.manager.dao.MatchRepository
import at.wuzeln.manager.dao.PlayerRepository
import at.wuzeln.manager.model.Match
import at.wuzeln.manager.model.Player
import at.wuzeln.manager.service.CalculationService
import at.wuzeln.manager.service.StatsService
import mu.KotlinLogging
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class ComputeStats(
        private val playerRepository: PlayerRepository,
        private val matchRepository: MatchRepository,
        private val calculationService: CalculationService,
        private val statsService: StatsService
) {

    companion object {
        const val PAGE_SIZE: Int = 100
    }

    private val log = KotlinLogging.logger {}

    @EventListener
    @Transactional
    fun onApplicationEvent(event: ContextRefreshedEvent) {

        computePlayerScores()
        computeMatchStats()
    }

    protected fun computePlayerScores() {

        log.info { "started ComputeStats.computePlayerScores" }

        var slice: Slice<Player>
        var count = 0

        do {
            slice = playerRepository.findAllByStatsIsNullAndTeam_match_endDateIsNotNull(PageRequest.of(0, PAGE_SIZE))
            calculationService.calculatePersonalScore(slice.content)
            count += slice.content.size

        } while (!slice.isLast)

        log.info { "finished ComputeStats.computePlayerScores. Processed $count players" }
    }

    protected fun computeMatchStats() {

        log.info { "started ComputeStats.computeMatchStats" }

        var slice: Slice<Match>
        var count = 0

        do {
            slice = matchRepository.findAllByStatsIsNullAndEndDateIsNotNull(PageRequest.of(0, PAGE_SIZE))
            slice.content.forEach { statsService.createMatchStats(it.id) }
            count += slice.content.size

        } while (!slice.isLast)

        log.info { "finished ComputeStats.computeMatchStats. Processed $count players" }
    }
}
