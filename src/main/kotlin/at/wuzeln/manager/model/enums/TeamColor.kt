package at.wuzeln.manager.model.enums

enum class TeamColor {
    BLUE, RED;

    fun otherColor(): TeamColor {
        return if (this == TeamColor.BLUE) TeamColor.RED else TeamColor.BLUE
    }
}