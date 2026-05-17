package cm.crfc.pointage.data

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import cm.crfc.pointage.R
import cm.crfc.pointage.data.local.ExportFileDao
import cm.crfc.pointage.model.AbsenceReason
import cm.crfc.pointage.model.DailyReport
import cm.crfc.pointage.model.Employee
import cm.crfc.pointage.model.ExportFileRecord
import cm.crfc.pointage.model.ExportFileType
import cm.crfc.pointage.model.ExportPayload
import cm.crfc.pointage.model.OperationResult
import cm.crfc.pointage.model.User
import cm.crfc.pointage.util.buildCityLine
import cm.crfc.pointage.util.exportFileName
import cm.crfc.pointage.util.formatDisplayDate
import cm.crfc.pointage.util.formatSlashDate
import cm.crfc.pointage.util.genId
import cm.crfc.pointage.util.nowIso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

class ExportService(
    private val context: Context,
    private val exportFileDao: ExportFileDao
) {
    fun observeExportFiles(): Flow<List<ExportFileRecord>> =
        exportFileDao.observeAll().map { items -> items.map { it.toDomain() } }

    fun exportPdf(
        report: DailyReport,
        employees: List<Employee>,
        absenceReasons: List<AbsenceReason>,
        author: User
    ): OperationResult {
        return runCatching {
            val file = File(pdfDir(), exportFileName("rapport-pointage-${report.date}", "pdf"))
            buildPdf(file, report, employees, absenceReasons, author)
            val record = ExportFileRecord(
                id = genId(),
                type = ExportFileType.PDF,
                fileName = file.name,
                filePath = file.absolutePath,
                sizeBytes = file.length(),
                createdAt = nowIso(),
                reportDate = report.date
            )
            runBlocking(Dispatchers.IO) { exportFileDao.insert(record.toEntity()) }
            shareFile(file, "application/pdf", "Partager le rapport PDF")
            OperationResult(true)
        }.getOrElse { OperationResult(false, it.message ?: "Erreur lors de l'export PDF.") }
    }

    fun exportExcel(payload: ExportPayload): OperationResult {
        return runCatching {
            val file = File(excelDir(), exportFileName("synthese-rapports", "xlsx"))
            buildExcel(file, payload)
            val record = ExportFileRecord(
                id = genId(),
                type = ExportFileType.EXCEL,
                fileName = file.name,
                filePath = file.absolutePath,
                sizeBytes = file.length(),
                createdAt = nowIso(),
                periodStart = payload.periodStart,
                periodEnd = payload.periodEnd
            )
            runBlocking(Dispatchers.IO) { exportFileDao.insert(record.toEntity()) }
            shareFile(
                file,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "Partager la synthese Excel"
            )
            OperationResult(true)
        }.getOrElse { OperationResult(false, it.message ?: "Erreur lors de l'export Excel.") }
    }

    suspend fun deleteExport(record: ExportFileRecord): OperationResult {
        return runCatching {
            File(record.filePath).takeIf { it.exists() }?.delete()
            runBlocking(Dispatchers.IO) { exportFileDao.deleteById(record.id) }
            OperationResult(true)
        }.getOrElse { OperationResult(false, it.message ?: "Suppression impossible.") }
    }

    fun shareExport(record: ExportFileRecord): OperationResult =
        runCatching {
            val file = File(record.filePath)
            if (!file.exists()) {
                return OperationResult(false, "Le fichier n'existe plus sur l'appareil.")
            }
            shareFile(file, mimeType(record.type), "Partager ${record.fileName}")
            OperationResult(true)
        }.getOrElse { OperationResult(false, it.message ?: "Partage impossible.") }

    private fun mimeType(type: ExportFileType): String =
        when (type) {
            ExportFileType.PDF -> "application/pdf"
            ExportFileType.EXCEL -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        }

    private fun pdfDir(): File = File(context.filesDir, "exports/pdf").apply { mkdirs() }

    private fun excelDir(): File = File(context.filesDir, "exports/excel").apply { mkdirs() }

    private fun shareFile(file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun buildPdf(
        file: File,
        report: DailyReport,
        employees: List<Employee>,
        reasons: List<AbsenceReason>,
        author: User
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        BitmapFactory.decodeResource(context.resources, R.drawable.crfc_template_background)?.let { bitmap ->
            canvas.drawBitmap(bitmap, null, android.graphics.Rect(0, 0, 595, 842), null)
        }

        val titlePaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 11f
            isAntiAlias = true
        }
        val smallPaint = Paint(textPaint).apply { textSize = 10f }
        val linePaint = Paint().apply {
            color = android.graphics.Color.BLACK
            strokeWidth = 1f
        }

        fun employeeName(id: String) = employees.firstOrNull { it.id == id }?.fullName ?: "Inconnu"
        fun reasonLabel(id: String) = reasons.firstOrNull { it.id == id }?.label ?: "Inconnu"

        var y = 92f
        canvas.drawText(buildCityLine(report.date), 370f, y, textPaint)
        y += 30f
        canvas.drawText("A l'attention de Monsieur le Coordonnateur National", 255f, y, textPaint)
        y += 16f
        canvas.drawText("du Centre de Reseaux des Filieres de Croissance (CRFC) au Cameroun", 180f, y, textPaint)
        y += 28f
        canvas.drawText("Objet : Compte rendu de la journee", 70f, y, titlePaint)
        y += 26f
        canvas.drawText("Monsieur le Coordonnateur National,", 70f, y, titlePaint)
        y += 26f
        canvas.drawText("Dans le cadre des missions qui sont a ma charge, je viens par la presente vous faire le point sur les presences du jour.", 70f, y, textPaint)
        y += 18f
        canvas.drawText("Vous trouverez ci-apres la liste des retards, des absences et des visiteurs de la journee du ${formatDisplayDate(report.date).lowercase()}.", 70f, y, textPaint)
        y += 26f

        fun drawTable(title: String, headers: List<String>, rows: List<List<String>>) {
            canvas.drawText(title, 250f, y, titlePaint)
            y += 14f
            val startX = 70f
            val widths = listOf(40f, 320f, 120f)
            var x = startX
            headers.forEachIndexed { index, header ->
                canvas.drawRect(x, y, x + widths[index], y + 20f, linePaint)
                canvas.drawText(header, x + 4f, y + 14f, smallPaint)
                x += widths[index]
            }
            y += 20f
            val safeRows = if (rows.isEmpty()) listOf(listOf("", "", "")) else rows
            safeRows.forEach { row ->
                x = startX
                row.forEachIndexed { index, value ->
                    canvas.drawRect(x, y, x + widths[index], y + 20f, linePaint)
                    canvas.drawText(value.take(42), x + 4f, y + 14f, smallPaint)
                    x += widths[index]
                }
                y += 20f
            }
            y += 18f
        }

        drawTable(
            "Retards",
            listOf("N", "NOMS ET PRENOMS", "HEURE D'ARRIVEE"),
            report.lateEntries.mapIndexed { index, entry ->
                listOf((index + 1).toString(), employeeName(entry.employeeId), entry.arrivalTime.replace(":", "h"))
            }
        )
        drawTable(
            "Absents",
            listOf("N", "NOMS ET PRENOMS", "MOTIF"),
            report.absenceEntries.mapIndexed { index, entry ->
                listOf((index + 1).toString(), employeeName(entry.employeeId), reasonLabel(entry.reasonId))
            }
        )

        canvas.drawText("Visiteurs enregistres : ${report.visitorCount}", 70f, y, titlePaint)
        y += 24f
        canvas.drawText("Dans l'attente de vos instructions, je vous prie d'agreer Monsieur le Coordonnateur National, l'expression de mon profond respect.", 70f, y, textPaint)
        y += 56f
        canvas.drawText(author.fullName, 390f, y, titlePaint)
        y += 16f
        canvas.drawText(author.jobTitle.ifBlank { "Agent" }, 390f, y, smallPaint)

        document.finishPage(page)
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
    }

    private fun buildExcel(file: File, payload: ExportPayload) {
        val workbook = XSSFWorkbook()
        val employees = payload.employees.associateBy { it.id }
        val reasons = payload.absenceReasons.associateBy { it.id }
        val sorted = payload.reports.sortedBy { it.date }

        workbook.createSheet("Synthese").apply {
            createRow(0).createCell(0).setCellValue("CRFC - Synthese des rapports de pointage")
            createRow(1).createCell(0).setCellValue("Periode : ${formatSlashDate(payload.periodStart)} - ${formatSlashDate(payload.periodEnd)}")
            createRow(2).createCell(0).setCellValue("Genere par : ${payload.author.fullName} (${payload.author.jobTitle})")
            createRow(4).apply {
                createCell(0).setCellValue("Date")
                createCell(1).setCellValue("Retards")
                createCell(2).setCellValue("Absences")
                createCell(3).setCellValue("Visiteurs")
                createCell(4).setCellValue("Minutes retard")
                createCell(5).setCellValue("Statut")
            }
            sorted.forEachIndexed { index, report ->
                createRow(index + 5).apply {
                    createCell(0).setCellValue(formatSlashDate(report.date))
                    createCell(1).setCellValue(report.lateEntries.size.toDouble())
                    createCell(2).setCellValue(report.absenceEntries.size.toDouble())
                    createCell(3).setCellValue(report.visitorCount.toDouble())
                    createCell(4).setCellValue(report.lateEntries.sumOf { it.minutesLate }.toDouble())
                    createCell(5).setCellValue(if (report.status.name == "FINALIZED") "Finalise" else "Brouillon")
                }
            }
        }

        workbook.createSheet("Retards").apply {
            createRow(0).apply {
                createCell(0).setCellValue("Date")
                createCell(1).setCellValue("Noms et prenoms")
                createCell(2).setCellValue("Heure d'arrivee")
                createCell(3).setCellValue("Minutes de retard")
            }
            sorted.flatMap { report -> report.lateEntries.map { report to it } }.forEachIndexed { index, (report, entry) ->
                createRow(index + 1).apply {
                    createCell(0).setCellValue(formatSlashDate(report.date))
                    createCell(1).setCellValue(employees[entry.employeeId]?.fullName ?: "Inconnu")
                    createCell(2).setCellValue(entry.arrivalTime)
                    createCell(3).setCellValue(entry.minutesLate.toDouble())
                }
            }
        }

        workbook.createSheet("Absences").apply {
            createRow(0).apply {
                createCell(0).setCellValue("Date")
                createCell(1).setCellValue("Noms et prenoms")
                createCell(2).setCellValue("Motif")
                createCell(3).setCellValue("Observations")
            }
            sorted.flatMap { report -> report.absenceEntries.map { report to it } }.forEachIndexed { index, (report, entry) ->
                createRow(index + 1).apply {
                    createCell(0).setCellValue(formatSlashDate(report.date))
                    createCell(1).setCellValue(employees[entry.employeeId]?.fullName ?: "Inconnu")
                    createCell(2).setCellValue(reasons[entry.reasonId]?.label ?: "Inconnu")
                    createCell(3).setCellValue(entry.comment ?: "")
                }
            }
        }

        FileOutputStream(file).use(workbook::write)
        workbook.close()
    }
}
