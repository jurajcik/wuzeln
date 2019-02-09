package at.wuzeln.manager.model.enums

enum class SecurityRole {

    UNKNOWN,
    REGISTERED_USER,
    ADMIN;

    companion object {
        const val ROLE_PREFIX = "ROLE_";
    }

    fun nameWithPrefix(): String {
        return ROLE_PREFIX + this.name
    }
}