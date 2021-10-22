package com.gv.weather.core

import android.content.Context
import com.gv.weather.R
import java.text.SimpleDateFormat
import java.util.*

val currentDateTime: Date
    get() = Calendar.getInstance().time

fun Date.dateToString(format: String = "yyyy/MM/dd HH:mm:ss",
                      locale: Locale = Locale.getDefault()): String =
    SimpleDateFormat(format, locale).format(this)

fun String.stringToDate(format: String = "yyyy/MM/dd HH:mm:ss",
                        locale: Locale = Locale.getDefault()): Date =
    SimpleDateFormat(format, locale).parse(this) ?: Date()

fun currentDateToString(format: String = "yyyy/MM/dd HH:mm:ss") =
    currentDateTime.dateToString(format)

fun String.serverDateToNormal(format: String) =
    stringToDate(format).dateToString("yyyy/MM/dd")

fun Context.formatLastUpdateDate(date: Date?, format: String = "yyyy/MM/dd HH:mm:ss"): String {
    return if (date != null) {
        val dateString = date.dateToString(format)
        if (dateString.substring(0, 4) == currentDateToString("yyyy")) {
            when {
                dateString.substring(5, 10) == currentDateToString("MM/dd") -> date.dateToString("HH:mm")
                dateString.substring(5, 7) == currentDateToString("MM") -> {
                    when (currentDateToString("dd").toInt() - dateString.substring(8, 10).toInt()) {
                        1 -> getString(R.string.yesterday_with_ph, date.dateToString("HH:mm"))
                        -1 -> getString(R.string.tomorrow_with_ph, date.dateToString("HH:mm"))
                        else -> date.dateToString("dd MMMM  HH:mm")
                    }
                }
                else -> date.dateToString("dd MMMM  HH:mm")
            }
        } else date.dateToString("dd MMMM yyyy HH:mm")
    } else ""
}

fun Context.formatDailyForecastsDate(_date: String, format: String = "yyyy/MM/dd"): String {
    val date = _date.stringToDate(format)
    return if (_date.substring(0, 4) == currentDateToString("yyyy")) {
        if (_date.substring(5, 10) == currentDateToString("MM/dd"))
            getString(R.string.today)
        else {
            when {
                _date.substring(8, 10).toInt() - currentDateToString("dd").toInt() == 1 ->
                    getString(R.string.tomorrow)
                _date.substring(8, 10).toInt() - currentDateToString("dd").toInt() == -1 ->
                    getString(R.string.yesterday)
                else -> date.dateToString("dd MMMM")
            }
        }
    } else
        date.dateToString("dd MMMM yyyy")
}