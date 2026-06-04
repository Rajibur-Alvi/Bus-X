package com.example.data

import android.content.Context
import com.example.data.rules.CompleteAuditReport
import com.example.data.rules.RulesEngine
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class BusinessXRayRepository(
    private val context: Context,
    private val appDao: AppDao
) {
    val allReports: Flow<List<FinancialReport>> = appDao.getAllReports()
    val allAuditLogs: Flow<List<AuditLog>> = appDao.getAllAuditLogs()

    suspend fun loadReportDetails(reportId: String): CompleteAuditReport? {
        val entity = appDao.getReportById(reportId) ?: return null
        return try {
            CompleteAuditReport.fromJsonString(entity.reportJson)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun analyzeAndSave(
        csvText: String,
        companyName: String,
        fiscalPeriod: String,
        currency: String
    ): CompleteAuditReport {
        // Run rules compiler
        val report = RulesEngine.evaluateCsv(context, csvText, companyName, fiscalPeriod, currency)
        
        val reportId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        if (report.errors.isNotEmpty()) {
            // Document a logging failure event
            val errorDetails = "Validation failure: ${report.errors.first().errorMessage}"
            val logId = UUID.randomUUID().toString()
            val checksum = AuditLog.calculateChecksum(logId, timestamp, "COMPLIANCE_FAILED", errorDetails)

            val failureLog = AuditLog(
                logId = logId,
                timestamp = timestamp,
                actorId = "Automated Compliance Gate",
                auditEventType = "COMPLIANCE_FAILED",
                targetReportId = "",
                actionDetails = errorDetails,
                integrityChecksum = checksum
            )
            appDao.insertAuditLog(failureLog)
            return report
        }

        // Format CompleteAuditReport back to json
        val reportJson = report.toJsonString()
        val reportChecksum = FinancialReport.calculateChecksum(
            id = reportId,
            companyName = companyName,
            score = report.healthScore,
            timestamp = timestamp,
            reportJson = reportJson
        )

        // Create Report DB Entity
        val reportEntity = FinancialReport(
            id = reportId,
            companyName = companyName,
            fiscalPeriod = fiscalPeriod,
            currency = currency,
            timestamp = timestamp,
            healthScore = report.healthScore,
            rationale = report.rationale,
            confidenceLevel = if (report.healthScore >= 80) "High" else "Medium",
            reportJson = reportJson,
            integrityChecksum = reportChecksum
        )
        appDao.insertReport(reportEntity)

        // Write Audit Logs tracking the operations
        val logIdScan = UUID.randomUUID().toString()
        val scanDetails = "Successfully ingested financial ledger data. Counted ${report.transactionCount} entries spanning ${report.periodStart} to ${report.periodEnd}."
        val scanChecksum = AuditLog.calculateChecksum(logIdScan, timestamp, "SCAN_TRIGGERED", scanDetails)

        val scanLog = AuditLog(
            logId = logIdScan,
            timestamp = timestamp,
            actorId = "Automated CA Engine",
            auditEventType = "SCAN_TRIGGERED",
            targetReportId = reportId,
            actionDetails = scanDetails,
            integrityChecksum = scanChecksum
        )
        appDao.insertAuditLog(scanLog)

        val logIdReport = UUID.randomUUID().toString()
        val reportDetails = "Completed full financial review. Score: ${report.healthScore}/100. Verification SHA-256 Digest is compliant."
        val reportChecksumLog = AuditLog.calculateChecksum(logIdReport, timestamp + 1, "REPORT_GENERATED", reportDetails)

        val reportLog = AuditLog(
            logId = logIdReport,
            timestamp = timestamp + 1,
            actorId = "Automated CA Engine",
            auditEventType = "REPORT_GENERATED",
            targetReportId = reportId,
            actionDetails = reportDetails,
            integrityChecksum = reportChecksumLog
        )
        appDao.insertAuditLog(reportLog)

        return report
    }

    suspend fun clearHistoryForTrace(reportId: String) {
        appDao.deleteReportById(reportId)
        
        val timestamp = System.currentTimeMillis()
        val logId = UUID.randomUUID().toString()
        val details = "Purged financial record reports index ID: $reportId."
        val checksum = AuditLog.calculateChecksum(logId, timestamp, "RECORD_REMOVED", details)

        val deleteLog = AuditLog(
            logId = logId,
            timestamp = timestamp,
            actorId = "Compliance Admin",
            auditEventType = "RECORD_REMOVED",
            targetReportId = reportId,
            actionDetails = details,
            integrityChecksum = checksum
        )
        appDao.insertAuditLog(deleteLog)
    }

    suspend fun logCustomAction(actor: String, eventType: String, details: String) {
        val timestamp = System.currentTimeMillis()
        val logId = UUID.randomUUID().toString()
        val checksum = AuditLog.calculateChecksum(logId, timestamp, eventType, details)

        val systemLog = AuditLog(
            logId = logId,
            timestamp = timestamp,
            actorId = actor,
            auditEventType = eventType,
            targetReportId = "",
            actionDetails = details,
            integrityChecksum = checksum
        )
        appDao.insertAuditLog(systemLog)
    }
}
