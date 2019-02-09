package at.wuzeln.manager.dto.enums

import at.wuzeln.manager.model.Match

enum class MatchState {
    CREATED, STARTED, FINISHED;

    companion object {
        fun ofMatch(match: Match): MatchState {
            return if (match.endDate != null) {
                FINISHED
            } else if (match.startDate != null) {
                STARTED
            } else {
                CREATED
            }
        }
    }
}