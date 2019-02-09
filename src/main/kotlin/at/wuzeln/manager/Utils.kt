package at.wuzeln.manager

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class Utils {
    companion object {
        fun localDateTimeToLong(date: LocalDateTime): Long {
            return date.atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli()
        }

        fun longToLocalDateTime(milliseconds: Long): LocalDateTime {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), ZoneId.systemDefault());
        }
    }
}