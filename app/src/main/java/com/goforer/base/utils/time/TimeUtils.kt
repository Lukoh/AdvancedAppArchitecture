package com.goforer.base.utils.time

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.goforer.advancedapparchitecture.R
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

object TimeUtils {
    private const val DATE_TIME_PATTERN_TIMEZONE_FULL = "yyyy-MM-dd HH:mm:ss.SSSSSSXXX"

    @ExperimentalTime
    val now = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .toInstant(TimeZone.currentSystemDefault())
        .toEpochMilliseconds()
        .toFloat()

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat")
    @ExperimentalTime
    fun nowMicros(): String {
        val microseconds =
            TimeUnit.SECONDS.toMicros(Instant.now().truncatedTo(ChronoUnit.MICROS).epochSecond) +
                    TimeUnit.NANOSECONDS.toMicros(
                        Instant.now().truncatedTo(ChronoUnit.MICROS).nano.toLong()
                    )
        val wholeSeconds = TimeUnit.MICROSECONDS.toSeconds(microseconds)
        val nanoAdjustment =
            TimeUnit.MICROSECONDS.toNanos(microseconds) - TimeUnit.SECONDS.toNanos(wholeSeconds)

        val instant = Instant.ofEpochSecond(wholeSeconds, nanoAdjustment)

        return SimpleDateFormat(DATE_TIME_PATTERN_TIMEZONE_FULL).format(Date.from(instant))
            .replace("000", (100..999).random().toString())
    }

    @ExperimentalTime
    fun now() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()

    @RequiresApi(Build.VERSION_CODES.O)
    @ExperimentalTime
    fun calculateBygoneTimeInterval(context: Context, dateTime: Float): String {
        val timeZone = TimeZone.currentSystemDefault()
        val nowMoment = Clock.System.now().toLocalDateTime(timeZone).toInstant(timeZone)
        val newsMoment =
            kotlinx.datetime.Instant.fromEpochSeconds(dateTime.toLong(), dateTime.toLong())
                .toLocalDateTime(timeZone)
                .toInstant(timeZone)
        val durationSinceThen = nowMoment.minus(newsMoment)
        val days = durationSinceThen.toInt(DurationUnit.DAYS)
        val hours = durationSinceThen.toInt(DurationUnit.HOURS)
        val minutes = durationSinceThen.toInt(DurationUnit.MINUTES)

        return if (days > 0) {
            when (days) {
                in 1.rangeTo(30) ->
                    "${days}${context.getString(R.string.day)}${" "}${
                        context.getString(
                            R.string.bygone_time
                        )
                    }"

                in 31.rangeTo(365) -> {
                    val month = days.div(30)

                    "${month}${context.getString(R.string.month)}${" "}${
                        context.getString(
                            R.string.bygone_time
                        )
                    }"
                }

                else -> {
                    val year = days.div(356)

                    "${year}${context.getString(R.string.day)}${" "}${
                        context.getString(
                            R.string.bygone_time
                        )
                    }"
                }
            }
        } else {
            when {
                (hours > 0) -> {
                    "${hours}${context.getString(R.string.hour)}${" "}${
                        context.getString(
                            R.string.bygone_time
                        )
                    }"
                }

                (minutes > 0) -> {
                    "${minutes}${context.getString(R.string.minute)}${" "}${context.getString(R.string.bygone_time)}"
                }

                else -> {
                    "${"몇 "}${context.getString(R.string.second)}${" "}${context.getString(R.string.bygone_time)}"
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @ExperimentalTime
    fun calculateBygoneTimeIntervalForComment(context: Context, dateTime: Float): String {
        val timeZone = TimeZone.currentSystemDefault()
        val nowMoment = Clock.System.now().toLocalDateTime(timeZone).toInstant(timeZone)
        val newsMoment =
            kotlinx.datetime.Instant.fromEpochSeconds(dateTime.toLong(), dateTime.toLong())
                .toLocalDateTime(timeZone)
                .toInstant(timeZone)
        val durationSinceThen = nowMoment.minus(newsMoment)
        val days = durationSinceThen.toInt(DurationUnit.DAYS)
        val hours = durationSinceThen.toInt(DurationUnit.HOURS)
        val minutes = durationSinceThen.toInt(DurationUnit.MINUTES)
        val seconds = durationSinceThen.toInt(DurationUnit.SECONDS)

        return if (days > 0) {
            when (days) {
                in 1.rangeTo(30) ->
                    "${days}${context.getString(R.string.day)}${" "}${
                        context.getString(
                            R.string.bygone_time
                        )
                    }"

                in 31.rangeTo(365) -> {
                    val month = days.div(30)

                    "${month}${context.getString(R.string.month)}${" "}${
                        context.getString(
                            R.string.bygone_time
                        )
                    }"
                }

                else -> {
                    val year = days.div(356)

                    "${year}${context.getString(R.string.day)}${" "}${
                        context.getString(
                            R.string.bygone_time
                        )
                    }"
                }
            }
        } else {
            when {
                (hours > 0) -> {
                    "${hours}${context.getString(R.string.hour)}${" "}${
                        context.getString(
                            R.string.bygone_time
                        )
                    }"
                }

                (minutes > 0) -> {
                    "${minutes}${context.getString(R.string.minute)}${" "}${context.getString(R.string.bygone_time)}"
                }

                else -> {
                    "${"몇 "}${context.getString(R.string.second)}${" "}${context.getString(R.string.bygone_time)}"
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @ExperimentalTime
    fun calculateBygoneTimeDuration(dateTime: kotlinx.datetime.Instant): Double {
        val now = Clock.System.now()
        val durationSinceThen: Duration = now.minus(dateTime)

        return durationSinceThen.inWholeSeconds.toDouble()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @ExperimentalTime
    fun isOverOneDay(dateTime: Float): Boolean {
        val timeZone = TimeZone.currentSystemDefault()
        val nowMoment = Clock.System.now().toLocalDateTime(timeZone).toInstant(timeZone)
        val moment =
            kotlinx.datetime.Instant.fromEpochSeconds(dateTime.toLong(), dateTime.toLong())
                .toLocalDateTime(timeZone)
                .toInstant(timeZone)
        val durationSinceThen = nowMoment.minus(moment)
        val days = durationSinceThen.toInt(DurationUnit.DAYS)

        return when {
            days > 0 -> true
            else -> false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @ExperimentalTime
    fun isOver7Days(dateTime: Float): Boolean {
        val timeZone = TimeZone.currentSystemDefault()
        val nowMoment = Clock.System.now().toLocalDateTime(timeZone).toInstant(timeZone)
        val moment =
            kotlinx.datetime.Instant.fromEpochSeconds(dateTime.toLong(), dateTime.toLong())
                .toLocalDateTime(timeZone)
                .toInstant(timeZone)
        val durationSinceThen = nowMoment.minus(moment)
        val days = durationSinceThen.toInt(DurationUnit.DAYS)

        return when {
            days > 7 -> true
            else -> false
        }
    }

    fun getTimeZone(): String = TimeZone.currentSystemDefault().id
}