package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AuditLog
import com.example.data.BusinessXRayRepository
import com.example.data.FinancialReport
import com.example.data.rules.CompleteAuditReport
import com.example.data.rules.RulesEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStreamReader

sealed interface UiState {
    object Idle : UiState
    object Loading : UiState
    data class Success(val report: CompleteAuditReport) : UiState
    data class Error(val message: String, val errors: List<com.example.data.rules.ComplianceError> = emptyList()) : UiState
}

class BusinessXRayViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BusinessXRayRepository

    // Form Ingesting Inputs
    val csvInput = MutableStateFlow("")
    val companyNameInput = MutableStateFlow("Globex Corp")
    val fiscalPeriodInput = MutableStateFlow("2026-Q1")
    val currencyInput = MutableStateFlow("USD")

    // Navigation and screen layout tabs state
    val selectedTab = MutableStateFlow(0) // 0: Dashboard, 1: Auditing Input, 2: API & Compliance Docs
    val selectedReportId = MutableStateFlow<String?>(null)
    val viewedReportDetails = MutableStateFlow<CompleteAuditReport?>(null)

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = BusinessXRayRepository(application, database.appDao())
        
        // Log launch
        viewModelScope.launch {
            repository.logCustomAction("System", "APP_LAUNCHED", "Business X-Ray workspace loaded successfully.")
        }
    }

    val reportsList: StateFlow<List<FinancialReport>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogList: StateFlow<List<AuditLog>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadSampleData() {
        try {
            val assetManager = getApplication<Application>().assets
            assetManager.open("sample_financials.csv").use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    csvInput.value = reader.readText()
                }
            }
            companyNameInput.value = "Globex Corp"
            fiscalPeriodInput.value = "2026-Q1"
            currencyInput.value = "USD"

            viewModelScope.launch {
                repository.logCustomAction("User", "LOAD_SAMPLE_DATA", "Loaded pre-configured sample financial ledger dataset.")
            }
        } catch (e: Exception) {
            _uiState.value = UiState.Error("Failed to load sample asset: ${e.localizedMessage}")
        }
    }

    fun triggerAnalysis() {
        if (csvInput.value.isBlank()) {
            _uiState.value = UiState.Error("Source field is empty. Please paste transactional ledger statements.")
            return
        }

        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val reportResult = repository.analyzeAndSave(
                    csvText = csvInput.value,
                    companyName = companyNameInput.value.trim(),
                    fiscalPeriod = fiscalPeriodInput.value.trim(),
                    currency = currencyInput.value.trim().uppercase()
                )

                if (reportResult.errors.isNotEmpty()) {
                    _uiState.value = UiState.Error(
                        message = "Compliance schema validation integrity check failed.",
                        errors = reportResult.errors
                    )
                } else {
                    _uiState.value = UiState.Success(reportResult)
                    viewedReportDetails.value = reportResult
                    selectedReportId.value = null // Viewing active new successfully compiled run
                    selectedTab.value = 0 // transition view to dashboard or focus the result
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Pipeline processing exceptions: ${e.localizedMessage}")
            }
        }
    }

    fun loadHistoricalReport(reportId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val report = repository.loadReportDetails(reportId)
            if (report != null) {
                viewedReportDetails.value = report
                selectedReportId.value = reportId
                _uiState.value = UiState.Success(report)
                repository.logCustomAction("Auditor", "VIEW_HISTORIC_REPORT", "Loaded historic audit records file index $reportId.")
            } else {
                _uiState.value = UiState.Error("Could not retrieve historic data indexes.")
            }
        }
    }

    fun discardReportAndTrace(reportId: String) {
        viewModelScope.launch {
            repository.clearHistoryForTrace(reportId)
            if (selectedReportId.value == reportId) {
                selectedReportId.value = null
                viewedReportDetails.value = null
                _uiState.value = UiState.Idle
            }
        }
    }

    fun dismissState() {
        _uiState.value = UiState.Idle
    }
}
