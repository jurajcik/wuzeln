package at.wuzeln.manager.model.enums

enum class Position(
        private val positionInArray4: Int
) {

    GOALKEEPER(0),
    DEFENDER(1),
    MIDFIELDER(2),
    FORWARD(3);

    companion object {
        fun toPosition(position: Int, nrPlayers: Int): Position {

            if (nrPlayers == 4) {
                return Position.values().first { it.positionInArray4 == position }
            } else if (nrPlayers == 3) {
                when (position) {
                    0 -> return Position.GOALKEEPER
                    1 -> return Position.MIDFIELDER
                    2 -> return Position.FORWARD
                }
            } else if (nrPlayers == 2) {
                when (position) {
                    0 -> return Position.GOALKEEPER
                    1 -> return Position.FORWARD
                }
            }
            throw RuntimeException("Unexpected number of players $nrPlayers or unexpected position $position")
        }
    }
}