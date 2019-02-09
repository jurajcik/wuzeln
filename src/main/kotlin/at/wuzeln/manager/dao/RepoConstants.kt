package at.wuzeln.manager.dao

class RepoConstants {

    companion object {
        const val RANGE_P = " p.team.match.startDate <= :endDate AND p.team.match.startDate >= :startDate "
        const val MATCH_FINISHED_P = " p.team.match.endDate IS NOT NULL "
        const val MATCH_FINISHED_AND_RANGE_P = MATCH_FINISHED_P + "AND" + RANGE_P

        const val RANGE_M = " m.startDate <= :endDate AND m.startDate >= :startDate "
        const val MATCH_FINISHED_M = " m.endDate IS NOT NULL "
        const val MATCH_FINISHED_AND_RANGE_M = MATCH_FINISHED_M + "AND" + RANGE_M
    }
}