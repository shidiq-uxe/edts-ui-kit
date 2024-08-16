package id.co.edtslib.uikit.utils

val Int.milliseconds: Long
    get() = this.toLong()

val Int.seconds: Long
    get() = this * 1000L

val Int.minutes: Long
    get() = this * 60.seconds

val Int.hours: Long
    get() = this * 60.minutes

val Int.days: Long
    get() = this * 24.hours

val Int.weeks: Long
    get() = this * 7.days

val Int.months: Long
    get() = this * 30.days // Approximate month

val Int.years: Long
    get() = this * 365.days // Non-leap year

// Convert from milliseconds to other units
val Long.inSeconds: Long
    get() = this / 1000L

val Long.inMinutes: Long
    get() = this / 1000L / 60L

val Long.inHours: Long
    get() = this / 1000L / 60L / 60L

val Long.inDays: Long
    get() = this / 1000L / 60L / 60L / 24L

val Long.inWeeks: Long
    get() = this / 1000L / 60L / 60L / 24L / 7L

val Long.inMonths: Long
    get() = this / 1000L / 60L / 60L / 24L / 30L

val Long.inYears: Long
    get() = this / 1000L / 60L / 60L / 24L / 365L

