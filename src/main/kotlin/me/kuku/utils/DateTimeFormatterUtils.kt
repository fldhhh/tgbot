@file:Suppress("unused")

package me.kuku.utils

import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object DateTimeFormatterUtils {
    private val FORMATTER_CACHE = ConcurrentHashMap<String, DateTimeFormatter?>()

    @JvmStatic
    fun format(localDateTime: LocalDateTime, pattern: String): String {
        val dtf = creat(pattern)
        return dtf.format(localDateTime)
    }

    @JvmStatic
    fun format(localDate: LocalDate, pattern: String): String {
        val dtf = creat(pattern)
        return dtf.format(localDate)
    }

    @JvmStatic
    fun format(localTime: LocalTime, pattern: String): String {
        val dtf = creat(pattern)
        return dtf.format(localTime)
    }

    @JvmStatic
    fun formatNow(pattern: String): String {
        val dtf = creat(pattern)
        return dtf.format(LocalDateTime.now())
    }

    @JvmStatic
    fun format(date: Date, pattern: String): String {
        val localDateTime = date.toInstant().atOffset(ZoneOffset.of("+8")).toLocalDateTime()
        return format(localDateTime, pattern)
    }

    @JvmStatic
    fun format(time: Long, pattern: String): String {
        val localDateTime = Instant.ofEpochMilli(time).atOffset(ZoneOffset.of("+8")).toLocalDateTime()
        return format(localDateTime, pattern)
    }

    @JvmStatic
    fun parseToLocalDateTime(str: String, pattern: String): LocalDateTime {
        val dtf = creat(pattern)
        return LocalDateTime.parse(str, dtf)
    }

    @JvmStatic
    fun parseToLocalDate(str: String, pattern: String): LocalDate {
        val dtf = creat(pattern)
        return LocalDate.parse(str, dtf)
    }

    @JvmStatic
    fun parseToLocalTime(str: String, pattern: String): LocalTime {
        val dtf = creat(pattern)
        return LocalTime.parse(str, dtf)
    }

    @JvmStatic
    fun parse(str: String, pattern: String): Long {
        val localDateTime = parseToLocalDateTime(str, pattern)
        return localDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli()
    }

    @JvmStatic
    fun parseToDate(str: String, pattern: String): Date {
        val localDateTime = parseToLocalDateTime(str, pattern)
        val instant = localDateTime.toInstant(ZoneOffset.of("+8"))
        return Date.from(instant)
    }

    @JvmStatic
    fun creat(pattern: String): DateTimeFormatter {
        var dtf = FORMATTER_CACHE[pattern]
        if (dtf == null) {
            dtf = DateTimeFormatter.ofPattern(pattern)
            val oldFormatter = FORMATTER_CACHE.putIfAbsent(pattern, dtf)
            if (oldFormatter != null) {
                dtf = oldFormatter
            }
        }
        return dtf!!
    }
}