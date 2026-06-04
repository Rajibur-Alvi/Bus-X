package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM financial_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<FinancialReport>>

    @Query("SELECT * FROM financial_reports WHERE id = :reportId LIMIT 1")
    suspend fun getReportById(reportId: String): FinancialReport?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: FinancialReport)

    @Query("DELETE FROM financial_reports WHERE id = :reportId")
    suspend fun deleteReportById(reportId: String)

    @Query("SELECT * FROM compliance_audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)
}
