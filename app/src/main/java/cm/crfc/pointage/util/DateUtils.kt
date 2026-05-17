package cm.crfc.pointage.util

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID

private val frenchLocale = Locale.FRANCE
private val storageFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val displayFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", frenchLocale)
private val slashFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", frenchLocale)
private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", frenchLocale)
private val fileFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
private val zone = ZoneId.of("Africa/Douala")
private val lateReference = LocalTime.of(8, 15)

fun nowIso(): String = LocalDateTime.now(zone).toString()

fun todayIso(): String = LocalDate.now(zone).format(storageFormatter)

fun subtractDays(days: Long): String = LocalDate.now(zone).minusDays(days).format(storageFormatter)

fun addDays(date: String, days: Long): String = LocalDate.parse(date).plusDays(days).format(storageFormatter)

fun genId(): String = UUID.randomUUID().toString()

fun formatDisplayDate(date: String): String =
    LocalDate.parse(date).format(displayFormatter).replaceFirstChar { it.titlecase(frenchLocale) }

fun formatSlashDate(date: String): String = LocalDate.parse(date).format(slashFormatter)

fun formatDateTime(dateTime: String): String = LocalDateTime.parse(dateTime).format(dateTimeFormatter)

fun calcMinutesLate(arrivalTime: String): Int =
    runCatching {
        val arrival = LocalTime.parse(arrivalTime)
        Duration.between(lateReference, arrival).toMinutes().toInt().coerceAtLeast(0)
    }.getOrDefault(0)

fun generateIntroText(date: String): String {
    val label = formatDisplayDate(date).lowercase(frenchLocale)
    return "Monsieur le Coordonnateur National,\n\nJ'ai l'honneur de vous rendre compte de la situation journaliere du personnel ce $label.\n\nEn effet, le pointage du personnel a permis de relever les faits ci-apres."
}

fun buildCityLine(date: String): String {
    val parsed = LocalDate.parse(date)
    val month = parsed.month.getDisplayName(TextStyle.FULL, frenchLocale)
    return "Yaounde, le ${parsed.dayOfMonth} $month ${parsed.year}"
}

fun exportFileName(prefix: String, extension: String): String =
    "$prefix-${LocalDateTime.now(zone).format(fileFormatter)}.$extension"
