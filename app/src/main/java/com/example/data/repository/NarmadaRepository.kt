package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import com.example.security.AppPermissions
import com.example.security.RbacAuthorizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class NarmadaRepository(
    private val devoteeDao: DevoteeDao,
    private val passSeatDao: PassSeatDao,
    private val eventProgramDao: EventProgramDao,
    private val financeDao: FinanceDao,
    private val cmsDao: CmsDao,
    private val auditDao: AuditDao,
    private val userDao: UserDao,
    private val roleDao: RoleDao
) {
    val allDevotees: Flow<List<Devotee>> = devoteeDao.getAllDevotees()
    val allPasses: Flow<List<PassItem>> = passSeatDao.getAllPasses()
    val allSeats: Flow<List<SeatItem>> = passSeatDao.getAllSeats()
    val allCheckins: Flow<List<CheckinRecord>> = passSeatDao.getAllCheckins()
    val allPrograms: Flow<List<EventProgram>> = eventProgramDao.getAllPrograms()
    val allDonations: Flow<List<DonationReceipt>> = financeDao.getAllDonations()
    val allExpenses: Flow<List<ExpenseItem>> = financeDao.getAllExpenses()
    val activeNotices: Flow<List<NoticeItem>> = cmsDao.getActiveNotices()
    val allNotices: Flow<List<NoticeItem>> = cmsDao.getAllNotices()
    val allFaqs: Flow<List<FaqItem>> = cmsDao.getAllFaqs()
    val allCmsContent: Flow<List<CmsItem>> = cmsDao.getAllCmsContent()
    val allAuditLogs: Flow<List<AuditLog>> = auditDao.getAllAuditLogs()
    val allUsers: Flow<List<AppUser>> = userDao.getAllUsers()
    val allCustomRoles: Flow<List<CustomRole>> = roleDao.getAllCustomRoles()

    val totalDonationsSum: Flow<Double?> = financeDao.getTotalDonationsSum()
    val totalExpensesSum: Flow<Double?> = financeDao.getTotalExpensesSum()
    val checkedInCount: Flow<Int> = passSeatDao.getCheckedInCount()

    suspend fun initializeDemoDataIfNeeded() = withContext(Dispatchers.IO) {
        val existing = devoteeDao.getAllDevotees().first()
        if (existing.isEmpty()) {
            seedFullDemoData()
        }
    }

    // SHA-256 generator for tamper-proof tokens
    fun generateSecureToken(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }.take(16).uppercase()
    }

    suspend fun isMobileRegistered(mobile: String): Devotee? = withContext(Dispatchers.IO) {
        devoteeDao.getDevoteeByMobile(mobile.trim())
    }

    suspend fun getDevoteeById(regId: String): Devotee? = withContext(Dispatchers.IO) {
        devoteeDao.getDevoteeById(regId.trim())
    }

    suspend fun getPassByRegistrationId(regId: String): PassItem? = withContext(Dispatchers.IO) {
        passSeatDao.getPassByRegistrationId(regId.trim())
    }

    suspend fun getGroupMembers(groupId: String): List<GroupMember> = withContext(Dispatchers.IO) {
        if (groupId.isBlank()) emptyList() else devoteeDao.getMembersByGroupIdSync(groupId)
    }

    // Registration Wizard Completion
    suspend fun registerDevoteeWithGroup(
        primaryDevotee: Devotee,
        additionalMembers: List<GroupMember>,
        autoAssignSeat: Boolean = true
    ): Triple<Devotee, PassItem, AppUser> = withContext(Dispatchers.IO) {
        // Generate Unique Reg ID if not pre-set
        val count = devoteeDao.getAllDevotees().first().size + 1
        val regId = if (primaryDevotee.registrationId.isNotBlank()) primaryDevotee.registrationId
                    else "NBF27-%06d".format(count)

        val hasGroup = additionalMembers.isNotEmpty()
        val groupId = if (hasGroup) "GRP-%04d".format(count) else ""

        val finalDevotee = primaryDevotee.copy(
            registrationId = regId,
            groupId = groupId,
            accompanyingPersonsCount = additionalMembers.size
        )

        devoteeDao.insertDevotee(finalDevotee)

        // Save additional group members
        if (hasGroup) {
            val membersWithIds = additionalMembers.mapIndexed { index, m ->
                val memberRegId = "NBF27-%06d-M%d".format(count, index + 1)
                m.copy(groupId = groupId, individualRegistrationId = memberRegId)
            }
            devoteeDao.insertGroupMembers(membersWithIds)
        }

        // Auto seat allocation check
        var allottedSeatNumber = ""
        var allottedSection = ""
        var allottedRow = ""

        if (autoAssignSeat) {
            val available = passSeatDao.getAvailableSeats()
            if (available.isNotEmpty()) {
                val seat = available.first()
                allottedSeatNumber = seat.seatNumber
                allottedSection = seat.section
                allottedRow = seat.row
                passSeatDao.assignSeat(
                    seatNum = seat.seatNumber,
                    status = "ASSIGNED",
                    devoteeId = regId,
                    name = finalDevotee.fullName,
                    grpId = groupId.ifBlank { null }
                )
            }
        }

        // Generate Pass
        val passCount = passSeatDao.getAllPasses().first().size + 1
        val passId = "PAS27-%06d".format(passCount)
        val qrToken = generateSecureToken("$regId|$passId|${finalDevotee.mobileNumber}")

        val passCategory = when {
            hasGroup -> "परिवार पास"
            finalDevotee.isChunriYatraParticipant -> "सामान्य भक्त पास"
            else -> "सामान्य भक्त पास"
        }

        val pass = PassItem(
            passId = passId,
            registrationId = regId,
            devoteeName = finalDevotee.fullName,
            category = passCategory,
            seatNumber = allottedSeatNumber,
            section = allottedSection,
            row = allottedRow,
            qrToken = qrToken,
            status = "ACTIVE"
        )
        passSeatDao.insertPass(pass)

        // Automatically create user credentials for devotee
        val autoPassword = "Narmada@%04d".format((1000..9999).random())
        val userAccount = AppUser(
            username = regId,
            email = "",
            password = autoPassword,
            fullName = finalDevotee.fullName,
            mobileNumber = finalDevotee.mobileNumber,
            role = UserRole.PUBLIC,
            registrationId = regId,
            createdBy = "AUTO_REGISTRATION"
        )
        userDao.insertUser(userAccount)

        // Audit Log
        auditDao.insertLog(
            AuditLog(
                user = "Public Wizard",
                role = "PUBLIC",
                action = "DEVOTEE_REGISTERED",
                recordType = "DEVOTEE",
                recordId = regId,
                oldValue = "",
                newValue = "${finalDevotee.fullName} (${finalDevotee.mobileNumber}), Seat: $allottedSeatNumber"
            )
        )

        Triple(finalDevotee, pass, userAccount)
    }

    // Dynamic Logo / DP Management
    fun getLogoConfig(): Flow<AppLogoConfig> = allCmsContent.map { list ->
        val map = list.associate { it.key to it.value }
        AppLogoConfig(
            type = map["app_dp_type"] ?: "PRESET",
            presetId = map["app_dp_preset"] ?: "CARTOON_MAA_NARMADA",
            customUrl = map["app_dp_url"] ?: "",
            festivalTitle = map["app_dp_title"] ?: "द्वितीय विशाल श्री मां नर्मदा जन्मोत्सव 2027",
            festivalSubtitle = map["app_dp_subtitle"] ?: "पावन चुनरी पदयात्रा एवं महाआरती महोत्सव",
            badgeText = map["app_dp_badge"] ?: "🌸 बाल रूप मां नर्मदा",
            updatedAt = map["app_dp_updated_at"]?.toLongOrNull() ?: System.currentTimeMillis(),
            updatedBy = map["app_dp_updated_by"] ?: "Deepak53802@gmail.com"
        )
    }

    suspend fun saveLogoConfig(config: AppLogoConfig, updatedBy: String) = withContext(Dispatchers.IO) {
        val items = listOf(
            CmsItem("app_dp_type", config.type),
            CmsItem("app_dp_preset", config.presetId),
            CmsItem("app_dp_url", config.customUrl),
            CmsItem("app_dp_title", config.festivalTitle),
            CmsItem("app_dp_subtitle", config.festivalSubtitle),
            CmsItem("app_dp_badge", config.badgeText),
            CmsItem("app_dp_updated_at", System.currentTimeMillis().toString()),
            CmsItem("app_dp_updated_by", updatedBy)
        )
        cmsDao.insertCmsItems(items)
        auditDao.insertLog(
            AuditLog(
                user = updatedBy,
                role = "SUPER_ADMIN",
                action = "DP_LOGO_UPDATED",
                recordType = "SYSTEM_LOGO",
                recordId = config.presetId,
                newValue = "Type: ${config.type}, Title: ${config.festivalTitle}"
            )
        )
    }

    // Authentication & Staff User Management
    suspend fun authenticate(identifier: String, pass: String): AppUser? = withContext(Dispatchers.IO) {
        val cleanId = identifier.trim()
        val user = userDao.findUserByIdentifier(cleanId)
        if (user != null && user.isActive && user.password == pass.trim()) {
            auditDao.insertLog(
                AuditLog(
                    user = user.username,
                    role = user.role.name,
                    action = "LOGIN_SUCCESS",
                    recordType = "USER",
                    recordId = user.id.toString(),
                    newValue = "Role: ${user.role.name}"
                )
            )
            user
        } else {
            null
        }
    }

    suspend fun findUserForRecovery(identifier: String): AppUser? = withContext(Dispatchers.IO) {
        userDao.findUserByIdentifier(identifier.trim())
    }

    suspend fun findUsersByMobile(mobile: String): List<AppUser> = withContext(Dispatchers.IO) {
        userDao.findUsersByMobile(mobile.trim())
    }

    suspend fun resetPassword(userId: Long, newPass: String) = withContext(Dispatchers.IO) {
        userDao.updatePassword(userId, newPass.trim())
        auditDao.insertLog(
            AuditLog(
                user = "Self/SuperAdmin",
                role = "SYSTEM",
                action = "PASSWORD_RESET",
                recordType = "USER",
                recordId = userId.toString()
            )
        )
    }

    suspend fun createStaffUser(user: AppUser, createdBy: String): Long = withContext(Dispatchers.IO) {
        val id = userDao.insertUser(user)
        auditDao.insertLog(
            AuditLog(
                user = createdBy,
                role = "SUPER_ADMIN",
                action = "STAFF_CREATED",
                recordType = "USER",
                recordId = id.toString(),
                newValue = "${user.fullName} (${user.role.name})"
            )
        )
        id
    }

    suspend fun updateStaffUser(user: AppUser, updatedBy: String) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
        auditDao.insertLog(
            AuditLog(
                user = updatedBy,
                role = "SUPER_ADMIN",
                action = "STAFF_UPDATED",
                recordType = "USER",
                recordId = user.id.toString()
            )
        )
    }

    suspend fun toggleStaffActive(userId: Long, isActive: Boolean, toggledBy: String) = withContext(Dispatchers.IO) {
        userDao.toggleActive(userId, isActive)
        auditDao.insertLog(
            AuditLog(
                user = toggledBy,
                role = "SUPER_ADMIN",
                action = if (isActive) "STAFF_ACTIVATED" else "STAFF_DEACTIVATED",
                recordType = "USER",
                recordId = userId.toString()
            )
        )
    }

    suspend fun deleteStaffUser(user: AppUser, deletedBy: String): Boolean = withContext(Dispatchers.IO) {
        if (user.email.equals("Deepak53802@gmail.com", ignoreCase = true) ||
            user.username.equals("Deepak53802@gmail.com", ignoreCase = true)) {
            return@withContext false
        }
        userDao.deleteUser(user)
        auditDao.insertLog(
            AuditLog(
                user = deletedBy,
                role = "SUPER_ADMIN",
                action = "STAFF_DELETED",
                recordType = "USER",
                recordId = user.id.toString(),
                newValue = user.username
            )
        )
        true
    }

    // ==========================================
    // RBAC PERMISSION ENFORCEMENT & SECURITY
    // ==========================================

    suspend fun getCustomRolesMap(): Map<String, CustomRole> = withContext(Dispatchers.IO) {
        try {
            roleDao.getAllCustomRoles().first().associateBy { it.id }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun checkPermission(
        actor: AppUser?,
        permissionId: String,
        actionName: String,
        recordType: String = "",
        recordId: String = ""
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val rolesMap = getCustomRolesMap()
        val check = RbacAuthorizer.enforce(actor, permissionId, actionName, rolesMap)
        if (check.isFailure) {
            auditDao.insertLog(
                AuditLog(
                    user = actor?.username ?: "ANONYMOUS",
                    role = actor?.role?.name ?: "UNKNOWN",
                    action = "${actionName.uppercase()}_DENIED",
                    recordType = recordType,
                    recordId = recordId,
                    oldValue = "Unauthorized attempt",
                    newValue = "Required: $permissionId",
                    permissionUsed = permissionId,
                    status = "DENIED"
                )
            )
            return@withContext check
        }
        Result.success(Unit)
    }

    // Custom Roles Management (Super Admin & Authorized Admin)
    suspend fun createCustomRole(role: CustomRole, actor: AppUser?): Result<Unit> = withContext(Dispatchers.IO) {
        val permCheck = checkPermission(actor, AppPermissions.ROLE_CREATE, "कस्टम भूमिका निर्माण", "CUSTOM_ROLE", role.id)
        if (permCheck.isFailure) return@withContext permCheck

        roleDao.insertRole(role)
        auditDao.insertLog(
            AuditLog(
                user = actor?.username ?: "SYSTEM",
                role = actor?.role?.name ?: "SUPER_ADMIN",
                action = "CUSTOM_ROLE_CREATED",
                recordType = "CUSTOM_ROLE",
                recordId = role.id,
                newValue = "${role.nameHindi} (${role.permissionsCsv})",
                permissionUsed = AppPermissions.ROLE_CREATE,
                status = "SUCCESS"
            )
        )
        Result.success(Unit)
    }

    suspend fun updateCustomRole(role: CustomRole, actor: AppUser?): Result<Unit> = withContext(Dispatchers.IO) {
        val permCheck = checkPermission(actor, AppPermissions.ROLE_EDIT, "कस्टम भूमिका संशोधन", "CUSTOM_ROLE", role.id)
        if (permCheck.isFailure) return@withContext permCheck

        roleDao.updateRole(role)
        auditDao.insertLog(
            AuditLog(
                user = actor?.username ?: "SYSTEM",
                role = actor?.role?.name ?: "SUPER_ADMIN",
                action = "CUSTOM_ROLE_UPDATED",
                recordType = "CUSTOM_ROLE",
                recordId = role.id,
                newValue = "${role.nameHindi} (${role.permissionsCsv})",
                permissionUsed = AppPermissions.ROLE_EDIT,
                status = "SUCCESS"
            )
        )
        Result.success(Unit)
    }

    suspend fun deleteCustomRole(roleId: String, actor: AppUser?): Result<Unit> = withContext(Dispatchers.IO) {
        val permCheck = checkPermission(actor, AppPermissions.ROLE_EDIT, "कस्टम भूमिका निष्कासन", "CUSTOM_ROLE", roleId)
        if (permCheck.isFailure) return@withContext permCheck

        roleDao.deleteRoleById(roleId)
        auditDao.insertLog(
            AuditLog(
                user = actor?.username ?: "SYSTEM",
                role = actor?.role?.name ?: "SUPER_ADMIN",
                action = "CUSTOM_ROLE_DELETED",
                recordType = "CUSTOM_ROLE",
                recordId = roleId,
                permissionUsed = AppPermissions.ROLE_EDIT,
                status = "SUCCESS"
            )
        )
        Result.success(Unit)
    }

    // Role Escalation & User Management
    suspend fun assignUserRoles(
        targetUserId: Long,
        newPrimaryRole: UserRole,
        secondaryRolesCsv: String,
        customPermsCsv: String,
        deniedPermsCsv: String,
        actor: AppUser?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val permCheck = checkPermission(actor, AppPermissions.ROLE_ASSIGN, "भूमिका आवंटन", "USER", targetUserId.toString())
        if (permCheck.isFailure) return@withContext permCheck

        val targetUser = userDao.getUserById(targetUserId)
            ?: return@withContext Result.failure(IllegalArgumentException("उपयोगकर्ता नहीं मिला"))

        // Security Check 1: Cannot demote or change the primary Super Admin
        if (targetUser.email.equals("Deepak53802@gmail.com", ignoreCase = true) ||
            targetUser.username.equals("Deepak53802@gmail.com", ignoreCase = true)) {
            if (newPrimaryRole != UserRole.SUPER_ADMIN) {
                return@withContext Result.failure(SecurityException("मुख्य सुपर एडमिन को डिमोट या संशोधित नहीं किया जा सकता।"))
            }
        }

        // Security Check 2: Only Super Admin can assign or promote anyone to Super Admin
        if (newPrimaryRole == UserRole.SUPER_ADMIN && actor?.role != UserRole.SUPER_ADMIN) {
            return@withContext Result.failure(SecurityException("केवल मुख्य सुपर एडमिन ही सुपर एडमिन भूमिका आवंटित कर सकता है।"))
        }

        // Security Check 3: Normal admin cannot manage or escalate to Super Admin
        if (!RbacAuthorizer.canManageUser(actor, targetUser)) {
            return@withContext Result.failure(SecurityException("आपके पास इस उपयोगकर्ता के अधिकार बदलने की अनुमति नहीं है।"))
        }

        val oldRoleDesc = "${targetUser.role.name} [Sec: ${targetUser.secondaryRolesCsv}]"
        val newRoleDesc = "${newPrimaryRole.name} [Sec: $secondaryRolesCsv]"

        val updatedUser = targetUser.copy(
            role = newPrimaryRole,
            secondaryRolesCsv = secondaryRolesCsv,
            customPermissionsCsv = customPermsCsv,
            deniedPermissionsCsv = deniedPermsCsv
        )
        userDao.updateUser(updatedUser)

        auditDao.insertLog(
            AuditLog(
                user = actor?.username ?: "SYSTEM",
                role = actor?.role?.name ?: "ADMIN",
                action = "ROLE_CHANGED",
                recordType = "USER",
                recordId = targetUserId.toString(),
                oldValue = oldRoleDesc,
                newValue = newRoleDesc,
                permissionUsed = AppPermissions.ROLE_ASSIGN,
                status = "SUCCESS"
            )
        )
        Result.success(Unit)
    }

    suspend fun createStaffUserWithRbac(user: AppUser, actor: AppUser?): Result<Long> = withContext(Dispatchers.IO) {
        val permCheck = checkPermission(actor, AppPermissions.USER_CREATE, "स्टाफ खाता निर्माण", "USER")
        if (permCheck.isFailure) return@withContext Result.failure(permCheck.exceptionOrNull()!!)

        // Security Check: Only Super Admin can create a Super Admin account
        if (user.role == UserRole.SUPER_ADMIN && actor?.role != UserRole.SUPER_ADMIN) {
            return@withContext Result.failure(SecurityException("केवल सुपर एडमिन ही सुपर एडमिन खाता बना सकता है।"))
        }

        val id = userDao.insertUser(user)
        auditDao.insertLog(
            AuditLog(
                user = actor?.username ?: user.createdBy,
                role = actor?.role?.name ?: "ADMIN",
                action = "STAFF_CREATED",
                recordType = "USER",
                recordId = id.toString(),
                newValue = "${user.fullName} (${user.role.name})",
                permissionUsed = AppPermissions.USER_CREATE,
                status = "SUCCESS"
            )
        )
        Result.success(id)
    }

    suspend fun toggleStaffActiveWithRbac(userId: Long, isActive: Boolean, actor: AppUser?): Result<Unit> = withContext(Dispatchers.IO) {
        val permCheck = checkPermission(actor, AppPermissions.USER_DISABLE, "खाता सक्रिय/निष्क्रिय", "USER", userId.toString())
        if (permCheck.isFailure) return@withContext permCheck

        val target = userDao.getUserById(userId)
        if (target != null && (target.email.equals("Deepak53802@gmail.com", ignoreCase = true) ||
                target.username.equals("Deepak53802@gmail.com", ignoreCase = true))) {
            return@withContext Result.failure(SecurityException("मुख्य सुपर एडमिन खाते को निष्क्रिय नहीं किया जा सकता।"))
        }

        userDao.toggleActive(userId, isActive)
        auditDao.insertLog(
            AuditLog(
                user = actor?.username ?: "SYSTEM",
                role = actor?.role?.name ?: "ADMIN",
                action = if (isActive) "STAFF_ACTIVATED" else "STAFF_DEACTIVATED",
                recordType = "USER",
                recordId = userId.toString(),
                permissionUsed = AppPermissions.USER_DISABLE,
                status = "SUCCESS"
            )
        )
        Result.success(Unit)
    }

    suspend fun deleteStaffUserWithRbac(user: AppUser, actor: AppUser?): Result<Boolean> = withContext(Dispatchers.IO) {
        val permCheck = checkPermission(actor, AppPermissions.USER_DELETE, "स्टाफ खाता निष्कासन", "USER", user.id.toString())
        if (permCheck.isFailure) return@withContext Result.failure(permCheck.exceptionOrNull()!!)

        if (user.email.equals("Deepak53802@gmail.com", ignoreCase = true) ||
            user.username.equals("Deepak53802@gmail.com", ignoreCase = true)) {
            return@withContext Result.failure(SecurityException("मुख्य सुपर एडमिन खाते को कभी हटाया नहीं जा सकता।"))
        }

        userDao.deleteUser(user)
        auditDao.insertLog(
            AuditLog(
                user = actor?.username ?: "SYSTEM",
                role = actor?.role?.name ?: "ADMIN",
                action = "STAFF_DELETED",
                recordType = "USER",
                recordId = user.id.toString(),
                newValue = user.username,
                permissionUsed = AppPermissions.USER_DELETE,
                status = "SUCCESS"
            )
        )
        Result.success(true)
    }

    // Critical Action: Clear Audit Logs (SUPER ADMIN ONLY)
    suspend fun clearAuditLogsWithRbac(actor: AppUser?): Result<Unit> = withContext(Dispatchers.IO) {
        val permCheck = checkPermission(actor, AppPermissions.AUDIT_DELETE, "ऑडिट लॉग मिटाना", "AUDIT", "ALL")
        if (permCheck.isFailure) return@withContext permCheck

        auditDao.clearAllLogs()
        auditDao.insertLog(
            AuditLog(
                user = actor?.username ?: "SUPER_ADMIN",
                role = "SUPER_ADMIN",
                action = "AUDIT_LOGS_PURGED",
                recordType = "AUDIT",
                recordId = "ALL",
                permissionUsed = AppPermissions.AUDIT_DELETE,
                status = "SUCCESS"
            )
        )
        Result.success(Unit)
    }

    // Critical Action: Delete Registration
    suspend fun deleteRegistrationWithRbac(regId: String, actor: AppUser?): Result<Unit> = withContext(Dispatchers.IO) {
        val permCheck = checkPermission(actor, AppPermissions.REGISTRATION_DELETE, "पंजीयन रिकॉर्ड डिलीट", "DEVOTEE", regId)
        if (permCheck.isFailure) return@withContext permCheck

        val devotee = devoteeDao.getDevoteeById(regId)
            ?: return@withContext Result.failure(IllegalArgumentException("पंजीयन नहीं मिला"))

        passSeatDao.releaseSeatByDevoteeId(regId)
        devoteeDao.deleteDevotee(regId)

        auditDao.insertLog(
            AuditLog(
                user = actor?.username ?: "SYSTEM",
                role = actor?.role?.name ?: "ADMIN",
                action = "REGISTRATION_DELETED",
                recordType = "DEVOTEE",
                recordId = regId,
                oldValue = "${devotee.fullName} (${devotee.mobileNumber})",
                permissionUsed = AppPermissions.REGISTRATION_DELETE,
                status = "SUCCESS"
            )
        )
        Result.success(Unit)
    }

    // Cancel Registration With Audit
    suspend fun cancelRegistrationWithRbac(regId: String, reason: String, actor: AppUser?): Result<Unit> = withContext(Dispatchers.IO) {
        val permCheck = checkPermission(actor, AppPermissions.REGISTRATION_CANCEL, "पंजीयन रद्दीकरण", "DEVOTEE", regId)
        if (permCheck.isFailure) return@withContext permCheck

        val devotee = devoteeDao.getDevoteeById(regId)
            ?: return@withContext Result.failure(IllegalArgumentException("पंजीयन नहीं मिला"))

        devoteeDao.updateDevoteeStatus(regId, "CANCELLED")
        passSeatDao.releaseSeatByDevoteeId(regId)

        auditDao.insertLog(
            AuditLog(
                user = actor?.username ?: "SYSTEM",
                role = actor?.role?.name ?: "ADMIN",
                action = "REGISTRATION_CANCELLED",
                recordType = "DEVOTEE",
                recordId = regId,
                oldValue = devotee.status,
                newValue = "CANCELLED (कारण: $reason)",
                permissionUsed = AppPermissions.REGISTRATION_CANCEL,
                status = "SUCCESS"
            )
        )
        Result.success(Unit)
    }

    // Cancel Receipt With Audit
    suspend fun cancelReceiptWithRbac(receiptNumber: String, reason: String, actor: AppUser?): Result<Unit> = withContext(Dispatchers.IO) {
        val permCheck = checkPermission(actor, AppPermissions.RECEIPT_CANCEL, "रसीद निरस्तीकरण", "RECEIPT", receiptNumber)
        if (permCheck.isFailure) return@withContext permCheck

        financeDao.updateDonationStatus(receiptNumber, "CANCELLED")
        auditDao.insertLog(
            AuditLog(
                user = actor?.username ?: "SYSTEM",
                role = actor?.role?.name ?: "ADMIN",
                action = "RECEIPT_CANCELLED",
                recordType = "RECEIPT",
                recordId = receiptNumber,
                newValue = "कारण: $reason",
                permissionUsed = AppPermissions.RECEIPT_CANCEL,
                status = "SUCCESS"
            )
        )
        Result.success(Unit)
    }

    // Block Seat With RBAC
    suspend fun blockSeatWithRbac(seatNumber: String, reason: String, actor: AppUser?): Result<Unit> = withContext(Dispatchers.IO) {
        val permCheck = checkPermission(actor, AppPermissions.SEAT_BLOCK, "सीट ब्लॉक / रिजर्व", "SEAT", seatNumber)
        if (permCheck.isFailure) return@withContext permCheck

        val seat = passSeatDao.getSeatByNumber(seatNumber)
            ?: return@withContext Result.failure(IllegalArgumentException("सीट नहीं मिली"))

        if (seat.status == "ASSIGNED" || seat.status == "OCCUPIED") {
            return@withContext Result.failure(IllegalStateException("सीट पहले से आवंटित है। पहले मुक्त करें।"))
        }

        passSeatDao.assignSeat(seatNumber, "BLOCKED", null, null, null)
        auditDao.insertLog(
            AuditLog(
                user = actor?.username ?: "SYSTEM",
                role = actor?.role?.name ?: "SEAT_MANAGER",
                action = "SEAT_BLOCKED",
                recordType = "SEAT",
                recordId = seatNumber,
                newValue = "ब्लॉक कारण: $reason",
                permissionUsed = AppPermissions.SEAT_BLOCK,
                status = "SUCCESS"
            )
        )
        Result.success(Unit)
    }

    // Release Seat With RBAC
    suspend fun releaseSeatWithRbac(seatNumber: String, actor: AppUser?): Result<Unit> = withContext(Dispatchers.IO) {
        val permCheck = checkPermission(actor, AppPermissions.SEAT_RELEASE, "सीट विमुक्ति", "SEAT", seatNumber)
        if (permCheck.isFailure) return@withContext permCheck

        val seat = passSeatDao.getSeatByNumber(seatNumber)
            ?: return@withContext Result.failure(IllegalArgumentException("सीट नहीं मिली"))

        passSeatDao.releaseSeat(seatNumber)
        auditDao.insertLog(
            AuditLog(
                user = actor?.username ?: "SYSTEM",
                role = actor?.role?.name ?: "SEAT_MANAGER",
                action = "SEAT_RELEASED",
                recordType = "SEAT",
                recordId = seatNumber,
                oldValue = "Status: ${seat.status}, Devotee: ${seat.assignedDevoteeId}",
                newValue = "AVAILABLE",
                permissionUsed = AppPermissions.SEAT_RELEASE,
                status = "SUCCESS"
            )
        )
        Result.success(Unit)
    }

    // Check-in Volunteer Scoped Verification: Minimum Required Info
    suspend fun verifyCheckinAttendee(tokenOrId: String, actor: AppUser?): com.example.security.CheckinAttendeeInfo = withContext(Dispatchers.IO) {
        val clean = tokenOrId.trim()
        val pass = passSeatDao.getPassByToken(clean)
            ?: passSeatDao.getPassById(clean)
            ?: passSeatDao.getPassByRegistrationId(clean)

        if (pass == null) {
            return@withContext com.example.security.CheckinAttendeeInfo(
                devoteeName = "",
                registrationId = "",
                passId = clean,
                seatNumber = "",
                passStatus = "NOT_FOUND",
                checkinStatus = "अमान्य पास (INVALID)",
                errorMessage = "पास या क्यूआर कोड सिस्टम में नहीं मिला।"
            )
        }

        val devotee = devoteeDao.getDevoteeById(pass.registrationId)
        val checkin = passSeatDao.getCheckinByPassId(pass.passId)
        val isAlreadyChecked = pass.status == "USED" || checkin != null

        val formattedTime = if (checkin != null) {
            SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault()).format(Date(checkin.checkinTimestamp))
        } else ""

        com.example.security.CheckinAttendeeInfo(
            devoteeName = devotee?.fullName ?: "भक्त",
            registrationId = pass.registrationId,
            passId = pass.passId,
            seatNumber = if (pass.seatNumber.isNotEmpty()) pass.seatNumber else "प्रथम जोन",
            passStatus = pass.status,
            checkinStatus = if (isAlreadyChecked) "ALREADY CHECKED IN" else "सत्यापित (VALID)",
            isAlreadyCheckedIn = isAlreadyChecked,
            checkinTimestamp = checkin?.checkinTimestamp ?: 0L,
            checkinFormattedTime = formattedTime,
            gate = "मुख्य द्वार"
        )
    }

    // Check-in Execution with Double Scan Prevention
    suspend fun performCheckinWithRbac(
        passTokenOrNumber: String,
        gate: String,
        volunteer: AppUser?
    ): Result<CheckinRecord> = withContext(Dispatchers.IO) {
        val permCheck = checkPermission(volunteer, AppPermissions.CHECKIN_CREATE, "प्रवेश चेक-इन", "CHECKIN", passTokenOrNumber)
        if (permCheck.isFailure) return@withContext Result.failure(permCheck.exceptionOrNull()!!)

        val clean = passTokenOrNumber.trim()
        val pass = passSeatDao.getPassByToken(clean)
            ?: passSeatDao.getPassById(clean)
            ?: passSeatDao.getPassByRegistrationId(clean)
            ?: return@withContext Result.failure(IllegalArgumentException("प्रवेश पास नहीं मिला।"))

        if (pass.status == "CANCELLED" || pass.status == "BLOCKED") {
            return@withContext Result.failure(IllegalStateException("यह पास निरस्त/ब्लॉक कर दिया गया है। प्रवेश निषेध है।"))
        }

        val existingCheckin = passSeatDao.getCheckinByPassId(pass.passId)
        if (pass.status == "USED" || existingCheckin != null) {
            val prevTime = if (existingCheckin != null) {
                SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault()).format(Date(existingCheckin.checkinTimestamp))
            } else "पूर्व में"
            return@withContext Result.failure(IllegalStateException("ALREADY CHECKED IN: यह पास पहले ही $prevTime पर चेक-इन हो चुका है!"))
        }

        val devotee = devoteeDao.getDevoteeById(pass.registrationId)
        val record = CheckinRecord(
            passId = pass.passId,
            registrationId = pass.registrationId,
            devoteeName = devotee?.fullName ?: "भक्त",
            seatNumber = pass.seatNumber,
            checkinTimestamp = System.currentTimeMillis(),
            checkedInBy = volunteer?.fullName ?: "स्वयंसेवक",
            status = "CHECKED_IN"
        )
        val checkinId = passSeatDao.insertCheckin(record)
        passSeatDao.updatePassStatus(pass.passId, "USED")

        auditDao.insertLog(
            AuditLog(
                user = volunteer?.username ?: "VOLUNTEER",
                role = volunteer?.role?.name ?: "CHECK_IN_VOLUNTEER",
                action = "CHECKIN_COMPLETED",
                recordType = "CHECKIN",
                recordId = checkinId.toString(),
                newValue = "Devotee: ${devotee?.fullName}, Pass: ${pass.passId}, Gate: $gate",
                permissionUsed = AppPermissions.CHECKIN_CREATE,
                status = "SUCCESS"
            )
        )
        Result.success(record)
    }

    // Manual Seat Assignment by Admin/Seat Manager
    suspend fun assignSeatManually(
        seatNumber: String,
        devoteeId: String,
        devoteeName: String,
        performedBy: String,
        role: String
    ): Boolean = withContext(Dispatchers.IO) {
        val seat = passSeatDao.getSeatByNumber(seatNumber) ?: return@withContext false
        if (seat.status == "ASSIGNED" || seat.status == "OCCUPIED" || seat.status == "BLOCKED") {
            // Already taken unless assigned to same devotee
            if (seat.assignedDevoteeId != devoteeId) return@withContext false
        }

        // Release old seat of this devotee if any
        passSeatDao.releaseSeatByDevoteeId(devoteeId)

        // Assign new seat
        passSeatDao.assignSeat(
            seatNum = seatNumber,
            status = "ASSIGNED",
            devoteeId = devoteeId,
            name = devoteeName,
            grpId = null
        )

        // Update Pass
        val pass = passSeatDao.getPassByRegistrationId(devoteeId)
        if (pass != null) {
            passSeatDao.updatePassSeat(
                passId = pass.passId,
                seatNum = seatNumber,
                sec = seat.section,
                row = seat.row
            )
        }

        auditDao.insertLog(
            AuditLog(
                user = performedBy,
                role = role,
                action = "SEAT_ASSIGNED",
                recordType = "SEAT",
                recordId = seatNumber,
                oldValue = seat.assignedDevoteeName ?: "NONE",
                newValue = "$devoteeName ($devoteeId)"
            )
        )

        true
    }

    suspend fun releaseSeat(seatNumber: String, performedBy: String, role: String) = withContext(Dispatchers.IO) {
        val seat = passSeatDao.getSeatByNumber(seatNumber)
        if (seat != null) {
            val devoteeId = seat.assignedDevoteeId
            passSeatDao.releaseSeat(seatNumber)
            if (devoteeId != null) {
                val pass = passSeatDao.getPassByRegistrationId(devoteeId)
                if (pass != null) {
                    passSeatDao.updatePassSeat(pass.passId, "", "", "")
                }
            }
            auditDao.insertLog(
                AuditLog(
                    user = performedBy,
                    role = role,
                    action = "SEAT_RELEASED",
                    recordType = "SEAT",
                    recordId = seatNumber,
                    oldValue = seat.assignedDevoteeName ?: "OCCUPIED",
                    newValue = "AVAILABLE"
                )
            )
        }
    }

    suspend fun blockSeat(seatNumber: String, performedBy: String, role: String) = withContext(Dispatchers.IO) {
        val seat = passSeatDao.getSeatByNumber(seatNumber) ?: return@withContext
        val newStatus = if (seat.status == "BLOCKED") "AVAILABLE" else "BLOCKED"
        passSeatDao.assignSeat(seatNumber, newStatus, null, null, null)
        auditDao.insertLog(
            AuditLog(
                user = performedBy,
                role = role,
                action = if (newStatus == "BLOCKED") "SEAT_BLOCKED" else "SEAT_UNBLOCKED",
                recordType = "SEAT",
                recordId = seatNumber,
                oldValue = seat.status,
                newValue = newStatus
            )
        )
    }

    // Check-in Scanner Result
    sealed class CheckinResult {
        data class Success(val pass: PassItem, val devotee: Devotee?, val checkinTime: String) : CheckinResult()
        data class AlreadyCheckedIn(val pass: PassItem, val previousTime: String) : CheckinResult()
        data class InvalidOrCancelled(val reason: String) : CheckinResult()
        object NotFound : CheckinResult()
    }

    suspend fun processCheckin(tokenOrId: String, scannedBy: String): CheckinResult = withContext(Dispatchers.IO) {
        val clean = tokenOrId.trim()
        val pass = passSeatDao.getPassById(clean)
            ?: passSeatDao.getPassByToken(clean)
            ?: passSeatDao.getPassByRegistrationId(clean)
            ?: return@withContext CheckinResult.NotFound

        if (pass.status == "CANCELLED") {
            return@withContext CheckinResult.InvalidOrCancelled("यह पास रद्द (Cancelled) कर दिया गया है।")
        }
        if (pass.status == "BLOCKED") {
            return@withContext CheckinResult.InvalidOrCancelled("यह पास प्रतिबंधित (Blocked) है।")
        }

        // Check if already checked in
        val existingCheckin = passSeatDao.getCheckinByPassId(pass.passId)
        if (existingCheckin != null) {
            val timeStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(existingCheckin.checkinTimestamp))
            return@withContext CheckinResult.AlreadyCheckedIn(pass, timeStr)
        }

        val devotee = devoteeDao.getDevoteeById(pass.registrationId)
        val now = System.currentTimeMillis()
        passSeatDao.insertCheckin(
            CheckinRecord(
                passId = pass.passId,
                registrationId = pass.registrationId,
                devoteeName = pass.devoteeName,
                seatNumber = pass.seatNumber,
                checkinTimestamp = now,
                checkedInBy = scannedBy,
                status = "CHECKED_IN"
            )
        )

        passSeatDao.updatePassStatus(pass.passId, "USED")

        auditDao.insertLog(
            AuditLog(
                user = scannedBy,
                role = "VOLUNTEER",
                action = "CHECKIN_SUCCESS",
                recordType = "CHECKIN",
                recordId = pass.passId,
                oldValue = "ACTIVE",
                newValue = "CHECKED_IN"
            )
        )

        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(now))
        CheckinResult.Success(pass, devotee, timeStr)
    }

    // Public verification without leaking sensitive details
    data class PublicPassVerification(
        val isValid: Boolean,
        val devoteeName: String,
        val registrationId: String,
        val passId: String,
        val category: String,
        val seatNumber: String,
        val venue: String,
        val eventDates: String,
        val status: String,
        val message: String
    )

    suspend fun verifyPassPublic(query: String): PublicPassVerification = withContext(Dispatchers.IO) {
        val q = query.trim()
        val pass = passSeatDao.getPassById(q)
            ?: passSeatDao.getPassByToken(q)
            ?: passSeatDao.getPassByRegistrationId(q)

        if (pass == null) {
            return@withContext PublicPassVerification(
                isValid = false,
                devoteeName = "",
                registrationId = "",
                passId = "",
                category = "",
                seatNumber = "",
                venue = "",
                eventDates = "",
                status = "INVALID",
                message = "✕ अमान्य पास (Invalid Pass) — कोई रिकॉर्ड प्राप्त नहीं हुआ।"
            )
        }

        if (pass.status == "CANCELLED" || pass.status == "BLOCKED") {
            return@withContext PublicPassVerification(
                isValid = false,
                devoteeName = pass.devoteeName,
                registrationId = pass.registrationId,
                passId = pass.passId,
                category = pass.category,
                seatNumber = pass.seatNumber,
                venue = pass.venue,
                eventDates = pass.validDate,
                status = pass.status,
                message = "✕ अमान्य / रद्द पास (${pass.status})"
            )
        }

        PublicPassVerification(
            isValid = true,
            devoteeName = pass.devoteeName,
            registrationId = pass.registrationId,
            passId = pass.passId,
            category = pass.category,
            seatNumber = if (pass.seatNumber.isNotBlank()) pass.seatNumber else "सामान्य प्रवेश (No Reserved Seat)",
            venue = pass.venue,
            eventDates = pass.validDate,
            status = pass.status,
            message = "✓ आधिकारिक एवं सत्यापित पास (Official Verified Pass)"
        )
    }

    // Public Receipt Verification
    data class PublicReceiptVerification(
        val isValid: Boolean,
        val receiptNumber: String,
        val donorName: String,
        val amount: Double,
        val date: String,
        val purpose: String,
        val status: String,
        val message: String
    )

    suspend fun verifyReceiptPublic(query: String): PublicReceiptVerification = withContext(Dispatchers.IO) {
        val q = query.trim()
        val receipt = financeDao.getDonationByReceiptNumber(q)
            ?: financeDao.getDonationByToken(q)

        if (receipt == null) {
            return@withContext PublicReceiptVerification(
                isValid = false,
                receiptNumber = "",
                donorName = "",
                amount = 0.0,
                date = "",
                purpose = "",
                status = "INVALID",
                message = "✕ अमान्य रसीद (Invalid Receipt) — आधिकारिक डेटाबेस में उपलब्ध नहीं।"
            )
        }

        if (receipt.status == "CANCELLED") {
            return@withContext PublicReceiptVerification(
                isValid = false,
                receiptNumber = receipt.receiptNumber,
                donorName = receipt.donorName,
                amount = receipt.amount,
                date = receipt.date,
                purpose = receipt.purpose,
                status = "CANCELLED",
                message = "✕ यह रसीद निरस्त (Cancelled) कर दी गई है।"
            )
        }

        PublicReceiptVerification(
            isValid = true,
            receiptNumber = receipt.receiptNumber,
            donorName = receipt.donorName,
            amount = receipt.amount,
            date = receipt.date,
            purpose = receipt.purpose,
            status = "VERIFIED",
            message = "✓ आधिकारिक एवं सत्यापित दान रसीद (Official Verified Receipt)"
        )
    }

    // Record Donation
    suspend fun recordDonation(
        donorName: String,
        mobileNumber: String,
        amount: Double,
        paymentMode: String,
        purpose: String,
        collectorName: String,
        transactionId: String = ""
    ): DonationReceipt = withContext(Dispatchers.IO) {
        val total = financeDao.getAllDonations().first().size + 1
        val receiptNo = "NBF-DON-2027-%06d".format(total)
        val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val token = generateSecureToken("$receiptNo|$donorName|$amount|$dateStr")

        val receipt = DonationReceipt(
            receiptNumber = receiptNo,
            donorName = donorName.trim(),
            mobileNumber = mobileNumber.trim(),
            amount = amount,
            paymentMode = paymentMode,
            purpose = purpose,
            collectorName = collectorName,
            date = dateStr,
            transactionId = transactionId.trim(),
            verificationToken = token,
            status = "VERIFIED"
        )
        financeDao.insertDonation(receipt)

        auditDao.insertLog(
            AuditLog(
                user = collectorName,
                role = "COLLECTOR",
                action = "DONATION_RECEIVED",
                recordType = "DONATION",
                recordId = receiptNo,
                oldValue = "",
                newValue = "₹$amount by $donorName ($paymentMode)"
            )
        )

        receipt
    }

    // Record Expense
    suspend fun recordExpense(
        category: String,
        description: String,
        amount: Double,
        paidTo: String,
        paymentMode: String,
        billRef: String,
        createdBy: String
    ): ExpenseItem = withContext(Dispatchers.IO) {
        val total = financeDao.getAllExpenses().first().size + 1
        val expId = "EXP27-%04d".format(total)
        val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        val expense = ExpenseItem(
            expenseId = expId,
            date = dateStr,
            category = category,
            description = description.trim(),
            amount = amount,
            paidTo = paidTo.trim(),
            paymentMode = paymentMode,
            billReference = billRef.trim(),
            createdBy = createdBy,
            approvalStatus = "APPROVED"
        )
        financeDao.insertExpense(expense)

        auditDao.insertLog(
            AuditLog(
                user = createdBy,
                role = "ADMIN",
                action = "EXPENSE_RECORDED",
                recordType = "EXPENSE",
                recordId = expId,
                oldValue = "",
                newValue = "₹$amount for $description to $paidTo"
            )
        )

        expense
    }

    // CMS Updates
    suspend fun setCmsValue(key: String, value: String, user: String) = withContext(Dispatchers.IO) {
        val old = cmsDao.getCmsValue(key) ?: ""
        cmsDao.setCmsValue(CmsItem(key = key, value = value))
        auditDao.insertLog(
            AuditLog(
                user = user,
                role = "ADMIN",
                action = "CMS_UPDATED",
                recordType = "CMS",
                recordId = key,
                oldValue = old.take(40),
                newValue = value.take(40)
            )
        )
    }

    suspend fun addNotice(notice: NoticeItem, user: String) = withContext(Dispatchers.IO) {
        cmsDao.insertNotice(notice)
        auditDao.insertLog(
            AuditLog(
                user = user,
                role = "ADMIN",
                action = "NOTICE_ADDED",
                recordType = "NOTICE",
                recordId = notice.title,
                oldValue = "",
                newValue = notice.description.take(50)
            )
        )
    }

    suspend fun deleteNotice(notice: NoticeItem, user: String) = withContext(Dispatchers.IO) {
        cmsDao.deleteNotice(notice)
        auditDao.insertLog(
            AuditLog(
                user = user,
                role = "ADMIN",
                action = "NOTICE_DELETED",
                recordType = "NOTICE",
                recordId = notice.title,
                oldValue = notice.description.take(50),
                newValue = "DELETED"
            )
        )
    }

    suspend fun addProgram(program: EventProgram, user: String) = withContext(Dispatchers.IO) {
        eventProgramDao.insertProgram(program)
        auditDao.insertLog(
            AuditLog(
                user = user,
                role = "ADMIN",
                action = "PROGRAM_ADDED",
                recordType = "PROGRAM",
                recordId = program.title,
                oldValue = "",
                newValue = "${program.date} ${program.time}"
            )
        )
    }

    suspend fun deleteProgram(program: EventProgram, user: String) = withContext(Dispatchers.IO) {
        eventProgramDao.deleteProgram(program)
        auditDao.insertLog(
            AuditLog(
                user = user,
                role = "ADMIN",
                action = "PROGRAM_DELETED",
                recordType = "PROGRAM",
                recordId = program.title,
                oldValue = "${program.date} ${program.time}",
                newValue = "DELETED"
            )
        )
    }

    suspend fun updateDevoteeStatus(regId: String, newStatus: String, user: String, role: String) = withContext(Dispatchers.IO) {
        val devotee = devoteeDao.getDevoteeById(regId) ?: return@withContext
        devoteeDao.updateDevoteeStatus(regId, newStatus)
        if (newStatus == "CANCELLED") {
            // Cancel pass and free up seat
            val pass = passSeatDao.getPassByRegistrationId(regId)
            if (pass != null) {
                passSeatDao.updatePassStatus(pass.passId, "CANCELLED")
                if (pass.seatNumber.isNotBlank()) {
                    passSeatDao.releaseSeat(pass.seatNumber)
                }
            }
        }
        auditDao.insertLog(
            AuditLog(
                user = user,
                role = role,
                action = "DEVOTEE_STATUS_CHANGED",
                recordType = "DEVOTEE",
                recordId = regId,
                oldValue = devotee.status,
                newValue = newStatus
            )
        )
    }

    // Re-seed demo data
    suspend fun resetDemoData() = withContext(Dispatchers.IO) {
        seedFullDemoData()
    }

    private suspend fun seedFullDemoData() {
        // 1. Initial CMS texts
        val defaultCms = listOf(
            CmsItem("org_name", "श्री मां नर्मदा भक्त परिवार"),
            CmsItem("event_name", "द्वितीय विशाल श्री मां नर्मदा जन्मोत्सव एवं चुनरी पदयात्रा महोत्सव 2027"),
            CmsItem("event_dates", "11–13 फरवरी 2027"),
            CmsItem("venue_name", "मां नर्मदा पावन तट, मुख्य महाआरती घाट एवं भव्य पंडाल परिसर"),
            CmsItem("hero_tagline", "॥ हर हर नर्मदे — नर्मदे हर ॥"),
            CmsItem("about_event", "मां रेवा के जन्मोत्सव के पावन अवसर पर श्री मां नर्मदा भक्त परिवार द्वारा 11 से 13 फ़रवरी 2027 तक भव्य तीन दिवसीय जन्मोत्सव एवं 255 मीटर की अखंड चुनरी पदयात्रा का महाआयोजन किया जा रहा है।"),
            CmsItem("about_chunri", "नर्मदा तट पर लाखों भक्तों द्वारा मां नर्मदा को श्रद्धापूर्वक 255 मीटर की सुसज्जित चुनरी अर्पित की जाएगी। यह यात्रा भक्तों के सामूहिक संकल्प और भक्ति का अद्वितीय संगम है।"),
            CmsItem("org_center", "तहसील तेंदूखेड़ा, जिला दमोह (म.प्र.)"),
            CmsItem("contact_phone", "7440771076"),
            CmsItem("contact_whatsapp", "7440771076"),
            CmsItem("contact_email", "Deepak53802@gmail.com"),
            CmsItem("contact_address", "माँ सांकला माता मंदिर, तारादेही, तहसील तेंदूखेड़ा, जिला दमोह (म.प्र.)"),
            CmsItem("developer_credit", "Deepak Patel"),
            CmsItem("app_dp_type", "PRESET"),
            CmsItem("app_dp_preset", "CARTOON_MAA_NARMADA"),
            CmsItem("app_dp_url", ""),
            CmsItem("app_dp_title", "द्वितीय विशाल श्री मां नर्मदा जन्मोत्सव 2027"),
            CmsItem("app_dp_subtitle", "पावन चुनरी पदयात्रा एवं महाआरती महोत्सव"),
            CmsItem("app_dp_badge", "🌸 बाल रूप मां नर्मदा"),
            CmsItem("app_dp_updated_at", System.currentTimeMillis().toString()),
            CmsItem("app_dp_updated_by", "Deepak53802@gmail.com")
        )
        cmsDao.insertCmsItems(defaultCms)

        // 2. Seating layout (100 seats: Section A 1-50, Section B 1-50)
        val initialSeats = mutableListOf<SeatItem>()
        // Section A (5 rows: A1 to A5, 10 seats each)
        for (r in 1..5) {
            val rowName = "A$r"
            for (s in 1..10) {
                val seatNum = "$rowName-%02d".format(s)
                initialSeats.add(
                    SeatItem(
                        seatId = "SEAT-$seatNum",
                        venue = "प्रथम जोन",
                        section = "Section A (सम्मुख भाग)",
                        row = rowName,
                        seatNumber = seatNum,
                        status = "AVAILABLE"
                    )
                )
            }
        }
        // Section B (5 rows: B1 to B5, 10 seats each)
        for (r in 1..5) {
            val rowName = "B$r"
            for (s in 1..10) {
                val seatNum = "$rowName-%02d".format(s)
                initialSeats.add(
                    SeatItem(
                        seatId = "SEAT-$seatNum",
                        venue = "प्रथम जोन",
                        section = "Section B (मध्य भाग)",
                        row = rowName,
                        seatNumber = seatNum,
                        status = "AVAILABLE"
                    )
                )
            }
        }
        passSeatDao.insertSeats(initialSeats)

        // 3. Realistic Demo Devotees (20 devotees, 5 families/groups)
        val demoDevotees = listOf(
            Devotee(
                registrationId = "NBF27-000001",
                groupId = "GRP-0001",
                fullName = "दीपक पटेल",
                fatherHusbandName = "सुदामा  पटेल",
                gender = "पुरुष",
                age = 34,
                mobileNumber = "9826012345",
                whatsappNumber = "9826012345",
                address = "वार्ड नं. 5, नर्मदा कॉलोनी",
                village = "नर्मदापुरम",
                post = "नर्मदापुरम",
                gramPanchayat = "नर्मदापुरम",
                tehsil = "नर्मदापुरम",
                district = "नर्मदापुरम",
                state = "मध्य प्रदेश",
                pinCode = "461001",
                emergencyContactName = "राजेश पटेल",
                emergencyContactNumber = "9826012349",
                emergencyRelationship = "भाई",
                isChunriYatraParticipant = true,
                accompanyingPersonsCount = 3,
                groupFamilyName = "पटेल परिवार नर्मदापुरम",
                preferredDate = "11-13 फ़रवरी 2027",
                isTransportRequired = false,
                isFoodRequired = true,
                isAccommodationRequired = false,
                status = "CONFIRMED"
            ),
            Devotee(
                registrationId = "NBF27-000002",
                groupId = "GRP-0002",
                fullName = "शिवनारायण शर्मा",
                fatherHusbandName = "पंडित राधेश्याम शर्मा",
                gender = "पुरुष",
                age = 58,
                mobileNumber = "9425098765",
                whatsappNumber = "9425098765",
                address = "तिलक मार्ग, भेड़ाघाट",
                village = "भेड़ाघाट",
                post = "भेड़ाघाट",
                gramPanchayat = "भेड़ाघाट",
                tehsil = "पाटन",
                district = "जबलपुर",
                state = "मध्य प्रदेश",
                pinCode = "482003",
                emergencyContactName = "सत्यम शर्मा",
                emergencyContactNumber = "9425098766",
                emergencyRelationship = "पुत्र",
                isChunriYatraParticipant = true,
                accompanyingPersonsCount = 2,
                groupFamilyName = "शर्मा परिवार जबलपुर",
                preferredDate = "12 फ़रवरी 2027",
                isTransportRequired = true,
                isFoodRequired = true,
                isAccommodationRequired = true,
                status = "CONFIRMED"
            ),
            Devotee(
                registrationId = "NBF27-000003",
                groupId = "GRP-0003",
                fullName = "श्रीमती उर्मिला बाई यादव",
                fatherHusbandName = "कन्हैयालाल यादव",
                gender = "महिला",
                age = 48,
                mobileNumber = "9893045678",
                whatsappNumber = "9893045678",
                address = "मां नर्मदा पथ, ओंकारेश्वर",
                village = "ओंकारेश्वर",
                post = "मांधाता",
                gramPanchayat = "ओंकारेश्वर",
                tehsil = "पुनासा",
                district = "खंडवा",
                state = "मध्य प्रदेश",
                pinCode = "450523",
                emergencyContactName = "कन्हैयालाल यादव",
                emergencyContactNumber = "9893045679",
                emergencyRelationship = "पति",
                isChunriYatraParticipant = true,
                accompanyingPersonsCount = 4,
                groupFamilyName = "यादव परिवार ओंकारेश्वर",
                preferredDate = "11-13 फ़रवरी 2027",
                isTransportRequired = false,
                isFoodRequired = true,
                isAccommodationRequired = true,
                status = "CONFIRMED"
            ),
            Devotee(
                registrationId = "NBF27-000004",
                groupId = "",
                fullName = "महेंद्र सिंह रघुवंशी",
                fatherHusbandName = "विजय सिंह रघुवंशी",
                gender = "पुरुष",
                age = 42,
                mobileNumber = "9827011223",
                whatsappNumber = "9827011223",
                address = "गांधी चौक, नरसिंहपुर",
                village = "नरसिंहपुर",
                post = "नरसिंहपुर",
                gramPanchayat = "नरसिंहपुर",
                tehsil = "नरसिंहपुर",
                district = "नरसिंहपुर",
                state = "मध्य प्रदेश",
                pinCode = "487001",
                emergencyContactName = "अमित रघुवंशी",
                emergencyContactNumber = "9827011224",
                emergencyRelationship = "मित्र",
                isChunriYatraParticipant = true,
                status = "CONFIRMED"
            ),
            Devotee(
                registrationId = "NBF27-000005",
                groupId = "GRP-0004",
                fullName = "सुरेश कुमार चौकसे",
                fatherHusbandName = "मन्नालाल चौकसे",
                gender = "पुरुष",
                age = 52,
                mobileNumber = "9406522334",
                whatsappNumber = "9406522334",
                address = "नर्मदा रोड, बड़वाह",
                village = "बड़वाह",
                post = "बड़वाह",
                gramPanchayat = "बड़वाह",
                tehsil = "बड़वाह",
                district = "खरगोन",
                state = "मध्य प्रदेश",
                pinCode = "451115",
                emergencyContactName = "अभिषेक चौकसे",
                emergencyContactNumber = "9406522335",
                emergencyRelationship = "पुत्र",
                isChunriYatraParticipant = true,
                accompanyingPersonsCount = 2,
                groupFamilyName = "चौकसे परिवार बड़वाह",
                status = "CONFIRMED"
            ),
            Devotee(
                registrationId = "NBF27-000006",
                groupId = "",
                fullName = "श्रीमती सुनीता देवी वर्मा",
                fatherHusbandName = "प्रेम नारायण वर्मा",
                gender = "महिला",
                age = 39,
                mobileNumber = "9752033445",
                whatsappNumber = "9752033445",
                village = "होशंगाबाद",
                district = "नर्मदापुरम",
                state = "मध्य प्रदेश",
                status = "CONFIRMED"
            ),
            Devotee(
                registrationId = "NBF27-000007",
                groupId = "GRP-0005",
                fullName = "रामस्वरूप पाटीदार",
                fatherHusbandName = "गोपाल पाटीदार",
                gender = "पुरुष",
                age = 45,
                mobileNumber = "9977055667",
                whatsappNumber = "9977055667",
                village = "मंडलेश्वर",
                district = "खरगोन",
                state = "मध्य प्रदेश",
                groupFamilyName = "पाटीदार भक्त मंडल",
                accompanyingPersonsCount = 5,
                status = "CONFIRMED"
            ),
            Devotee(
                registrationId = "NBF27-000008",
                groupId = "",
                fullName = "पंडित घनश्याम तिवारी",
                fatherHusbandName = "काशीनाथ तिवारी",
                gender = "पुरुष",
                age = 65,
                mobileNumber = "9826543211",
                village = "अमरकंटक",
                district = "अनूपपुर",
                state = "मध्य प्रदेश",
                status = "CONFIRMED"
            ),
            Devotee(
                registrationId = "NBF27-000009",
                groupId = "",
                fullName = "कमलेश मालवीय",
                fatherHusbandName = "राजेन्द्र मालवीय",
                gender = "पुरुष",
                age = 29,
                mobileNumber = "9111088990",
                village = "सीहोर",
                district = "सीहोर",
                state = "मध्य प्रदेश",
                status = "CONFIRMED"
            ),
            Devotee(
                registrationId = "NBF27-000010",
                groupId = "",
                fullName = "श्रीमती विमला बाई सोलंकी",
                fatherHusbandName = "दिलीप सोलंकी",
                gender = "महिला",
                age = 50,
                mobileNumber = "9300011223",
                village = "नेमावर",
                district = "देवास",
                state = "मध्य प्रदेश",
                status = "CONFIRMED"
            ),
            Devotee(
                registrationId = "NBF27-000011",
                groupId = "",
                fullName = "हर्षित जोशी",
                fatherHusbandName = "राकेश जोशी",
                gender = "पुरुष",
                age = 24,
                mobileNumber = "9630044556",
                village = "इंदौर",
                district = "इंदौर",
                state = "मध्य प्रदेश",
                status = "CONFIRMED"
            ),
            Devotee(
                registrationId = "NBF27-000012",
                groupId = "",
                fullName = "श्रीमती सरोज देवी दुबे",
                fatherHusbandName = "भगवती प्रसाद दुबे",
                gender = "महिला",
                age = 61,
                mobileNumber = "9826788991",
                village = "गाडरवारा",
                district = "नरसिंहपुर",
                state = "मध्य प्रदेश",
                status = "CONFIRMED"
            ),
            Devotee(
                registrationId = "NBF27-000013",
                groupId = "",
                fullName = "अनिल कुमार जैन",
                fatherHusbandName = "शिखरचंद जैन",
                gender = "पुरुष",
                age = 47,
                mobileNumber = "9425411229",
                village = "भोपाल",
                district = "भोपाल",
                state = "मध्य प्रदेश",
                status = "CONFIRMED"
            ),
            Devotee(
                registrationId = "NBF27-000014",
                groupId = "",
                fullName = "विकास गोस्वामी",
                fatherHusbandName = "कैलाश गिरि",
                gender = "पुरुष",
                age = 31,
                mobileNumber = "9893322110",
                village = "सांडिया",
                district = "नर्मदापुरम",
                state = "मध्य प्रदेश",
                status = "CONFIRMED"
            ),
            Devotee(
                registrationId = "NBF27-000015",
                groupId = "",
                fullName = "भगवान दास कुशवाहा",
                fatherHusbandName = "हरिराम कुशवाहा",
                gender = "पुरुष",
                age = 55,
                mobileNumber = "9713055443",
                village = "बावई",
                district = "नर्मदापुरम",
                state = "मध्य प्रदेश",
                status = "CONFIRMED"
            ),
            Devotee(
                registrationId = "NBF27-000016",
                groupId = "",
                fullName = "श्रीमती रेखा मिश्रा",
                fatherHusbandName = "संजय मिश्रा",
                gender = "महिला",
                age = 37,
                mobileNumber = "9827566778",
                village = "हरदा",
                district = "हरदा",
                state = "मध्य प्रदेश",
                status = "CONFIRMED"
            ),
            Devotee(
                registrationId = "NBF27-000017",
                groupId = "",
                fullName = "मनीष विश्वकर्मा",
                fatherHusbandName = "बाबूलाल विश्वकर्मा",
                gender = "पुरुष",
                age = 33,
                mobileNumber = "9407088992",
                village = "इटारसी",
                district = "नर्मदापुरम",
                state = "मध्य प्रदेश",
                status = "CONFIRMED"
            ),
            Devotee(
                registrationId = "NBF27-000018",
                groupId = "",
                fullName = "ओमप्रकाश राजपूत",
                fatherHusbandName = "महिपाल सिंह",
                gender = "पुरुष",
                age = 49,
                mobileNumber = "9755012398",
                village = "बुधनी",
                district = "सीहोर",
                state = "मध्य प्रदेश",
                status = "CONFIRMED"
            ),
            Devotee(
                registrationId = "NBF27-000019",
                groupId = "",
                fullName = "गोविंद शरण व्यास",
                fatherHusbandName = "नारायण व्यास",
                gender = "पुरुष",
                age = 63,
                mobileNumber = "9826998877",
                village = "उज्जैन",
                district = "उज्जैन",
                state = "मध्य प्रदेश",
                status = "WAITLISTED"
            ),
            Devotee(
                registrationId = "NBF27-000020",
                groupId = "",
                fullName = "श्रीमती ममता तिवारी",
                fatherHusbandName = "मुकेश तिवारी",
                gender = "महिला",
                age = 41,
                mobileNumber = "9981012340",
                village = "सागर",
                district = "सागर",
                state = "मध्य प्रदेश",
                status = "WAITLISTED"
            )
        )
        devoteeDao.insertDevotees(demoDevotees)

        // 4. Group Members
        val demoGroupMembers = listOf(
            GroupMember(groupId = "GRP-0001", individualRegistrationId = "NBF27-000001-M1", memberName = "श्रीमती आरती पटेल", relationship = "पत्नी", age = 31, gender = "महिला"),
            GroupMember(groupId = "GRP-0001", individualRegistrationId = "NBF27-000001-M2", memberName = "आरव पटेल", relationship = "पुत्र", age = 8, gender = "पुरुष"),
            GroupMember(groupId = "GRP-0001", individualRegistrationId = "NBF27-000001-M3", memberName = "श्रीमती कमला बाई पटेल", relationship = "माता", age = 62, gender = "महिला"),
            GroupMember(groupId = "GRP-0002", individualRegistrationId = "NBF27-000002-M1", memberName = "श्रीमती गायत्री शर्मा", relationship = "पत्नी", age = 54, gender = "महिला"),
            GroupMember(groupId = "GRP-0002", individualRegistrationId = "NBF27-000002-M2", memberName = "सत्यम शर्मा", relationship = "पुत्र", age = 26, gender = "पुरुष")
        )
        devoteeDao.insertGroupMembers(demoGroupMembers)

        // 5. Passes and Initial Seat Assignments for confirmed devotees
        val demoPasses = mutableListOf<PassItem>()
        val initialAssignMap = mapOf(
            "NBF27-000001" to "A1-01",
            "NBF27-000002" to "A1-02",
            "NBF27-000003" to "A1-03",
            "NBF27-000004" to "A1-04",
            "NBF27-000005" to "A1-05",
            "NBF27-000006" to "A1-06",
            "NBF27-000007" to "A1-07",
            "NBF27-000008" to "A1-08",
            "NBF27-000009" to "A1-09",
            "NBF27-000010" to "A1-10",
            "NBF27-000011" to "A2-01",
            "NBF27-000012" to "A2-02",
            "NBF27-000013" to "A2-03",
            "NBF27-000014" to "A2-04",
            "NBF27-000015" to "A2-05",
            "NBF27-000016" to "A2-06",
            "NBF27-000017" to "A2-07",
            "NBF27-000018" to "A2-08"
        )

        demoDevotees.take(18).forEachIndexed { index, d ->
            val seatNum = initialAssignMap[d.registrationId] ?: ""
            val passId = "PAS27-%06d".format(index + 1)
            val qr = generateSecureToken("${d.registrationId}|$passId|${d.mobileNumber}")
            val category = when {
                d.groupId.isNotBlank() -> "परिवार पास"
                index < 5 -> "विशेष अतिथि पास"
                else -> "सामान्य भक्त पास"
            }
            demoPasses.add(
                PassItem(
                    passId = passId,
                    registrationId = d.registrationId,
                    devoteeName = d.fullName,
                    category = category,
                    seatNumber = seatNum,
                    section = if (seatNum.startsWith("A1")) "Section A" else "Section A",
                    row = if (seatNum.startsWith("A1")) "A1" else "A2",
                    status = if (index == 0) "USED" else "ACTIVE",
                    qrToken = qr
                )
            )

            // Update seat state in table
            if (seatNum.isNotBlank()) {
                passSeatDao.assignSeat(
                    seatNum = seatNum,
                    status = if (index == 0) "OCCUPIED" else "ASSIGNED",
                    devoteeId = d.registrationId,
                    name = d.fullName,
                    grpId = d.groupId.ifBlank { null }
                )
            }
        }
        passSeatDao.insertPasses(demoPasses)

        // Seed checkin for first devotee
        passSeatDao.insertCheckin(
            CheckinRecord(
                passId = "PAS27-000001",
                registrationId = "NBF27-000001",
                devoteeName = "दीपक पटेल",
                seatNumber = "A1-01",
                checkinTimestamp = System.currentTimeMillis() - 3600000L,
                checkedInBy = "Volunteer Ramesh",
                status = "CHECKED_IN"
            )
        )

        // 6. Programs (11, 12, 13 February 2027)
        val demoPrograms = listOf(
            EventProgram(
                date = "11 फ़रवरी 2027",
                time = "प्रातः 07:30 बजे",
                title = "मां नर्मदा महाभिषेक एवं ध्वजारोहण",
                venue = "मुख्य महाआरती घाट",
                description = "वैदिक ब्राह्मणों द्वारा 108 पावन तीर्थों के जल से मां नर्मदा का दुग्धाभिषेक, पंचामृत स्नान एवं उत्सव ध्वज स्थापना।",
                chiefGuest = "अध्यक्ष श्री मां नर्मदा भक्त परिवार",
                category = "आरती एवं पूजन",
                status = "UPCOMING"
            ),
            EventProgram(
                date = "11 फ़रवरी 2027",
                time = "दोपहर 03:00 बजे",
                title = "भव्य कलश एवं शोभायात्रा",
                venue = "नगर भ्रमण से मुख्य घाट तक",
                description = "सैकड़ों मातृशक्ति द्वारा हाथों में चुनरी धारण कर नगर के प्रमुख मार्गों से भव्य मंगल यात्रा।",
                chiefGuest = "सचिव श्री मां  नर्मदा भक्त परिवार",
                category = "चुनरी यात्रा",
                status = "UPCOMING"
            ),
            EventProgram(
                date = "12 फ़रवरी 2027",
                time = "प्रातः 09:00 बजे",
                title = "255 मीटर अखंड चुनरी महापदयात्रा",
                venue = "सेठानी घाट से मुख्य आयोजन स्थल",
                description = "ऐतिहासिक चुनरी पदयात्रा — भक्तों द्वारा मां नर्मदा को 255 मीटर की अखंड रेशमी चुनरी का समर्पण।",
                chiefGuest = "महामंडलेश्वर स्वामी चिन्मयानंद जी",
                category = "चुनरी यात्रा",
                status = "UPCOMING"
            ),
            EventProgram(
                date = "12 फ़रवरी 2027",
                time = "सायं 06:30 बजे",
                title = "भव्य नर्मदा महाआरती एवं दीपदान",
                venue = "नर्मदा तट, दीप घाट",
                description = "अद्भुत संगीतमय महाआरती, जल में दीपदान एवं अलौकिक आध्यात्मिक अनुभूति।",
                chiefGuest = "समस्त संत मंडल एवं विशिष्ट अतिथि",
                category = "आरती एवं पूजन",
                status = "UPCOMING"
            ),
            EventProgram(
                date = "12 फ़रवरी 2027",
                time = "रात्रि 08:00 बजे",
                title = "अखिल भारतीय भक्ति भजन संध्या",
                venue = "मुख्य सांस्कृतिक मंच, पंडाल",
                description = "सुप्रसिद्ध भजन गायकों द्वारा मां नर्मदा के पावन भजनों की अमृतमयी प्रस्तुति।",
                chiefGuest = "ख्यातिलब्ध भजन गायक",
                category = "सांस्कृतिक संध्या",
                status = "UPCOMING"
            ),
            EventProgram(
                date = "13 फ़रवरी 2027",
                time = "प्रातः 10:00 बजे",
                title = "नर्मदा जन्मोत्सव पूर्णाहुति महायज्ञ एवं संत समागम",
                venue = "यज्ञशाला परिसर",
                description = "विशाल हवन, संतों के आशीर्वचन, चुनरी यात्रा के स्वयंसेवकों का सम्मान।",
                chiefGuest = "वरिष्ठ संतों की पावन उपस्थिति",
                category = "सत्संग एवं प्रवचन",
                status = "UPCOMING"
            ),
            EventProgram(
                date = "13 फ़रवरी 2027",
                time = "दोपहर 12:30 बजे से",
                title = "अखंड महाप्रसाद विशाल भंडारा",
                venue = "भंडारा परिसर",
                description = "समस्त श्रद्धालुओं एवं नगरवासियों के लिए अनवरत मां नर्मदा महाप्रसाद वितरण।",
                chiefGuest = "सर्व भक्तगण",
                category = "महाप्रसाद / भंडारा",
                status = "UPCOMING"
            )
        )
        eventProgramDao.insertPrograms(demoPrograms)

        // 7. Donations (5 Demo Donations)
        val demoDonations = listOf(
            DonationReceipt(
                receiptNumber = "NBF-DON-2027-000001",
                donorName = "दीपक पटेल",
                mobileNumber = "9826012345",
                amount = 21000.0,
                paymentMode = "UPI / QR",
                purpose = "255 मी. चुनरी अर्पण सेवा",
                collectorName = "पंडित मदन मोहन",
                date = "15/01/2027",
                transactionId = "UPI/719283719283",
                verificationToken = generateSecureToken("NBF-DON-2027-000001|दीपक पटेल|21000|15/01/2027"),
                status = "VERIFIED"
            ),
            DonationReceipt(
                receiptNumber = "NBF-DON-2027-000002",
                donorName = "शिवनारायण शर्मा",
                mobileNumber = "9425098765",
                amount = 11000.0,
                paymentMode = "नकद (Cash)",
                purpose = "महाप्रसाद एवं भंडारा",
                collectorName = "राजेश तिवारी",
                date = "18/01/2027",
                transactionId = "",
                verificationToken = generateSecureToken("NBF-DON-2027-000002|शिवनारायण शर्मा|11000|18/01/2027"),
                status = "VERIFIED"
            ),
            DonationReceipt(
                receiptNumber = "NBF-DON-2027-000003",
                donorName = "सुरेश कुमार चौकसे",
                mobileNumber = "9406522334",
                amount = 51000.0,
                paymentMode = "बैंक ट्रांसफर",
                purpose = "मुख्य पंडाल व्यवस्था",
                collectorName = "पंडित मदन मोहन",
                date = "20/01/2027",
                transactionId = "NEFT/SBIN0029381",
                verificationToken = generateSecureToken("NBF-DON-2027-000003|सुरेश कुमार चौकसे|51000|20/01/2027"),
                status = "VERIFIED"
            ),
            DonationReceipt(
                receiptNumber = "NBF-DON-2027-000004",
                donorName = "श्रीमती उर्मिला बाई यादव",
                mobileNumber = "9893045678",
                amount = 5100.0,
                paymentMode = "UPI / QR",
                purpose = "108 दीप महाआरती",
                collectorName = "अमित मालवीय",
                date = "22/01/2027",
                transactionId = "UPI/902837192837",
                verificationToken = generateSecureToken("NBF-DON-2027-000004|श्रीमती उर्मिला बाई यादव|5100|22/01/2027"),
                status = "VERIFIED"
            ),
            DonationReceipt(
                receiptNumber = "NBF-DON-2027-000005",
                donorName = "महेंद्र सिंह रघुवंशी",
                mobileNumber = "9827011223",
                amount = 15000.0,
                paymentMode = "नकद (Cash)",
                purpose = "भक्त विश्राम गृह",
                collectorName = "राजेश तिवारी",
                date = "24/01/2027",
                transactionId = "",
                verificationToken = generateSecureToken("NBF-DON-2027-000005|महेंद्र सिंह रघुवंशी|15000|24/01/2027"),
                status = "VERIFIED"
            )
        )
        financeDao.insertDonations(demoDonations)

        // 8. Expenses (3 Demo Expenses)
        val demoExpenses = listOf(
            ExpenseItem(
                expenseId = "EXP27-0001",
                date = "10/01/2027",
                category = "प्रचार-प्रसार",
                description = "महोत्सव आमंत्रण पत्र, पोस्टर एवं फ्लेक्स प्रिंटिंग",
                amount = 18500.0,
                paidTo = "श्री गणेश प्रिंटर्स नर्मदापुरम",
                paymentMode = "बैंक ट्रांसफर",
                billReference = "INV-7821",
                createdBy = "व्यवस्थापक",
                approvalStatus = "APPROVED"
            ),
            ExpenseItem(
                expenseId = "EXP27-0002",
                date = "15/01/2027",
                category = "मंडप एवं पंडाल",
                description = "मुख्य पंडाल एवं वीआईपी स्टेज वाटरप्रूफ डोम अग्रिम भुगतान",
                amount = 45000.0,
                paidTo = "महाकाल टेंट हाउस",
                paymentMode = "चेक",
                billReference = "CHK-910291",
                createdBy = "व्यवस्थापक",
                approvalStatus = "APPROVED"
            ),
            ExpenseItem(
                expenseId = "EXP27-0003",
                date = "20/01/2027",
                category = "चुनरी निर्माण",
                description = "255 मीटर अखंड रेशमी चुनरी एवं गोटा-किनारी सामग्री",
                amount = 32000.0,
                paidTo = "जय मां टेक्सटाइल्स",
                paymentMode = "UPI",
                billReference = "TX-89102",
                createdBy = "व्यवस्थापक",
                approvalStatus = "APPROVED"
            )
        )
        financeDao.insertExpenses(demoExpenses)

        // 9. Notices
        val demoNotices = listOf(
            NoticeItem(
                title = "255 मीटर चुनरी अर्पण हेतु भक्तों का दिशा-निर्देश",
                description = "12 फ़रवरी को प्रातः 9 बजे से चुनरी यात्रा प्रारंभ होगी। सभी भक्तजन निर्धारित पंक्ति में रहें और अनुशासन बनाए रखें।",
                date = "28 जनवरी 2027",
                priority = "HIGH",
                isActive = true,
                isBanner = true
            ),
            NoticeItem(
                title = "निशुल्क महाप्रसाद एवं जल सेवा काउंटर",
                description = "तीनों दिन मुख्य घाट एवं पंडाल के समीप 24 घंटे शुद्ध पेयजल एवं अल्पाहार की व्यवस्था रहेगी।",
                date = "26 जनवरी 2027",
                priority = "NORMAL",
                isActive = true,
                isBanner = false
            ),
            NoticeItem(
                title = "वाहन पार्किंग एवं विशेष रूट प्लान जारी",
                description = "बाहर से आने वाले वाहनों के लिए आईटीआई मैदान एवं दशहरा मैदान में निःशुल्क सुरक्षित पार्किंग निर्धारित की गई है।",
                date = "25 जनवरी 2027",
                priority = "MEDIUM",
                isActive = true,
                isBanner = false
            )
        )
        cmsDao.insertNotices(demoNotices)

        // 10. FAQs
        val demoFaqs = listOf(
            FaqItem(
                question = "महोत्सव में भक्त पंजीयन कैसे करें?",
                answer = "होम पेज पर 'भक्त पंजीयन करें' बटन पर क्लिक करके 6-चरणीय सरल फॉर्म भरें। पंजीयन पूर्ण होने पर आपको तत्काल डिजिटल पास एवं क्यूआर कोड प्राप्त होगा।",
                category = "पंजीयन",
                displayOrder = 1
            ),
            FaqItem(
                question = "क्या परिवार या समूह के सदस्यों का एक साथ पंजीयन संभव है?",
                answer = "हाँ, 'परिवार / समूह पंजीयन' विकल्प चुनकर आप एक साथ कई सदस्यों को जोड़ सकते हैं। सभी को निकटवर्ती सीटें एवं अलग-अलग क्यूआर पास जारी किए जाएंगे।",
                category = "पंजीयन",
                displayOrder = 2
            ),
            FaqItem(
                question = "सीट आवंटन एवं डिजिटल पास कैसे प्राप्त होगा?",
                answer = "पंजीयन के बाद तुरंत स्क्रीन पर पास दिखाई देगा। आप 'अपना पंजीयन देखें' में मोबाइल नंबर दर्ज करके भी कभी भी पास व सीट नंबर देख सकते हैं और व्हाट्सएप पर शेयर कर सकते हैं।",
                category = "पास व सीट",
                displayOrder = 3
            ),
            FaqItem(
                question = "चुनरी यात्रा का समय एवं मार्ग क्या रहेगा?",
                answer = "12 फ़रवरी 2027 को प्रातः 09:00 बजे सेठानी घाट से प्रारंभ होकर मुख्य नर्मदा जन्मोत्सव पंडाल तट तक 255 मीटर की अखंड चुनरी समर्पित की जाएगी।",
                category = "यात्रा",
                displayOrder = 4
            ),
            FaqItem(
                question = "दान रसीद को कैसे सत्यापित करें?",
                answer = "रसीद पर दिए गए रसीद नंबर अथवा क्यूआर कोड को 'रसीद सत्यापित करें' पृष्ठ पर दर्ज करके इसकी प्रामाणिकता की तत्काल पुष्टि की जा सकती है।",
                category = "दान",
                displayOrder = 5
            )
        )
        cmsDao.insertFaqs(demoFaqs)

        // 11. Initial Audit Log
        auditDao.insertLog(
            AuditLog(
                user = "System",
                role = "SUPER_ADMIN",
                action = "SYSTEM_INITIALIZED",
                recordType = "SYSTEM",
                recordId = "NBF2027",
                oldValue = "",
                newValue = "Initialized with 20 Devotees, 100 Seats, 7 Programs, 5 Donations, 3 Notices"
            )
        )

        // 12. Initial Custom Roles
        val initialCustomRoles = listOf(
            CustomRole(
                id = "ROLE_YATRA_PRABHARI",
                nameHindi = "पदयात्रा व कार्यक्रम प्रभारी",
                description = "चुनरी पदयात्रा व्यवस्था, कार्यक्रम व सूचना प्रबंधन",
                permissionsCsv = "event.view,event.create,event.edit,event.publish,notice.manage,report.view",
                createdBy = "Deepak53802@gmail.com"
            ),
            CustomRole(
                id = "ROLE_VIP_COORDINATOR",
                nameHindi = "अतिथि एवं वीआईपी समन्वयक",
                description = "विशेष अतिथियों हेतु सीट आरक्षण, पास एवं प्रवेश जांच",
                permissionsCsv = "pass.view,pass.create,pass.verify,seat.view,seat.block,seat.assign,report.view",
                createdBy = "Deepak53802@gmail.com"
            )
        )
        for (cr in initialCustomRoles) {
            roleDao.insertRole(cr)
        }

        // 13. Initial Staff and Devotee User Accounts (All 10 Built-in Roles + Multi-Role + Custom Role)
        val initialUsers = listOf(
            AppUser(
                username = "Deepak53802@gmail.com",
                email = "Deepak53802@gmail.com",
                password = "Aditya@123",
                fullName = "दीपक पटेल (Super Admin)",
                mobileNumber = "7440771076",
                role = UserRole.SUPER_ADMIN,
                createdBy = "SYSTEM"
            ),
            AppUser(
                username = "admin@narmada.org",
                email = "admin@narmada.org",
                password = "Admin@2027",
                fullName = "राजेश शर्मा (प्रशासक)",
                mobileNumber = "9826011122",
                role = UserRole.ADMIN,
                createdBy = "Deepak53802@gmail.com"
            ),
            AppUser(
                username = "event@narmada.org",
                email = "event@narmada.org",
                password = "Event@2027",
                fullName = "कैलाश चंद्र दुबे (कार्यक्रम प्रबंधक)",
                mobileNumber = "9826022211",
                role = UserRole.EVENT_MANAGER,
                createdBy = "Deepak53802@gmail.com"
            ),
            AppUser(
                username = "operator1@narmada.org",
                email = "operator1@narmada.org",
                password = "Oper@2027",
                fullName = "सुरेश वर्मा (पंजीयन ऑपरेटर)",
                mobileNumber = "9826022233",
                role = UserRole.REGISTRATION_OPERATOR,
                createdBy = "Deepak53802@gmail.com"
            ),
            AppUser(
                username = "seat@narmada.org",
                email = "seat@narmada.org",
                password = "Seat@2027",
                fullName = "दिलीप चौहान (सीट प्रबंधक)",
                mobileNumber = "9826033311",
                role = UserRole.SEAT_MANAGER,
                createdBy = "Deepak53802@gmail.com"
            ),
            AppUser(
                username = "coll1@narmada.org",
                email = "coll1@narmada.org",
                password = "Dan@2027",
                fullName = "महेश तिवारी (दान संग्रहकर्ता)",
                mobileNumber = "9826044455",
                role = UserRole.COLLECTION_OPERATOR,
                createdBy = "Deepak53802@gmail.com"
            ),
            AppUser(
                username = "vol1@narmada.org",
                email = "vol1@narmada.org",
                password = "Scan@2027",
                fullName = "अमित कुमार (स्कैनर स्वयंसेवक)",
                mobileNumber = "9826033344",
                role = UserRole.CHECK_IN_VOLUNTEER,
                createdBy = "Deepak53802@gmail.com"
            ),
            AppUser(
                username = "cms@narmada.org",
                email = "cms@narmada.org",
                password = "Cms@2027",
                fullName = "पूजा मिश्रा (सामग्री प्रबंधक)",
                mobileNumber = "9826055566",
                role = UserRole.CONTENT_MANAGER,
                createdBy = "Deepak53802@gmail.com"
            ),
            AppUser(
                username = "report@narmada.org",
                email = "report@narmada.org",
                password = "Report@2027",
                fullName = "विकास जैन (रिपोर्ट समीक्षक)",
                mobileNumber = "9826066677",
                role = UserRole.REPORT_VIEWER,
                createdBy = "Deepak53802@gmail.com"
            ),
            AppUser(
                username = "multi@narmada.org",
                email = "multi@narmada.org",
                password = "Multi@2027",
                fullName = "संजय राठौर (कार्यक्रम + सीट समन्वयक)",
                mobileNumber = "9826077788",
                role = UserRole.EVENT_MANAGER,
                secondaryRolesCsv = "SEAT_MANAGER",
                createdBy = "Deepak53802@gmail.com"
            ),
            AppUser(
                username = "prabhari@narmada.org",
                email = "prabhari@narmada.org",
                password = "Yatra@2027",
                fullName = "मनोज शास्त्री (पदयात्रा प्रभारी)",
                mobileNumber = "9826088899",
                role = UserRole.PUBLIC_USER,
                secondaryRolesCsv = "ROLE_YATRA_PRABHARI",
                createdBy = "Deepak53802@gmail.com"
            ),
            AppUser(
                username = "NBF27-000001",
                email = "",
                password = "Narmada@1001",
                fullName = "दीपक पटेल",
                mobileNumber = "7440771076",
                role = UserRole.PUBLIC_USER,
                registrationId = "NBF27-000001",
                createdBy = "AUTO_REGISTRATION"
            ),
            AppUser(
                username = "NBF27-000002",
                email = "",
                password = "Narmada@1002",
                fullName = "शिवनारायण शर्मा",
                mobileNumber = "9425098765",
                role = UserRole.PUBLIC_USER,
                registrationId = "NBF27-000002",
                createdBy = "AUTO_REGISTRATION"
            )
        )
        userDao.insertUsers(initialUsers)
    }
}
