package com.example.data.model

enum class IssueSeverity {
    HEALTHY,
    WARNING,
    CRITICAL
}

data class DiagnosticIssue(
    val id: String,
    val title: String,
    val whyItMatters: String,
    val recommendedAction: String,
    val severity: IssueSeverity,
    val category: String
)

enum class TestStatus {
    NOT_RUN,
    PASSED,
    FAILED,
    SKIPPED
}

data class HardwareTestItem(
    val id: String,
    val titleResId: Int,
    val descriptionResId: Int,
    val iconName: String,
    val status: TestStatus = TestStatus.NOT_RUN,
    val metricValue: String? = null
)
