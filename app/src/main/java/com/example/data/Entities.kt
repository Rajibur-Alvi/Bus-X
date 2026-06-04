package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.security.MessageDigest

@Entity(tableName = "financial_reports")
data class FinancialReport(
    @PrimaryKey val id: String,
    val companyName: String,
    val fiscalPeriod: String,
    val currency: String,
    val timestamp: Long,
    val healthScore: Int,
    val rationale: String,
    val confidenceLevel: String, // "High", "Medium", "Low"
    val reportJson: String, // Detailed JSON representing List<RuleCheckResult> and NextActions
    val integrityChecksum: String
) {
    companion object {
        fun calculateChecksum(
            id: String,
            companyName: String,
            score: Int,
            timestamp: Long,
            reportJson: String
        ): String {
            val input = "$id:$companyName:$score:$timestamp:$reportJson"
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}

@Entity(tableName = "compliance_audit_logs")
data class AuditLog(
    @PrimaryKey val logId: String,
    val timestamp: Long,
    val actorId: String, // e.g. "Automated CA Engine" or "User"
    val auditEventType: String, // "SCAN_TRIGGERED", "REPORT_GENERATED", "AUDIT_LOG_EXPORTED", "COMPLIANCE_FAILED"
    val targetReportId: String, // Association for traceability
    val actionDetails: String,
    val integrityChecksum: String
) {
    companion object {
        fun calculateChecksum(
            logId: String,
            timestamp: Long,
            eventType: String,
            details: String
        ): String {
            val input = "$logId:$timestamp:$eventType:$details"
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
