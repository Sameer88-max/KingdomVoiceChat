
package com.kingdomvoicechat

/**
 * KingdomVoiceChat
 * Central Role, Permission, Dashboard and Profile Title Configuration.
 *
 * IMPORTANT:
 * This file defines app-side role configuration.
 * Actual role assignment and permission enforcement must also
 * be verified by the Cloudflare Worker / trusted backend.
 */

enum class AppRole(val displayName: String) {
    CHIEF_HEAD_MANAGER("Chief Head Manager"),
    ASSISTANT_HEAD_MANAGER("Assistant Head Manager"),
    OPERATIONS_MANAGER("Operations Manager"),
    SUPER_ADMIN("Super Admin"),
    ADMIN("Admin"),
    BD_MANAGER("BD Manager"),
    BD("Business Developer"),
    AGENCY_MANAGER("Agency Manager"),
    AGENCY_OWNER("Agency Owner"),
    TEAM_LEADER("Team Leader"),
    COINS_MANAGER("Coins Manager"),
    SENIOR_COINS_SELLER("Senior Coins Seller"),
    COINS_SELLER("Coins Seller"),
    SENIOR_MERCHANT("Senior Merchant"),
    MERCHANT("Merchant")
}

enum class AppPermission {
    VIEW_DASHBOARD,
    VIEW_TEAM,
    MANAGE_ASSIGNED_TEAM,
    VIEW_REPORTS,
    SUBMIT_REPORTS,

    VIEW_USERS,
    VIEW_USER_PROFILES,
    MANAGE_USER_REPORTS,
    WARN_USERS,
    SUSPEND_USERS,

    VIEW_ROOMS,
    MANAGE_ASSIGNED_ROOMS,
    MODERATE_ROOMS,

    VIEW_BD_TEAM,
    MANAGE_BD_TEAM,
    REGISTER_AGENCIES,
    VIEW_AGENCIES,
    MANAGE_ASSIGNED_AGENCIES,
    MANAGE_OWN_AGENCY,
    INVITE_AGENCY_MEMBERS,

    VIEW_SELLERS,
    MANAGE_ASSIGNED_SELLERS,
    VIEW_MERCHANTS,
    MANAGE_ASSIGNED_MERCHANTS,

    VIEW_COIN_STOCK,
    VIEW_COIN_BALANCE,
    CREDIT_COINS,
    VERIFY_OFFLINE_PAYMENT,
    VIEW_COIN_REPORTS,
    MANAGE_COIN_LIMITS,

    VIEW_TASKS,
    MANAGE_ASSIGNED_TASKS,

    VIEW_OPERATIONAL_REPORTS,
    MANAGE_ANNOUNCEMENTS,

    VIEW_ROLE_DETAILS,
    MANAGE_ROLE_ASSIGNMENTS,
    MANAGE_ROLE_PERMISSIONS,
    MANAGE_TITLE_DESIGNS
}

enum class DashboardModule {
    TEAM_OVERVIEW,
    TEAM_MEMBERS,
    DEPARTMENT_REPORTS,
    TASKS,
    USER_SEARCH,
    USER_REPORTS,
    USER_MODERATION,
    ROOM_MANAGEMENT,
    BD_MANAGEMENT,
    AGENCY_MANAGEMENT,
    SELLER_MANAGEMENT,
    MERCHANT_MANAGEMENT,
    COIN_STOCK,
    COIN_CREDIT,
    OFFLINE_PAYMENT_CONFIRMATION,
    COIN_REPORTS,
    ROLE_DETAILS,
    ANNOUNCEMENTS
}

data class RoleTitleStyle(
    val text: String,
    val startColor: String,
    val endColor: String,
    val textColor: String = "#FFFFFF",
    val borderColor: String = "#FFFFFF",
    val icon: String = "✦",
    val bold: Boolean = true,
    val glowEnabled: Boolean = true
)

data class RoleDefinition(
    val role: AppRole,
    val dashboardName: String,
    val permissions: Set<AppPermission>,
    val modules: Set<DashboardModule>,
    val defaultTitleStyle: RoleTitleStyle
)

data class RoleAssignment(
    val userId: String,
    val role: AppRole,
    val assignedBy: String,
    val customPermissions: Set<AppPermission>? = null,
    val titleStyle: RoleTitleStyle? = null,
    val enabled: Boolean = true
)

object RoleAccessManager {

    private fun permissions(
        vararg values: AppPermission
    ) = values.toSet()

    private fun modules(
        vararg values: DashboardModule
    ) = values.toSet()

    private fun style(
        role: AppRole,
        start: String,
        end: String
    ) = RoleTitleStyle(
        text = role.displayName.uppercase(),
        startColor = start,
        endColor = end
    )

    val roleDefinitions: Map<AppRole, RoleDefinition> =
        listOf(

            RoleDefinition(
                AppRole.CHIEF_HEAD_MANAGER,
                "Chief Head Manager Dashboard",
                permissions(
                    AppPermission.VIEW_DASHBOARD,
                    AppPermission.VIEW_TEAM,
                    AppPermission.VIEW_REPORTS,
                    AppPermission.SUBMIT_REPORTS,
                    AppPermission.VIEW_BD_TEAM,
                    AppPermission.VIEW_AGENCIES,
                    AppPermission.VIEW_SELLERS,
                    AppPermission.VIEW_MERCHANTS,
                    AppPermission.VIEW_OPERATIONAL_REPORTS,
                    AppPermission.VIEW_TASKS
                ),
                modules(
                    DashboardModule.TEAM_OVERVIEW,
                    DashboardModule.TEAM_MEMBERS,
                    DashboardModule.DEPARTMENT_REPORTS,
                    DashboardModule.TASKS,
                    DashboardModule.BD_MANAGEMENT,
                    DashboardModule.AGENCY_MANAGEMENT,
                    DashboardModule.SELLER_MANAGEMENT,
                    DashboardModule.MERCHANT_MANAGEMENT
                ),
                style(AppRole.CHIEF_HEAD_MANAGER, "#7117B8", "#E5A500")
            ),

            RoleDefinition(
                AppRole.ASSISTANT_HEAD_MANAGER,
                "Assistant Head Manager Dashboard",
                permissions(
                    AppPermission.VIEW_DASHBOARD,
                    AppPermission.VIEW_TEAM,
                    AppPermission.MANAGE_ASSIGNED_TEAM,
                    AppPermission.VIEW_REPORTS,
                    AppPermission.SUBMIT_REPORTS,
                    AppPermission.VIEW_TASKS,
                    AppPermission.MANAGE_ASSIGNED_TASKS
                ),
                modules(
                    DashboardModule.TEAM_OVERVIEW,
                    DashboardModule.TEAM_MEMBERS,
                    DashboardModule.DEPARTMENT_REPORTS,
                    DashboardModule.TASKS
                ),
                style(AppRole.ASSISTANT_HEAD_MANAGER, "#2447C6", "#8B5CF6")
            ),

            RoleDefinition(
                AppRole.OPERATIONS_MANAGER,
                "Operations Manager Dashboard",
                permissions(
                    AppPermission.VIEW_DASHBOARD,
                    AppPermission.VIEW_ROOMS,
                    AppPermission.MANAGE_ASSIGNED_ROOMS,
                    AppPermission.VIEW_OPERATIONAL_REPORTS,
                    AppPermission.VIEW_USERS,
                    AppPermission.VIEW_TASKS
                ),
                modules(
                    DashboardModule.TEAM_OVERVIEW,
                    DashboardModule.ROOM_MANAGEMENT,
                    DashboardModule.USER_SEARCH,
                    DashboardModule.DEPARTMENT_REPORTS,
                    DashboardModule.TASKS
                ),
                style(AppRole.OPERATIONS_MANAGER, "#087F8C", "#20C997")
            ),

            RoleDefinition(
                AppRole.SUPER_ADMIN,
                "Super Admin Dashboard",
                permissions(
                    AppPermission.VIEW_DASHBOARD,
                    AppPermission.VIEW_USERS,
                    AppPermission.VIEW_USER_PROFILES,
                    AppPermission.MANAGE_USER_REPORTS,
                    AppPermission.WARN_USERS,
                    AppPermission.SUSPEND_USERS,
                    AppPermission.VIEW_ROOMS,
                    AppPermission.MODERATE_ROOMS,
                    AppPermission.MANAGE_ANNOUNCEMENTS
                ),
                modules(
                    DashboardModule.TEAM_OVERVIEW,
                    DashboardModule.USER_SEARCH,
                    DashboardModule.USER_REPORTS,
                    DashboardModule.USER_MODERATION,
                    DashboardModule.ROOM_MANAGEMENT,
                    DashboardModule.ANNOUNCEMENTS
                ),
                style(AppRole.SUPER_ADMIN, "#A51C1C", "#F97316")
            ),

            RoleDefinition(
                AppRole.ADMIN,
                "Admin Dashboard",
                permissions(
                    AppPermission.VIEW_DASHBOARD,
                    AppPermission.VIEW_USERS,
                    AppPermission.VIEW_USER_PROFILES,
                    AppPermission.MANAGE_USER_REPORTS,
                    AppPermission.WARN_USERS,
                    AppPermission.VIEW_ROOMS,
                    AppPermission.MANAGE_ASSIGNED_ROOMS
                ),
                modules(
                    DashboardModule.USER_SEARCH,
                    DashboardModule.USER_REPORTS,
                    DashboardModule.USER_MODERATION,
                    DashboardModule.ROOM_MANAGEMENT
                ),
                style(AppRole.ADMIN, "#174EA6", "#42A5F5")
            ),

            RoleDefinition(
                AppRole.BD_MANAGER,
                "BD Manager Dashboard",
                permissions(
                    AppPermission.VIEW_DASHBOARD,
                    AppPermission.VIEW_BD_TEAM,
                    AppPermission.MANAGE_BD_TEAM,
                    AppPermission.VIEW_AGENCIES,
                    AppPermission.VIEW_REPORTS,
                    AppPermission.VIEW_TASKS
                ),
                modules(
                    DashboardModule.TEAM_MEMBERS,
                    DashboardModule.BD_MANAGEMENT,
                    DashboardModule.AGENCY_MANAGEMENT,
                    DashboardModule.DEPARTMENT_REPORTS,
                    DashboardModule.TASKS
                ),
                style(AppRole.BD_MANAGER, "#6D28D9", "#C084FC")
            ),

            RoleDefinition(
                AppRole.BD,
                "Business Developer Dashboard",
                permissions(
                    AppPermission.VIEW_DASHBOARD,
                    AppPermission.REGISTER_AGENCIES,
                    AppPermission.VIEW_AGENCIES,
                    AppPermission.SUBMIT_REPORTS
                ),
                modules(
                    DashboardModule.AGENCY_MANAGEMENT,
                    DashboardModule.DEPARTMENT_REPORTS
                ),
                style(AppRole.BD, "#00695C", "#26A69A")
            ),

            RoleDefinition(
                AppRole.AGENCY_MANAGER,
                "Agency Manager Dashboard",
                permissions(
                    AppPermission.VIEW_DASHBOARD,
                    AppPermission.VIEW_AGENCIES,
                    AppPermission.MANAGE_ASSIGNED_AGENCIES,
                    AppPermission.VIEW_REPORTS,
                    AppPermission.VIEW_TASKS
                ),
                modules(
                    DashboardModule.AGENCY_MANAGEMENT,
                    DashboardModule.TEAM_MEMBERS,
                    DashboardModule.DEPARTMENT_REPORTS,
                    DashboardModule.TASKS
                ),
                style(AppRole.AGENCY_MANAGER, "#37474F", "#78909C")
            ),

            RoleDefinition(
                AppRole.AGENCY_OWNER,
                "Agency Owner Dashboard",
                permissions(
                    AppPermission.VIEW_DASHBOARD,
                    AppPermission.MANAGE_OWN_AGENCY,
                    AppPermission.INVITE_AGENCY_MEMBERS,
                    AppPermission.VIEW_REPORTS,
                    AppPermission.VIEW_TASKS
                ),
                modules(
                    DashboardModule.AGENCY_MANAGEMENT,
                    DashboardModule.TEAM_MEMBERS,
                    DashboardModule.DEPARTMENT_REPORTS,
                    DashboardModule.TASKS
                ),
                style(AppRole.AGENCY_OWNER, "#8E24AA", "#EC407A")
            ),

            RoleDefinition(
                AppRole.TEAM_LEADER,
                "Team Leader Dashboard",
                permissions(
                    AppPermission.VIEW_DASHBOARD,
                    AppPermission.VIEW_TEAM,
                    AppPermission.MANAGE_ASSIGNED_TEAM,
                    AppPermission.VIEW_TASKS,
                    AppPermission.MANAGE_ASSIGNED_TASKS,
                    AppPermission.SUBMIT_REPORTS
                ),
                modules(
                    DashboardModule.TEAM_OVERVIEW,
                    DashboardModule.TEAM_MEMBERS,
                    DashboardModule.TASKS,
                    DashboardModule.DEPARTMENT_REPORTS
                ),
                style(AppRole.TEAM_LEADER, "#1565C0", "#00ACC1")
            ),

            RoleDefinition(
                AppRole.COINS_MANAGER,
                "Coins Manager Dashboard",
                permissions(
                    AppPermission.VIEW_DASHBOARD,
                    AppPermission.VIEW_SELLERS,
                    AppPermission.VIEW_MERCHANTS,
                    AppPermission.VIEW_COIN_STOCK,
                    AppPermission.VIEW_COIN_REPORTS,
                    AppPermission.MANAGE_COIN_LIMITS
                ),
                modules(
                    DashboardModule.SELLER_MANAGEMENT,
                    DashboardModule.MERCHANT_MANAGEMENT,
                    DashboardModule.COIN_STOCK,
                    DashboardModule.COIN_REPORTS
                ),
                style(AppRole.COINS_MANAGER, "#B77900", "#F5C542")
            ),

            RoleDefinition(
                AppRole.SENIOR_COINS_SELLER,
                "Senior Coins Seller Dashboard",
                permissions(
                    AppPermission.VIEW_DASHBOARD,
                    AppPermission.VIEW_COIN_STOCK,
                    AppPermission.VIEW_COIN_BALANCE,
                    AppPermission.VIEW_SELLERS,
                    AppPermission.MANAGE_ASSIGNED_SELLERS,
                    AppPermission.CREDIT_COINS,
                    AppPermission.VERIFY_OFFLINE_PAYMENT,
                    AppPermission.VIEW_COIN_REPORTS
                ),
                modules(
                    DashboardModule.COIN_STOCK,
                    DashboardModule.COIN_CREDIT,
                    DashboardModule.OFFLINE_PAYMENT_CONFIRMATION,
                    DashboardModule.SELLER_MANAGEMENT,
                    DashboardModule.COIN_REPORTS
                ),
                style(AppRole.SENIOR_COINS_SELLER, "#8D4B00", "#F59E0B")
            ),

            RoleDefinition(
                AppRole.COINS_SELLER,
                "Coins Seller Dashboard",
                permissions(
                    AppPermission.VIEW_DASHBOARD,
                    AppPermission.VIEW_COIN_STOCK,
                    AppPermission.VIEW_COIN_BALANCE,
                    AppPermission.CREDIT_COINS,
                    AppPermission.VERIFY_OFFLINE_PAYMENT,
                    AppPermission.VIEW_COIN_REPORTS
                ),
                modules(
                    DashboardModule.COIN_STOCK,
                    DashboardModule.COIN_CREDIT,
                    DashboardModule.OFFLINE_PAYMENT_CONFIRMATION,
                    DashboardModule.COIN_REPORTS
                ),
                style(AppRole.COINS_SELLER, "#075985", "#06B6D4")
            ),

            RoleDefinition(
                AppRole.SENIOR_MERCHANT,
                "Senior Merchant Dashboard",
                permissions(
                    AppPermission.VIEW_DASHBOARD,
                    AppPermission.VIEW_COIN_STOCK,
                    AppPermission.VIEW_COIN_BALANCE,
                    AppPermission.VIEW_MERCHANTS,
                    AppPermission.MANAGE_ASSIGNED_MERCHANTS,
                    AppPermission.CREDIT_COINS,
                    AppPermission.VERIFY_OFFLINE_PAYMENT,
                    AppPermission.VIEW_COIN_REPORTS
                ),
                modules(
                    DashboardModule.COIN_STOCK,
                    DashboardModule.COIN_CREDIT,
                    DashboardModule.OFFLINE_PAYMENT_CONFIRMATION,
                    DashboardModule.MERCHANT_MANAGEMENT,
                    DashboardModule.COIN_REPORTS
                ),
                style(AppRole.SENIOR_MERCHANT, "#6B21A8", "#D946EF")
            ),

            RoleDefinition(
                AppRole.MERCHANT,
                "Merchant Dashboard",
                permissions(
                    AppPermission.VIEW_DASHBOARD,
                    AppPermission.VIEW_COIN_STOCK,
                    AppPermission.VIEW_COIN_BALANCE,
                    AppPermission.CREDIT_COINS,
                    AppPermission.VERIFY_OFFLINE_PAYMENT,
                    AppPermission.VIEW_COIN_REPORTS
                ),
                modules(
                    DashboardModule.COIN_STOCK,
                    DashboardModule.COIN_CREDIT,
                    DashboardModule.OFFLINE_PAYMENT_CONFIRMATION,
                    DashboardModule.COIN_REPORTS
                ),
                style(AppRole.MERCHANT, "#9D174D", "#F472B6")
            )

        ).associateBy { it.role }

    fun getRoleDefinition(role: AppRole): RoleDefinition? {
        return roleDefinitions[role]
    }

    fun getDashboardName(role: AppRole): String {
        return roleDefinitions[role]?.dashboardName ?: ""
    }

    fun getDefaultPermissions(role: AppRole): Set<AppPermission> {
        return roleDefinitions[role]?.permissions ?: emptySet()
    }

    fun getDefaultModules(role: AppRole): Set<DashboardModule> {
        return roleDefinitions[role]?.modules ?: emptySet()
    }

    fun getProfileTitle(
        assignment: RoleAssignment
    ): RoleTitleStyle? {
        if (!assignment.enabled) return null

        return assignment.titleStyle
            ?: roleDefinitions[assignment.role]?.defaultTitleStyle
    }

    fun hasPermission(
        assignment: RoleAssignment?,
        permission: AppPermission
    ): Boolean {
        if (assignment == null || !assignment.enabled) return false

        val allowed = assignment.customPermissions
            ?: getDefaultPermissions(assignment.role)

        return permission in allowed
    }

    fun getAllowedPermissions(
        assignment: RoleAssignment?
    ): Set<AppPermission> {
        if (assignment == null || !assignment.enabled) {
            return emptySet()
        }

        return assignment.customPermissions
            ?: getDefaultPermissions(assignment.role)
    }

    fun getAllowedModules(
        assignment: RoleAssignment?
    ): Set<DashboardModule> {
        if (assignment == null || !assignment.enabled) {
            return emptySet()
        }

        val definition = roleDefinitions[assignment.role]
            ?: return emptySet()

        return definition.modules
    }

    fun isCoinsOperator(
        assignment: RoleAssignment?
    ): Boolean {
        return hasPermission(
            assignment,
            AppPermission.CREDIT_COINS
        ) && hasPermission(
            assignment,
            AppPermission.VERIFY_OFFLINE_PAYMENT
        )
    }

    fun canManageRoles(
        assignment: RoleAssignment?
    ): Boolean {
        return hasPermission(
            assignment,
            AppPermission.MANAGE_ROLE_ASSIGNMENTS
        ) && hasPermission(
            assignment,
            AppPermission.MANAGE_ROLE_PERMISSIONS
        )
    }
}
