
package com.kingdomvoicechat

/**
 * Monthly agency and BD Pearl earning calculator.
 *
 * This file calculates the amounts for display/settlement preview.
 * Actual Pearl wallet credits MUST be performed by the trusted backend.
 */

data class AgencyMemberMonthlyStats(
    val userId: String,
    val pearlsEarned: Long
)

data class AgencyMonthlyStats(
    val agencyId: String,
    val ownerId: String,
    val isWorking: Boolean,
    val monthlyPearlTarget: Long,
    val members: List<AgencyMemberMonthlyStats>
)

data class AgencyOwnerMonthlyEarning(
    val ownerId: String,
    val totalMemberPearls: Long,
    val earningPearls: Long
)

data class BdMonthlyEarning(
    val bdId: String,
    val nonWorkingAgencyCount: Int,
    val combinedMonthlyTarget: Long,
    val earningPearls: Long
)

data class MonthlyAgencyEarningResult(
    val monthKey: String,
    val ownerEarnings: List<AgencyOwnerMonthlyEarning>,
    val bdEarnings: List<BdMonthlyEarning>
)

object AgencyMonthlyEarnings {

    private const val OWNER_PERCENT = 10L
    private const val BD_PERCENT = 12L

    /**
     * Owner earns 10% of total pearls earned by agency members.
     * This applies whether the owner is working or not.
     */
    fun calculateOwnerEarning(
        ownerId: String,
        agencies: List<AgencyMonthlyStats>
    ): AgencyOwnerMonthlyEarning {

        val ownerAgencies = agencies.filter {
            it.ownerId == ownerId
        }

        val totalMemberPearls = ownerAgencies
            .flatMap { it.members }
            .sumOf { it.pearlsEarned.coerceAtLeast(0L) }

        val earning = percentage(
            totalMemberPearls,
            OWNER_PERCENT
        )

        return AgencyOwnerMonthlyEarning(
            ownerId = ownerId,
            totalMemberPearls = totalMemberPearls,
            earningPearls = earning
        )
    }

    /**
     * BD earns 12% of the combined monthly targets
     * of agencies assigned to that BD which are marked non-working.
     */
    fun calculateBdEarning(
        bdId: String,
        assignedAgencies: List<AgencyMonthlyStats>,
        agencyBdAssignments: Map<String, String>
    ): BdMonthlyEarning {

        val nonWorkingAgencies = assignedAgencies.filter { agency ->
            agencyBdAssignments[agency.agencyId] == bdId &&
                !agency.isWorking
        }

        val combinedTarget = nonWorkingAgencies.sumOf {
            it.monthlyPearlTarget.coerceAtLeast(0L)
        }

        val earning = percentage(
            combinedTarget,
            BD_PERCENT
        )

        return BdMonthlyEarning(
            bdId = bdId,
            nonWorkingAgencyCount = nonWorkingAgencies.size,
            combinedMonthlyTarget = combinedTarget,
            earningPearls = earning
        )
    }

    /**
     * Creates a preview for all owners and BD accounts.
     * monthKey format: YYYY-MM
     */
    fun calculateMonthlyPreview(
        monthKey: String,
        agencies: List<AgencyMonthlyStats>,
        agencyBdAssignments: Map<String, String>
    ): MonthlyAgencyEarningResult {

        require(monthKey.matches(Regex("\\d{4}-\\d{2}"))) {
            "Month must use YYYY-MM format"
        }

        val ownerIds = agencies
            .map { it.ownerId }
            .distinct()

        val bdIds = agencyBdAssignments.values.distinct()

        val ownerResults = ownerIds.map { ownerId ->
            calculateOwnerEarning(ownerId, agencies)
        }

        val bdResults = bdIds.map { bdId ->
            calculateBdEarning(
                bdId = bdId,
                assignedAgencies = agencies,
                agencyBdAssignments = agencyBdAssignments
            )
        }

        return MonthlyAgencyEarningResult(
            monthKey = monthKey,
            ownerEarnings = ownerResults,
            bdEarnings = bdResults
        )
    }

    private fun percentage(
        amount: Long,
        percent: Long
    ): Long {
        require(amount >= 0L) {
            "Amount cannot be negative"
        }

        return Math.multiplyExact(amount, percent) / 100L
    }
}
