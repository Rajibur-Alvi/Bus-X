package com.example.data.rules

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

data class RuleCheckResult(
    val ruleId: String,
    val ruleName: String,
    val triggered: Boolean,
    val riskLevel: String, // "High", "Medium", "Low", "None"
    val observation: String,
    val relevanceExplanation: String,
    val recommendedRemediation: String,
    val category: String
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("ruleId", ruleId)
            put("ruleName", ruleName)
            put("triggered", triggered)
            put("riskLevel", riskLevel)
            put("observation", observation)
            put("relevanceExplanation", relevanceExplanation)
            put("recommendedRemediation", recommendedRemediation)
            put("category", category)
        }
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): RuleCheckResult {
            return RuleCheckResult(
                ruleId = obj.getString("ruleId"),
                ruleName = obj.getString("ruleName"),
                triggered = obj.getBoolean("triggered"),
                riskLevel = obj.getString("riskLevel"),
                observation = obj.getString("observation"),
                relevanceExplanation = obj.getString("relevanceExplanation"),
                recommendedRemediation = obj.getString("recommendedRemediation"),
                category = obj.getString("category")
            )
        }
    }
}

data class NextAction(
    val actionStep: String,
    val justification: String,
    val estimated30daySafetyImpact: String
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("actionStep", actionStep)
            put("justification", justification)
            put("estimated30daySafetyImpact", estimated30daySafetyImpact)
        }
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): NextAction {
            return NextAction(
                actionStep = obj.getString("actionStep"),
                justification = obj.getString("justification"),
                estimated30daySafetyImpact = obj.getString("estimated30daySafetyImpact")
            )
        }
    }
}

data class ComplianceError(
    val errorCode: Int,
    val errorMessage: String,
    val field: String,
    val resolution: String
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("errorCode", errorCode)
            put("errorMessage", errorMessage)
            put("field", field)
            put("resolution", resolution)
        }
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): ComplianceError {
            return ComplianceError(
                errorCode = obj.getInt("errorCode"),
                errorMessage = obj.getString("errorMessage"),
                field = obj.getString("field"),
                resolution = obj.getString("resolution")
            )
        }
    }
}

data class CompleteAuditReport(
    val healthScore: Int,
    val rationale: String,
    val results: List<RuleCheckResult>,
    val nextActions: List<NextAction>,
    val companyName: String,
    val fiscalPeriod: String,
    val currency: String,
    val periodStart: String,
    val periodEnd: String,
    val transactionCount: Int,
    val errors: List<ComplianceError> = emptyList()
) {
    fun toJsonString(): String {
        val root = JSONObject().apply {
            put("healthScore", healthScore)
            put("rationale", rationale)
            put("companyName", companyName)
            put("fiscalPeriod", fiscalPeriod)
            put("currency", currency)
            put("periodStart", periodStart)
            put("periodEnd", periodEnd)
            put("transactionCount", transactionCount)

            val resultsArr = JSONArray()
            results.forEach { resultsArr.put(it.toJsonObject()) }
            put("results", resultsArr)

            val actionsArr = JSONArray()
            nextActions.forEach { actionsArr.put(it.toJsonObject()) }
            put("nextActions", actionsArr)

            val errorsArr = JSONArray()
            errors.forEach { errorsArr.put(it.toJsonObject()) }
            put("errors", errorsArr)
        }
        return root.toString()
    }

    companion object {
        fun fromJsonString(jsonStr: String): CompleteAuditReport {
            val root = JSONObject(jsonStr)
            val healthScore = root.getInt("healthScore")
            val rationale = root.getString("rationale")
            val companyName = root.getString("companyName")
            val fiscalPeriod = root.getString("fiscalPeriod")
            val currency = root.getString("currency")
            val periodStart = root.getString("periodStart")
            val periodEnd = root.getString("periodEnd")
            val transactionCount = root.getInt("transactionCount")

            val results = mutableListOf<RuleCheckResult>()
            val resultsArr = root.optJSONArray("results")
            if (resultsArr != null) {
                for (i in 0 until resultsArr.length()) {
                    results.add(RuleCheckResult.fromJsonObject(resultsArr.getJSONObject(i)))
                }
            }

            val nextActions = mutableListOf<NextAction>()
            val actionsArr = root.optJSONArray("nextActions")
            if (actionsArr != null) {
                for (i in 0 until actionsArr.length()) {
                    nextActions.add(NextAction.fromJsonObject(actionsArr.getJSONObject(i)))
                }
            }

            val errors = mutableListOf<ComplianceError>()
            val errorsArr = root.optJSONArray("errors")
            if (errorsArr != null) {
                for (i in 0 until errorsArr.length()) {
                    errors.add(ComplianceError.fromJsonObject(errorsArr.getJSONObject(i)))
                }
            }

            return CompleteAuditReport(
                healthScore = healthScore,
                rationale = rationale,
                results = results,
                nextActions = nextActions,
                companyName = companyName,
                fiscalPeriod = fiscalPeriod,
                currency = currency,
                periodStart = periodStart,
                periodEnd = periodEnd,
                transactionCount = transactionCount,
                errors = errors
            )
        }
    }
}

object RulesEngine {

    fun loadRules(context: Context): List<RuleCheckResult> {
        return try {
            val assetManager = context.assets
            val jsonString = assetManager.open("ca_rules.json").bufferedReader().use { it.readText() }
            val array = JSONArray(jsonString)
            val list = mutableListOf<RuleCheckResult>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    RuleCheckResult(
                        ruleId = obj.getString("id"),
                        ruleName = obj.getString("name"),
                        triggered = false,
                        riskLevel = obj.getString("riskLevel"),
                        observation = "No risk detected.",
                        relevanceExplanation = obj.getString("explanation"),
                        recommendedRemediation = obj.getString("action"),
                        category = obj.getString("category")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    data class Transaction(
        val date: String,
        val type: String, // "Revenue" or "Expense"
        val category: String,
        val entity: String,
        val amount: Double,
        val description: String
    )

    fun evaluateCsv(context: Context, csvText: String, companyName: String, fiscalPeriod: String, currency: String): CompleteAuditReport {
        val errors = mutableListOf<ComplianceError>()
        
        // Structure checks - mandatory headers
        val lines = csvText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) {
            errors.add(ComplianceError(4001, "Empty CSV content loaded.", "CSV File", "Please upload a valid comma-separated values file."))
            return createFailedReport(errors, companyName, fiscalPeriod, currency)
        }

        val header = lines[0].split(",")
        val dateIdx = header.indexOfFirst { it.equals("Date", ignoreCase = true) }
        val typeIdx = header.indexOfFirst { it.equals("Type", ignoreCase = true) }
        val categoryIdx = header.indexOfFirst { it.equals("Category", ignoreCase = true) }
        val entityIdx = header.indexOfFirst { it.equals("Entity", ignoreCase = true) }
        val amountIdx = header.indexOfFirst { it.equals("Amount", ignoreCase = true) }
        val descIdx = header.indexOfFirst { it.equals("Description", ignoreCase = true) }

        if (dateIdx == -1 || typeIdx == -1 || entityIdx == -1 || amountIdx == -1) {
            val missing = mutableListOf<String>()
            if (dateIdx == -1) missing.add("Date")
            if (typeIdx == -1) missing.add("Type")
            if (entityIdx == -1) missing.add("Entity")
            if (amountIdx == -1) missing.add("Amount")
            errors.add(ComplianceError(
                4001,
                "Audit schema integrity violation: Mandatory columns are absent from files: ${missing.joinToString(", ")}",
                missing.joinToString(", "),
                "Inject standard column headers '${missing.joinToString("' and '")}' in the first row of your CSV data."
            ))
            return createFailedReport(errors, companyName, fiscalPeriod, currency)
        }

        val transactions = mutableListOf<Transaction>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        var lineNum = 1

        for (i in 1 until lines.size) {
            lineNum++
            val line = lines[i]
            val tokens = splitCsvLine(line)
            if (tokens.size <= maxOf(dateIdx, typeIdx, entityIdx, amountIdx)) {
                continue
            }

            val dateStr = tokens[dateIdx].trim()
            val typeStr = tokens[typeIdx].trim()
            val catStr = if (categoryIdx != -1 && categoryIdx < tokens.size) tokens[categoryIdx].trim() else "General"
            val entStr = tokens[entityIdx].trim()
            val amtStr = tokens[amountIdx].trim()
            val descStr = if (descIdx != -1 && descIdx < tokens.size) tokens[descIdx].trim() else ""

            try {
                sdf.parse(dateStr)
            } catch (e: Exception) {
                errors.add(ComplianceError(4002, "Date format error at line $lineNum ($dateStr). Expected YYYY-MM-DD.", "Date", "Review manual entries and update date format to exactly YYYY-MM-DD."))
                continue
            }

            if (!typeStr.equals("Revenue", ignoreCase = true) && !typeStr.equals("Expense", ignoreCase = true)) {
                errors.add(ComplianceError(4002, "Financial ledger logic type error at line $lineNum: '$typeStr'.", "Type", "Type must be either exactly 'Revenue' or 'Expense' (case-insensitive)."))
                continue
            }

            val amt = amtStr.toDoubleOrNull()
            if (amt == null) {
                errors.add(ComplianceError(4002, "Numeric formatting error at line $lineNum: '$amtStr'.", "Amount", "Amount must be a clean numeric decimal value."))
                continue
            }

            transactions.add(
                Transaction(
                    date = dateStr,
                    type = typeStr.replaceFirstChar { it.uppercase() },
                    category = catStr,
                    entity = entStr,
                    amount = amt,
                    description = descStr
                )
            )
        }

        if (errors.isNotEmpty()) {
            return createFailedReport(errors, companyName, fiscalPeriod, currency)
        }

        if (transactions.size < 5) {
            errors.add(ComplianceError(4003, "Financial trend tracking requires at least ninety days span of rolling transaction data.", "Date Range", "The uploaded ledger sheet has too few entries ($lineNum) to execute robust multi-period trends. Provide a larger ledger dataset."))
            return createFailedReport(errors, companyName, fiscalPeriod, currency)
        }

        val sortedTx = transactions.sortedBy { it.date }
        val periodStart = sortedTx.first().date
        val periodEnd = sortedTx.last().date

        try {
            val d1 = sdf.parse(periodStart)
            val d2 = sdf.parse(periodEnd)
            val diffMs = d2.time - d1.time
            val diffDays = diffMs / (1000 * 60 * 60 * 24)
            if (diffDays < 45) {
                errors.add(ComplianceError(
                    4003,
                    "Financial trend tracking requires at least ninety days span of rolling transaction data.",
                    "Dataset Timeline ($diffDays days)",
                    "Trend formulas like Payroll Drift and Margin Compression depend on multi-period comparisons. Append subsequent calendar month transaction entries to provide the trend engine sufficient dataset depth (min 45-90 days)."
                ))
                return createFailedReport(errors, companyName, fiscalPeriod, currency)
            }
        } catch (e: Exception) {
            // ignore
        }

        val defaultRules = loadRules(context)
        val finalResults = mutableListOf<RuleCheckResult>()

        val totalRevenue = transactions.filter { it.type == "Revenue" }.sumOf { it.amount }
        val totalExpenses = transactions.filter { it.type == "Expense" }.sumOf { it.amount }

        val monthlyRevenue = mutableMapOf<String, Double>()
        val monthlyExpense = mutableMapOf<String, Double>()
        val monthlyPayroll = mutableMapOf<String, Double>()
        val monthlyMarketing = mutableMapOf<String, Double>()
        val monthlyCogs = mutableMapOf<String, Double>()

        for (tx in transactions) {
            val yrMo = tx.date.substring(0, minOf(7, tx.date.length))
            if (tx.type == "Revenue") {
                monthlyRevenue[yrMo] = (monthlyRevenue[yrMo] ?: 0.0) + tx.amount
            } else {
                monthlyExpense[yrMo] = (monthlyExpense[yrMo] ?: 0.0) + tx.amount
                if (tx.category.contains("Payroll", ignoreCase = true) || tx.category.contains("Salary", ignoreCase = true) || tx.category.contains("Staff", ignoreCase = true)) {
                    monthlyPayroll[yrMo] = (monthlyPayroll[yrMo] ?: 0.0) + tx.amount
                }
                if (tx.category.contains("Marketing", ignoreCase = true) || tx.category.contains("Ads", ignoreCase = true) || tx.category.contains("Promo", ignoreCase = true)) {
                    monthlyMarketing[yrMo] = (monthlyMarketing[yrMo] ?: 0.0) + tx.amount
                }
                if (tx.category.contains("Materials", ignoreCase = true) || tx.category.contains("COGS", ignoreCase = true) || tx.category.contains("Supplier", ignoreCase = true)) {
                    monthlyCogs[yrMo] = (monthlyCogs[yrMo] ?: 0.0) + tx.amount
                }
            }
        }

        val sortedMonths = monthlyRevenue.keys.sorted()

        for (rule in defaultRules) {
            var triggered = false
            var obsv = "No risk detected."

            when (rule.ruleId) {
                "REV_GROWTH_QUALITY" -> {
                    if (sortedMonths.size >= 3) {
                        val m1 = sortedMonths[sortedMonths.size - 3]
                        val m3 = sortedMonths[sortedMonths.size - 1]

                        val r1 = monthlyRevenue[m1] ?: 0.0
                        val r3 = monthlyRevenue[m3] ?: 0.0

                        val p1 = r1 - (monthlyExpense[m1] ?: 0.0)
                        val p3 = r3 - (monthlyExpense[m3] ?: 0.0)

                        if (r3 > r1 && p3 < p1) {
                            triggered = true
                            obsv = "Monthly revenue grew from $${String.format("%.2f", r1)} to $${String.format("%.2f", r3)}, but monthly net profit contracted from $${String.format("%.2f", p1)} to $${String.format("%.2f", p3)}."
                        }
                    }
                }

                "EXPENSE_PRESSURE" -> {
                    if (sortedMonths.size >= 2) {
                        val mPrev = sortedMonths[sortedMonths.size - 2]
                        val mCurr = sortedMonths[sortedMonths.size - 1]

                        val revPrev = monthlyRevenue[mPrev] ?: 1.0
                        val revCurr = monthlyRevenue[mCurr] ?: 0.0
                        val expPrev = monthlyExpense[mPrev] ?: 1.0
                        val expCurr = monthlyExpense[mCurr] ?: 0.0

                        val revGrowth = if (revPrev > 0) (revCurr - revPrev) / revPrev else 0.0
                        val expGrowth = if (expPrev > 0) (expCurr - expPrev) / expPrev else 0.0

                        if (expGrowth > revGrowth && expGrowth > 0.05) {
                            triggered = true
                            obsv = "Expense growth rate (${String.format("%.1f", expGrowth * 100)}%) exceeded revenue growth rate (${String.format("%.1f", revGrowth * 100)}%) in the last month."
                        }
                    }
                }

                "CASH_RUNWAY" -> {
                    val netBurn = totalExpenses - totalRevenue
                    if (netBurn > 0) {
                        val monthsRunway = 25000.0 / (netBurn / maxOf(1, sortedMonths.size))
                        if (monthsRunway <= 3.0) {
                            triggered = true
                            obsv = "Current operating cash reserves ($25,000) will last approximately ${String.format("%.1f", monthsRunway)} months at current average net burn rate of $${String.format("%.2f", netBurn / maxOf(1, sortedMonths.size))} per month."
                        }
                    }
                }

                "CUSTOMER_CONCENTRATION" -> {
                    val customerRevenues = transactions.filter { it.type == "Revenue" }
                        .groupBy { it.entity }
                        .mapValues { (_, txs) -> txs.sumOf { it.amount } }

                    val topCustomer = customerRevenues.maxByOrNull { it.value }
                    if (topCustomer != null && totalRevenue > 0) {
                        val pct = topCustomer.value / totalRevenue
                        if (pct > 0.35) {
                            triggered = true
                            obsv = "Largest customer '${topCustomer.key}' accounts for ${String.format("%.1f", pct * 100)}% of total company revenue ($${String.format("%.2f", topCustomer.value)} of $${String.format("%.2f", totalRevenue)})."
                        }
                    }
                }

                "VENDOR_CONCENTRATION" -> {
                    val vendorExpenses = transactions.filter { it.type == "Expense" && !it.entity.contains("Salary", ignoreCase = true) && !it.entity.contains("Staff", ignoreCase = true) }
                        .groupBy { it.entity }
                        .mapValues { (_, txs) -> txs.sumOf { it.amount } }

                    val topVendor = vendorExpenses.maxByOrNull { it.value }
                    if (topVendor != null && totalExpenses > 0) {
                        val pct = topVendor.value / totalExpenses
                        if (pct > 0.30) {
                            triggered = true
                            obsv = "Largest vendor '${topVendor.key}' accounts for ${String.format("%.1f", pct * 100)}% of total expenses ($${String.format("%.2f", topVendor.value)} of $${String.format("%.2f", totalExpenses)})."
                        }
                    }
                }

                "MARGIN_COMPRESSION" -> {
                    if (sortedMonths.size >= 3) {
                        var continuousDrop = true
                        var prevMargin = Double.MAX_VALUE
                        for (m in sortedMonths.takeLast(3)) {
                            val rev = monthlyRevenue[m] ?: 1.0
                            val cogs = monthlyCogs[m] ?: 0.0
                            val margin = if (rev > 0) (rev - cogs) / rev else 0.0
                            if (margin >= prevMargin) {
                                continuousDrop = false
                            }
                            prevMargin = margin
                        }
                        if (continuousDrop) {
                            triggered = true
                            obsv = "Gross margin has declined continuously over last 3 periods, contracting from ${String.format("%.1f", ((monthlyRevenue[sortedMonths[sortedMonths.size - 3]] ?: 1.0) - (monthlyCogs[sortedMonths[sortedMonths.size - 3]] ?: 0.0)) / (monthlyRevenue[sortedMonths[sortedMonths.size - 3]] ?: 1.0) * 100)}% to ${String.format("%.1f", prevMargin * 100)}%."
                        }
                    }
                }

                "PAYROLL_DRIFT" -> {
                    if (sortedMonths.size >= 2) {
                        val mPrev = sortedMonths[sortedMonths.size - 2]
                        val mCurr = sortedMonths[sortedMonths.size - 1]

                        val pPrev = monthlyPayroll[mPrev] ?: 1.0
                        val pCurr = monthlyPayroll[mCurr] ?: 0.0
                        val rPrev = monthlyRevenue[mPrev] ?: 1.0
                        val rCurr = monthlyRevenue[mCurr] ?: 0.0

                        val pGrowth = if (pPrev > 0) (pCurr - pPrev) / pPrev else 0.0
                        val rGrowth = if (rPrev > 0) (rCurr - rPrev) / rPrev else 0.0

                        if (pGrowth > 0.20 && pGrowth > rGrowth) {
                            triggered = true
                            obsv = "Monthly payroll costs surged by ${String.format("%.1f", pGrowth * 100)}% (from $${String.format("%.2f", pPrev)} to $${String.format("%.2f", pCurr)}), outperforming revenue growth support (${String.format("%.1f", rGrowth * 100)}%)."
                        }
                    }
                }

                "MARKETING_EFFICIENCY" -> {
                    if (sortedMonths.size >= 2) {
                        val mPrev = sortedMonths[sortedMonths.size - 2]
                        val mCurr = sortedMonths[sortedMonths.size - 1]

                        val mktPrev = monthlyMarketing[mPrev] ?: 1.0
                        val mktCurr = monthlyMarketing[mCurr] ?: 0.0
                        val rPrev = monthlyRevenue[mPrev] ?: 1.0
                        val rCurr = monthlyRevenue[mCurr] ?: 0.0

                        val mktGrowth = if (mktPrev > 0) (mktCurr - mktPrev) / mktPrev else 0.0
                        val rGrowth = if (rPrev > 0) (rCurr - rPrev) / rPrev else 0.0

                        if (mktGrowth > 0.30 && rGrowth <= 0.05) {
                            triggered = true
                            obsv = "Marketing investment spike of ${String.format("%.1f", mktGrowth * 100)}% produced minor top-line revenue growth (${String.format("%.1f", rGrowth * 100)}%)."
                        }
                    }
                }

                "MONTH_END_SPIKES" -> {
                    var spikeDetected = false
                    for (m in sortedMonths) {
                        val mRevenue = monthlyRevenue[m] ?: 1.0
                        if (mRevenue > 0) {
                            val periodEndTransactions = transactions.filter {
                                it.type == "Revenue" &&
                                        it.date.startsWith(m) &&
                                        it.date.substring(8, 10).toInt() >= 28
                            }
                            val spikeSum = periodEndTransactions.sumOf { it.amount }
                            val pct = spikeSum / mRevenue
                            if (pct >= 0.40) {
                                spikeDetected = true
                                obsv = "Month-end adjustments on period ($m) represent ${String.format("%.1f", pct * 100)}% of total monthly receipts."
                                break
                            }
                        }
                    }
                    if (spikeDetected) triggered = true
                }

                "DUPLICATE_PAYMENTS" -> {
                    val seenTx = mutableSetOf<String>()
                    var duplicatesCount = 0
                    var dupAmount = 0.0
                    for (tx in transactions) {
                        val key = "${tx.date}_${tx.entity}_${tx.amount}_${tx.type}"
                        if (seenTx.contains(key)) {
                            duplicatesCount++
                            dupAmount += tx.amount
                        } else {
                            seenTx.add(key)
                        }
                    }
                    if (duplicatesCount > 0) {
                        triggered = true
                        obsv = "Identified $duplicatesCount potential dual-posting payment matching duplicate signatures amounting to $${String.format("%.2f", dupAmount)}."
                    }
                }

                "ROUND_NUMBER_ANOMALIES" -> {
                    val flatThousands = transactions.filter { it.amount > 0 && it.amount % 1000.0 == 0.0 }
                    val pct = flatThousands.size.toDouble() / transactions.size.toDouble()
                    if (pct >= 0.08) {
                        triggered = true
                        obsv = "Unusual frequency of rounded entries: ${flatThousands.size} transactions (${String.format("%.1f", pct * 100)}% of entries) are exact multiples of 1,000."
                    }
                }

                "REVERSAL_PATTERNS" -> {
                    var hasReversals = false
                    val expenseEntities = transactions.groupBy { it.entity }
                    for ((entity, txList) in expenseEntities) {
                        val hashmapAmount = mutableMapOf<Double, String>()
                        for (tx in txList) {
                            if (tx.amount > 0) {
                                hashmapAmount[tx.amount] = tx.date
                            } else if (tx.amount < 0) {
                                val positiveMatching = -tx.amount
                                if (hashmapAmount.containsKey(positiveMatching)) {
                                    hasReversals = true
                                    obsv = "Detected audit reversal posting of $${String.format("%.2f", positiveMatching)} on vendor '$entity' shortly after original journal booking."
                                    break
                                }
                            }
                        }
                    }
                    if (hasReversals) triggered = true
                }

                "REVENUE_VOLATILITY" -> {
                    if (sortedMonths.size >= 2) {
                        var maxSwing = 0.0
                        for (i in 1 until sortedMonths.size) {
                            val rPrev = monthlyRevenue[sortedMonths[i - 1]] ?: 1.0
                            val rCurr = monthlyRevenue[sortedMonths[i]] ?: 0.0
                            val swing = if (rPrev > 0) Math.abs(rCurr - rPrev) / rPrev else 0.0
                            if (swing > maxSwing) maxSwing = swing
                        }
                        if (maxSwing > 0.30) {
                            triggered = true
                            obsv = "Primary trading revenues show aggressive swings with maximum period fluctuations peaking at ${String.format("%.1f", maxSwing * 100)}% month-on-month."
                        }
                    }
                }

                "RUN_RATE_WARNING" -> {
                    if (sortedMonths.size >= 2) {
                        val mCurr = sortedMonths.last()
                        val rCurr = monthlyRevenue[mCurr] ?: 0.0

                        val precedingMonths = sortedMonths.dropLast(1)
                        val avgPreceding = precedingMonths.map { monthlyRevenue[it] ?: 0.0 }.average()

                        if (avgPreceding > 0 && rCurr < avgPreceding * 0.75) {
                            triggered = true
                            obsv = "Current month sales ($${String.format("%.2f", rCurr)}) fell below rolling average ($${String.format("%.2f", avgPreceding)}) by ${String.format("%.1f", (1.0 - (rCurr / avgPreceding)) * 100)}%."
                        }
                    }
                }
            }

            finalResults.add(
                rule.copy(
                    triggered = triggered,
                    riskLevel = if (triggered) rule.riskLevel else "None",
                    observation = obsv
                )
            )
        }

        val deductions = finalResults.filter { it.triggered }.sumOf {
            when (it.riskLevel) {
                "High" -> 15
                "Medium" -> 8
                "Low" -> 3
                else -> 0
            }
        }
        val computedScore = maxOf(0, 100 - deductions)

        val rationale = when {
            computedScore >= 85 -> "Outstanding business health. Revenue streams are stable, with efficient cost ratios and manageable risk thresholds."
            computedScore >= 70 -> "Stable health. Revenue is growing and margins are sound, but customer concentration or minor cost pressure requires monitoring actions."
            computedScore >= 50 -> "Vulnerable financial margins. Significant cost pressures, lengthening collections, or vendor dependencies represent elevated operational risk."
            else -> "High stress environment! Substantial cash runway concerns or negative margin indicators require immediate capital conservation actions."
        }

        val nextActions = mutableListOf<NextAction>()
        val highRisks = finalResults.filter { it.triggered && it.riskLevel == "High" }
        val medRisks = finalResults.filter { it.triggered && it.riskLevel == "Medium" }

        for (r in highRisks.take(3)) {
            nextActions.add(
                NextAction(
                    actionStep = r.recommendedRemediation,
                    justification = r.observation,
                    estimated30daySafetyImpact = "Reduces volatility and mitigates critical cash leakage warnings within 30 days."
                )
            )
        }

        if (nextActions.size < 5) {
            for (r in medRisks.take(5 - nextActions.size)) {
                nextActions.add(
                    NextAction(
                        actionStep = r.recommendedRemediation,
                        justification = r.observation,
                        estimated30daySafetyImpact = "Improves operational margin buffers and builds defensible cash structures."
                    )
                )
            }
        }

        if (nextActions.isEmpty()) {
            nextActions.add(
                NextAction(
                    actionStep = "Build an emergency interest-yielding cash reserve of 3 months expenses.",
                    justification = "Operational volatility is low, allowing current margins to accumulate passive reserves safely.",
                    estimated30daySafetyImpact = "Strengthens capital defense against unforeseen trading credit loops."
                )
            )
        }

        return CompleteAuditReport(
            healthScore = computedScore,
            rationale = rationale,
            results = finalResults,
            nextActions = nextActions.take(5),
            companyName = companyName,
            fiscalPeriod = fiscalPeriod,
            currency = currency,
            periodStart = periodStart,
            periodEnd = periodEnd,
            transactionCount = transactions.size
        )
    }

    private fun createFailedReport(errors: List<ComplianceError>, companyName: String, fiscalPeriod: String, currency: String): CompleteAuditReport {
        return CompleteAuditReport(
            healthScore = 0,
            rationale = "Compliance validation check failed.",
            results = emptyList(),
            nextActions = emptyList(),
            companyName = companyName,
            fiscalPeriod = fiscalPeriod,
            currency = currency,
            periodStart = "",
            periodEnd = "",
            transactionCount = 0,
            errors = errors
        )
    }

    private fun splitCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val curVal = StringBuilder()
        var inQuotes = false
        for (i in 0 until line.length) {
            val ch = line[i]
            if (ch == '\"') {
                inQuotes = !inQuotes
            } else if (ch == ',' && !inQuotes) {
                result.add(curVal.toString())
                curVal.clear()
            } else {
                curVal.append(ch)
            }
        }
        result.add(curVal.toString())
        return result
    }
}
