package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuditLog
import com.example.data.FinancialReport
import com.example.data.rules.CompleteAuditReport
import com.example.data.rules.RuleCheckResult
import com.example.data.rules.RulesEngine
import com.example.viewmodel.BusinessXRayViewModel
import com.example.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// High-contrast, WCAG 2.2 AA compliant cohesive theme colors
val SlatePrimary = Color(0xFF1B1C17)      // Dark charcoal/olive-black
val SlateAccent = Color(0xFF55624C)       // Brand Sage Accent
val EmeraldSuccess = Color(0xFF386B20)    // Compliance Forest Green
val AmberWarning = Color(0xFFB45309)      // Compliant Deep Amber
val CrimsonDanger = Color(0xFFBA1A1A)     // Minimal Burgundy/Rose Red
val SlateLightBg = Color(0xFFFAF9F2)      // Warm minimalist parchment background
val IndigoBrand = Color(0xFF55624C)       // Brand sage accent for primary controls

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessXRayMainScreen(viewModel: BusinessXRayViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val reportsList by viewModel.reportsList.collectAsState()
    val auditLogs by viewModel.auditLogList.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val viewedReportDetails by viewModel.viewedReportDetails.collectAsState()
    val selectedReportId by viewModel.selectedReportId.collectAsState()

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(SlateAccent, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "◈",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Business X-Ray",
                            fontWeight = FontWeight.SemiBold,
                            color = SlatePrimary,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SlateLightBg
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.loadSampleData(); viewModel.selectedTab.value = 1 },
                        modifier = Modifier.testTag("action_load_sample")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Quick load sample finances dataset",
                            tint = SlatePrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFE2E3D8), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "JD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlatePrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF0F1E4),
                tonalElevation = 0.dp,
                modifier = Modifier
                    .border(width = 1.dp, color = Color(0xFFE1E3D3))
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectedTab.value = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard Screen link") },
                    label = { Text("Dashboard", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldSuccess,
                        selectedTextColor = EmeraldSuccess,
                        indicatorColor = Color(0xFFDDE5D1),
                        unselectedIconColor = Color(0xFF44483D),
                        unselectedTextColor = Color(0xFF44483D)
                    ),
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectedTab.value = 1 },
                    icon = { Icon(Icons.Default.Send, contentDescription = "Manual Auditing Ingestion Input link") },
                    label = { Text("Audit Ingest", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldSuccess,
                        selectedTextColor = EmeraldSuccess,
                        indicatorColor = Color(0xFFDDE5D1),
                        unselectedIconColor = Color(0xFF44483D),
                        unselectedTextColor = Color(0xFF44483D)
                    ),
                    modifier = Modifier.testTag("nav_tab_ingest")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.selectedTab.value = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Developer API and Integration compliance docs link") },
                    label = { Text("Developer", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldSuccess,
                        selectedTextColor = EmeraldSuccess,
                        indicatorColor = Color(0xFFDDE5D1),
                        unselectedIconColor = Color(0xFF44483D),
                        unselectedTextColor = Color(0xFF44483D)
                    ),
                    modifier = Modifier.testTag("nav_tab_dev")
                )
            }
        },
        containerColor = SlateLightBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    reports = reportsList,
                    viewedDetails = viewedReportDetails,
                    selectedReportId = selectedReportId
                )
                1 -> FileIngestScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
                2 -> DeveloperComplianceScreen(
                    logs = auditLogs
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(
    viewModel: BusinessXRayViewModel,
    uiState: UiState,
    reports: List<FinancialReport>,
    viewedDetails: CompleteAuditReport?,
    selectedReportId: String?
) {
    var selectedCategory by remember { mutableStateOf("All") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (viewedDetails == null) {
            item {
                WelcomeEmptyStateCard(
                    onLoadSample = {
                        viewModel.loadSampleData()
                        viewModel.selectedTab.value = 1
                    }
                )
            }
        } else {
            // Business Health Header block
            item {
                HealthScoreCard(
                    report = viewedDetails,
                    onDiscard = if (selectedReportId != null) {
                        { viewModel.discardReportAndTrace(selectedReportId) }
                    } else null
                )
            }

            // Fun Interactive Zone
            item {
                AuditFunZoneCard(report = viewedDetails)
            }

            // Checklist Categories Filter Selector
            item {
                Text(
                    text = "Financial Audit Insights",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlatePrimary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf("All", "Revenue", "Expenses", "Cash", "Risks", "Opportunities")
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFDDE5D1),
                                selectedLabelColor = Color(0xFF181D12),
                                containerColor = Color(0xFFF0F1E4),
                                labelColor = Color(0xFF44483D)
                            )
                        )
                    }
                }
            }

            // Insights list matching output rules: One screen ≈ one insight card
            val filteredRules = viewedDetails.results.filter {
                selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true)
            }

            if (filteredRules.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No active triggers found in the '$selectedCategory' framework.",
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredRules) { result ->
                    InsightCard(result = result)
                }
            }

            // Next 30 Days Action list
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("next_30_days_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F1E4)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color.White, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Next 30 Days Action Plan",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B1C17),
                                letterSpacing = (-0.3).sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        viewedDetails.nextActions.forEachIndexed { idx, action ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                // Dynamic white circle badge matching design HTML
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color.White, RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${idx + 1}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1B1C17)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = action.actionStep,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1B1C17)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Triggered by: ${action.justification}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF44483D)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Expected impact: ${action.estimated30daySafetyImpact}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = EmeraldSuccess
                                    )
                                    if (idx < viewedDetails.nextActions.size - 1) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        HorizontalDivider(color = Color(0xFFE1E3D3), thickness = 1.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Historic Scan Runs index
        if (reports.isNotEmpty()) {
            item {
                Text(
                    text = "Historic Auditing Logs Index",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlatePrimary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }

            items(reports) { r ->
                HistoricReportItem(
                    report = r,
                    isActive = r.id == selectedReportId,
                    onSelect = { viewModel.loadHistoricalReport(r.id) }
                )
            }
        }
    }
}

@Composable
fun HealthScoreCard(
    report: CompleteAuditReport,
    onDiscard: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("health_score_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1E4D5)),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "BUSINESS HEALTH SCORE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF44483D),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${report.healthScore}",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Light,
                            color = SlatePrimary,
                            letterSpacing = (-1.5).sp
                        )
                        Text(
                            text = "/100",
                            fontSize = 16.sp,
                            color = SlatePrimary.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onDiscard != null) {
                        IconButton(onClick = onDiscard) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Discard and purge report tracing logs",
                                tint = CrimsonDanger
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val statusText = when {
                report.healthScore >= 85 -> "Excellent Health"
                report.healthScore >= 70 -> "Stable Cushion"
                report.healthScore >= 50 -> "Vulnerable Exposure"
                else -> "Distressed Assets"
            }
            val badgeColor = when {
                report.healthScore >= 80 -> EmeraldSuccess
                report.healthScore >= 60 -> AmberWarning
                else -> CrimsonDanger
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .background(Color(0xFFDDE5D1), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(badgeColor, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF181D12)
                    )
                }

                Text(
                    text = "High Confidence Verification",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF44483D).copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFE1E3D3), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Company Legal Entity: ${report.companyName}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = SlatePrimary
            )
            Text(
                text = "Period: ${report.fiscalPeriod} • Currency: ${report.currency}",
                fontSize = 12.sp,
                color = Color(0xFF44483D)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "CA Audit Rationale Summary:",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = SlatePrimary
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = report.rationale,
                fontSize = 13.sp,
                color = Color(0xFF44483D),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFE1E3D3), thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "AUDIT COVERAGE BREAKDOWN",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF44483D),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Metric 1: Checkpoints Checked
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text("Checkpoints", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${report.results.size}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SlatePrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { 1.0f },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = EmeraldSuccess,
                        trackColor = Color(0xFFE1E3D3)
                    )
                }

                // Metric 2: Compliance Coverage %
                val compliantCount = report.results.count { !it.triggered }
                val compliancePct = if (report.results.isEmpty()) 1.0f else (compliantCount.toFloat() / report.results.size.toFloat())
                Column(
                    modifier = Modifier
                        .weight(1.5f)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text("Compliant Ratio", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${(compliancePct * 100).toInt()}% Clear", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SlatePrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { compliancePct },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = if (compliancePct >= 0.8f) EmeraldSuccess else if (compliancePct >= 0.5f) AmberWarning else CrimsonDanger,
                        trackColor = Color(0xFFE1E3D3)
                    )
                }

                // Metric 3: Active Risks
                val alertsCount = report.results.count { it.triggered }
                val alertsRatio = if (report.results.isEmpty()) 0.0f else (alertsCount.toFloat() / report.results.size.toFloat())
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text("Active Risks", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$alertsCount Triggered", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (alertsCount > 0) CrimsonDanger else EmeraldSuccess)
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { alertsRatio },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = if (alertsCount > 3) CrimsonDanger else if (alertsCount > 0) AmberWarning else EmeraldSuccess,
                        trackColor = Color(0xFFE1E3D3)
                    )
                }
            }
        }
    }
}

@Composable
fun InsightCard(result: RuleCheckResult) {
    // Determine card styling based on level to apply warm minimalist palette container
    val isOpportunity = result.category.equals("Opportunities", ignoreCase = true)
    
    val (statusLabel, iconElement, bgClassColor, textClassColor, borderStroke, iconColor) = when {
        !result.triggered -> {
            if (isOpportunity) {
                // Compliant opportunity
                Spacer(modifier = Modifier.height(0.dp)) // trivial placeholder
                android.util.Log.d("XRay", "Compliant opp")
                Sextext(
                    "OPPORTUNITY ACCESSIBLE",
                    Icons.Default.CheckCircle,
                    Color(0xFFD1E9CF),
                    Color(0xFF002107),
                    BorderStroke(1.dp, Color(0xFFD1E9CF)),
                    EmeraldSuccess
                )
            } else {
                Sextext(
                    "COMPLIANT",
                    Icons.Default.CheckCircle,
                    Color.White,
                    Color(0xFF1B1C17),
                    BorderStroke(1.dp, Color(0xFFE1E3D3)),
                    EmeraldSuccess
                )
            }
        }
        result.riskLevel == "High" -> Sextext(
            "HIGH RISK DETECTED",
            Icons.Default.Warning,
            Color(0xFFFFDAD6),
            Color(0xFF410002),
            BorderStroke(1.5.dp, CrimsonDanger),
            CrimsonDanger
        )
        result.riskLevel == "Medium" -> Sextext(
            "ATTENTION REQUIRED",
            Icons.Default.Warning,
            Color(0xFFFFF1D6),
            Color(0xFF78350F),
            BorderStroke(1.dp, AmberWarning),
            AmberWarning
        )
        else -> {
            if (isOpportunity) {
                Sextext(
                    "OPPORTUNITY GAIN",
                    Icons.Default.CheckCircle,
                    Color(0xFFD1E9CF),
                    Color(0xFF002107),
                    BorderStroke(1.dp, EmeraldSuccess),
                    EmeraldSuccess
                )
            } else {
                Sextext(
                    "ADVISORY LOG",
                    Icons.Default.Info,
                    Color(0xFFF0F1E4),
                    Color(0xFF44483D),
                    BorderStroke(1.dp, Color(0xFFE1E3D3)),
                    Color(0xFF44483D)
                )
            }
        }
    }

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("rule_card_${result.ruleId}"),
        colors = CardDefaults.cardColors(containerColor = bgClassColor),
        border = borderStroke,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = iconElement,
                    contentDescription = "Alert status indicator",
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.ruleName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = textClassColor
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = statusLabel,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = iconColor
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Framework: ${result.category}",
                            color = textClassColor.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse detail" else "Expand detail",
                    tint = textClassColor.copy(alpha = 0.5f)
                )
            }

            if (result.triggered) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Observation:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = textClassColor
                )
                Text(
                    text = result.observation,
                    fontSize = 13.sp,
                    color = textClassColor.copy(alpha = 0.9f)
                )
            }

            AnimatedVisibility(
                visible = expanded || result.riskLevel == "High",
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFE1E3D3), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Why It Matters:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = textClassColor
                    )
                    Text(
                        text = result.relevanceExplanation,
                        fontSize = 13.sp,
                        color = textClassColor.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EmeraldSuccess.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .border(1.dp, EmeraldSuccess.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Recommended CA Action Plan:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = EmeraldSuccess
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = result.recommendedRemediation,
                                fontSize = 12.sp,
                                color = Color(0xFF002107),
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// Inline helper holder for 6 variables
data class Sextext(
    val statusLabel: String,
    val iconElement: androidx.compose.ui.graphics.vector.ImageVector,
    val bgClassColor: Color,
    val textClassColor: Color,
    val borderStroke: BorderStroke,
    val iconColor: Color
)

@Composable
fun WelcomeEmptyStateCard(onLoadSample: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(IndigoBrand.copy(alpha = 0.1f), RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = IndigoBrand,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome to Business X-Ray",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = SlatePrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "A deterministic CA-grade compliance and financial health reviewing workbench. We analyze your ledger CSV data against 20 key chartered accountant checkpoints.",
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onLoadSample,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("welcome_load_sample_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoBrand)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Load Auditing Demo Assets (CSV)", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun HistoricReportItem(
    report: FinancialReport,
    isActive: Boolean,
    onSelect: () -> Unit
) {
    val dateString = try {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)
        sdf.format(Date(report.timestamp))
    } catch (e: Exception) {
        ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("historic_item_${report.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) IndigoBrand.copy(alpha = 0.05f) else Color.White
        ),
        border = BorderStroke(
            1.dp,
            if (isActive) IndigoBrand else Color(0xFFE2E8F0)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.companyName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SlatePrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Period: ${report.fiscalPeriod}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateString,
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = when {
                            report.healthScore >= 80 -> EmeraldSuccess.copy(alpha = 0.1f)
                            report.healthScore >= 60 -> AmberWarning.copy(alpha = 0.1f)
                            else -> CrimsonDanger.copy(alpha = 0.1f)
                        },
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${report.healthScore}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = when {
                        report.healthScore >= 80 -> EmeraldSuccess
                        report.healthScore >= 60 -> AmberWarning
                        else -> CrimsonDanger
                    }
                )
            }
        }
    }
}

@Composable
fun FileIngestScreen(
    viewModel: BusinessXRayViewModel,
    uiState: UiState
) {
    val csvInput by viewModel.csvInput.collectAsState()
    val companyName by viewModel.companyNameInput.collectAsState()
    val fiscalPeriod by viewModel.fiscalPeriodInput.collectAsState()
    val currency by viewModel.currencyInput.collectAsState()

    var showActiveRules by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Financial Statements Compliance Gate",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = SlatePrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Incorporate manual transaction records. These will be vetted against security schemas before triggering the CA heuristics engine.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Metadata inputs
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { viewModel.companyNameInput.value = it },
                    label = { Text("Company Legal Entity Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_company_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = fiscalPeriod,
                        onValueChange = { viewModel.fiscalPeriodInput.value = it },
                        label = { Text("Fiscal Period (e.g. 2026-Q1)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_fiscal_period"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = currency,
                        onValueChange = { viewModel.currencyInput.value = it },
                        label = { Text("Currency (e.g. USD)") },
                        modifier = Modifier
                            .weight(0.6f)
                            .testTag("input_currency"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Source CSV Transaction Ledger:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SlatePrimary
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Large ledger input textarea
                OutlinedTextField(
                    value = csvInput,
                    onValueChange = { viewModel.csvInput.value = it },
                    placeholder = {
                        Text(
                            "Date,Type,Category,Entity,Amount,Description\n2026-03-01,Revenue,Consulting,Acme Corp,15000.00,Monthly fees...",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .testTag("input_csv_area"),
                    maxLines = 100,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.loadSampleData() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_load_sample_csv")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pre-load Demo")
                    }

                    Button(
                        onClick = { viewModel.triggerAnalysis() },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("btn_evaluate_pipeline"),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoBrand)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verify & Audit")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dynamic State feedback (Loading or Compliance failures)
        when (uiState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = IndigoBrand)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Running automated compliance vetting & CA ledger checks...", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            is UiState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CrimsonDanger.copy(alpha = 0.05f)),
                    border = BorderStroke(1.5.dp, CrimsonDanger),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = "Vetting error sign", tint = CrimsonDanger)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Compliance Error Blocked Ingestion",
                                fontWeight = FontWeight.Bold,
                                color = CrimsonDanger,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = uiState.message,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = SlatePrimary
                        )

                        if (uiState.errors.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = CrimsonDanger.copy(alpha = 0.2f), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            uiState.errors.forEach { err ->
                                Text(
                                    text = "Code ${err.errorCode}: ${err.errorMessage}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = CrimsonDanger
                                )
                                Text(
                                    text = "Impact field: ${err.field}",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = "Vetting Resolution: ${err.resolution}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EmeraldSuccess
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.dismissState() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonDanger),
                            border = BorderStroke(1.dp, CrimsonDanger)
                        ) {
                            Text("Acknowledge & Correct")
                        }
                    }
                }
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Toggle list of 20 CA Rules currently active in logic engine
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showActiveRules = !showActiveRules },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.List, contentDescription = null, tint = IndigoBrand)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Vetting Rules Library (20 Checklist checks)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SlatePrimary
                        )
                    }
                    Icon(
                        imageVector = if (showActiveRules) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Rules List view",
                        tint = Color.Gray
                    )
                }

                if (showActiveRules) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val context = LocalContext.current
                    val ruleList = remember(context) { RulesEngine.loadRules(context) }
                    ruleList.forEachIndexed { i, rule ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (rule.riskLevel) {
                                                "High" -> CrimsonDanger.copy(alpha = 0.1f)
                                                else -> AmberWarning.copy(alpha = 0.1f)
                                            },
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = rule.riskLevel,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (rule.riskLevel) {
                                            "High" -> CrimsonDanger
                                            else -> AmberWarning
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${i + 1}. ${rule.ruleName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = SlatePrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Rule Concept: ${rule.relevanceExplanation}",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                            if (i < ruleList.size - 1) {
                                Spacer(modifier = Modifier.height(4.dp))
                                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeveloperComplianceScreen(
    logs: List<AuditLog>
) {
    val clipboardManager = LocalClipboardManager.current
    var copiedText by remember { mutableStateOf<String?>(null) }

    val mockComplianceSchemaJson = """
{
  "${'$'}schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "BusinessXRayComplianceSchema",
  "required": [
    "company_name",
    "fiscal_period",
    "currency",
    "Amount",
    "Type",
    "Date"
  ],
  "validation_rules": {
    "CUSTOMER_CONCENTRATION": "Top customer > 35%",
    "CASH_RUNWAY": "Insolvency risk if < 90 Days",
    "PAYROLL_DRIFT": "Payroll surges > 20% without revenue support"
  }
}
""".trimIndent()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = IndigoBrand)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vetting Pipeline Schemas & API Docs",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SlatePrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Integrating Business X-Ray validation APIs into corporate governance streams is highly feasible. Below is the standardized compliance JSON schemas for automatic reporting pipelines.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vetting Ingest Schema (JSON Validation):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = SlatePrimary
                    )

                    TextButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(mockComplianceSchemaJson))
                            copiedText = "Vetting Ingest Schema copied!"
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Schema", fontSize = 12.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateAccent, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = mockComplianceSchemaJson,
                        color = Color(0xFF38BDF8),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }

                if (copiedText != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(copiedText!!, color = EmeraldSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LaunchedEffect(copiedText) {
                        kotlinx.coroutines.delay(2000)
                        copiedText = null
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Real-time Database Audit Logs to satisfy "comprehensive audit log feature for traceability"
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "System Real-Time Audit Trail (Vetting Logs)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SlatePrimary
                )
                Text(
                    text = "Vetted scan processes recorded securely. Digital checksum digests are stored to maintain compliance traceability.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No audit log traces registered in local SQLite datastore.", color = Color.Gray, fontSize = 13.sp)
                    }
                } else {
                    logs.forEachIndexed { idx, log ->
                        val dateStr = try {
                            val sdf = SimpleDateFormat("HH:mm:ss M/dd", Locale.US)
                            sdf.format(Date(log.timestamp))
                        } catch (e: Exception) {
                            "Unknown time"
                        }

                        val badgeColor = when (log.auditEventType) {
                            "COMPLIANCE_FAILED" -> CrimsonDanger
                            "SCAN_TRIGGERED" -> IndigoBrand
                            "REPORT_GENERATED" -> EmeraldSuccess
                            else -> Color.DarkGray
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = log.auditEventType,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "By: ${log.actorId}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.DarkGray
                                    )
                                }
                                Text(
                                    text = dateStr,
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = log.actionDetails,
                                fontSize = 12.sp,
                                color = SlatePrimary,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "SHA-256: ${log.integrityChecksum.take(24)}...",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (idx < logs.size - 1) {
                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuditFunZoneCard(report: CompleteAuditReport) {
    // 1. Compliance Badge/Emblem Level
    val (badgeTitle, badgeColor, badgeDesc, shieldIcon) = when {
        report.healthScore >= 90 -> Quadruple(
            "Gold Merit Shield",
            Color(0xFFD4AF37), // Beautiful gold
            "Enterprise-Grade Compliance Integrity",
            "🛡️"
        )
        report.healthScore >= 70 -> Quadruple(
            "Silver Integrity Crest",
            Color(0xFF8C9088), // Silver
            "Sustainable Buffer Protection",
            "✨"
        )
        report.healthScore >= 50 -> Quadruple(
            "Bronze Warning Plate",
            Color(0xFFCD7F32), // Bronze
            "Active Capital Drift Risk Present",
            "⚠️"
        )
        else -> Quadruple(
            "Red Distressed Alert",
            CrimsonDanger,
            "Critical Ledger Vulnerabilities",
            "🚨"
        )
    }

    // Interactive witty quote generator rotation state
    val funQuotes = listOf(
        "If a single customer dictates >35% of your sales: you don't own a company, they own you! 🤫",
        "Wages growing while revenue is shrinking is like buying an extra-large suitcase for a staycation! 🧳",
        "Pumping Google Ads budget when sales traffic is completely flat is highly aesthetic performance art! 🎨",
        "Vendor concentration >30% means you should probably give that supplier naming rights to your kitchen fridge! 🧊",
        "Running out of cash buffers relative to costs is essentially driving an experimental vehicle on DeX with no brakes! 🏎️",
        "Auditing clean books is like eating a fiber-rich salad. Boring, but your corporate longevity will thank you! 🥗",
        "Always keep receipts. Otherwise, tax agents will assume your business expense is actually a private tropical island! 🏝️"
    )

    var currentQuoteIndex by remember { mutableStateOf((report.healthScore + 2) % funQuotes.size) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("audit_fun_zone_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F1E4)),
        border = BorderStroke(1.dp, Color(0xFFE1E3D3)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💡", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Chartered Fun-Zone Tips",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlatePrimary,
                    letterSpacing = (-0.3).sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Badge section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive miniature circular level gauge
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                        .border(1.5.dp, badgeColor, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(shieldIcon, fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "ACHIEVEMENT: $badgeTitle",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = badgeColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = badgeDesc,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlatePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Wisdom Capsule Quote Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE1E3D3), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "CA AUDITING INSIGHT:",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = funQuotes[currentQuoteIndex],
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlatePrimary,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Tap button with gorgeous dynamic ripple
                    Button(
                        onClick = {
                            currentQuoteIndex = (currentQuoteIndex + 1) % funQuotes.size
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDDE5D1),
                            contentColor = Color(0xFF1B1C17)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .align(Alignment.End),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Text("🎲 Next Fun Tip", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
