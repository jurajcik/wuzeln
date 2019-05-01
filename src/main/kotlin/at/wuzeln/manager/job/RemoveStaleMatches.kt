package at.wuzeln.manager.job

import at.wuzeln.manager.dao.MatchRepository
import at.wuzeln.manager.dao.RegistrationRepository
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import javax.transaction.Transactional

@Component
class RemoveStaleMatches(
        private val matchRepository: MatchRepository,
        private val registrationRepository: RegistrationRepository
) {

    private val log = KotlinLogging.logger {}

    @Transactional
    @Scheduled(cron = "0 0 3 * * *")
    fun removeStaleMatches() {

        val date = LocalDateTime.now()//.minusDays(1)
        removeStaleMatches(date)
    }

    @Transactional
    fun removeStaleMatches(staleDate: LocalDateTime) {

        log.info("removeStaleMatches started")

        val matches = matchRepository.findByMetadata_CreatedDateBeforeAndEndDateIsNull(staleDate)

        log.info("removeStaleMatches, ${matches.size} to remove: $matches")

        for (match in matches) {
            registrationRepository.deleteByMatch(match)
        }

        matchRepository.deleteAll(matches)

        log.info("removeStaleMatches finished")
    }

}