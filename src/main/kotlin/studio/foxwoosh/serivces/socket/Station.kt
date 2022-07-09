package studio.foxwoosh.serivces.socket

enum class Station(val code: Int) {
    ULTRA(0);

    companion object {
        fun get(code: Int) = values().find { it.code == code }
    }
}