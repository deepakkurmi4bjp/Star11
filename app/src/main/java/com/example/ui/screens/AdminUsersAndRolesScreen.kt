package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppUser
import com.example.data.model.AuditLog
import com.example.data.model.CustomRole
import com.example.data.model.UserRole
import com.example.security.AppPermissions
import com.example.security.PermissionDef
import com.example.security.RbacAuthorizer
import com.example.security.RolePermissions
import com.example.ui.NarmadaViewModel
import com.example.ui.components.NarmadaOutlinedTextField
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class UsersAndRolesSubTab(val titleHindi: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    USERS("उपयोगकर्ता (Users)", Icons.Default.People),
    ROLES("भूमिकाएं (Roles)", Icons.Default.Shield),
    PERMISSIONS("अनुमतियां (Matrix)", Icons.Default.Key),
    AUDIT_LOGS("सुरक्षा ऑडिट (Audit)", Icons.Default.History)
}

/**
 * Production-ready RBAC Users & Roles Admin Screen
 * Fulfills all 24 specifications:
 * - 10 Standard Roles + Custom Roles
 * - Granular Permissions Matrix
 * - Multi-Role Support
 * - Backend Security Enforced
 * - Audit Logging & Confirmations
 */
@Composable
fun AdminUsersAndRolesTab(viewModel: NarmadaViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allUsers by viewModel.repository.allUsers.collectAsStateWithLifecycle(emptyList())
    val customRoles by viewModel.allCustomRoles.collectAsStateWithLifecycle()
    val auditLogs by viewModel.repository.allAuditLogs.collectAsStateWithLifecycle(emptyList())

    val isSuperAdmin = currentUser?.role == UserRole.SUPER_ADMIN ||
            currentUser?.email.equals("Deepak53802@gmail.com", ignoreCase = true) ||
            currentUser?.username.equals("Deepak53802@gmail.com", ignoreCase = true)

    val canViewUsers = isSuperAdmin || viewModel.hasPermission(AppPermissions.USER_VIEW)

    if (!canViewUsers) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SurfaceCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = StatusError,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "आरक्षित: उपयोगकर्ता व सुरक्षा प्रबंधन",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "भूमिका व उपयोगकर्ता प्रबंधन अधिकार केवल सुपर एडमिन व अधिकृत प्रशासक को उपलब्ध है।",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        return
    }

    var selectedSubTab by remember { mutableStateOf(UsersAndRolesSubTab.USERS) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
    ) {
        // Top Sub-Tab Navigation Bar
        Surface(
            color = PureWhite,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                UsersAndRolesSubTab.entries.forEach { subTab ->
                    FilterChip(
                        selected = selectedSubTab == subTab,
                        onClick = { selectedSubTab = subTab },
                        leadingIcon = {
                            Icon(
                                imageVector = subTab.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text(
                                text = subTab.titleHindi,
                                fontSize = 11.sp,
                                fontWeight = if (selectedSubTab == subTab) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SaffronPrimary,
                            selectedLabelColor = PureWhite,
                            selectedLeadingIconColor = PureWhite,
                            containerColor = WarmIvory
                        ),
                        modifier = Modifier.testTag("rbac_subtab_${subTab.name.lowercase()}")
                    )
                }
            }
        }

        // SubTab Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedSubTab) {
                UsersAndRolesSubTab.USERS -> UsersManagementView(
                    viewModel = viewModel,
                    allUsers = allUsers,
                    customRoles = customRoles,
                    isSuperAdmin = isSuperAdmin
                )
                UsersAndRolesSubTab.ROLES -> RolesManagementView(
                    viewModel = viewModel,
                    allUsers = allUsers,
                    customRoles = customRoles,
                    isSuperAdmin = isSuperAdmin
                )
                UsersAndRolesSubTab.PERMISSIONS -> PermissionsMatrixView()
                UsersAndRolesSubTab.AUDIT_LOGS -> AuditLogsView(
                    viewModel = viewModel,
                    auditLogs = auditLogs,
                    isSuperAdmin = isSuperAdmin
                )
            }
        }
    }
}

// ==========================================
// 1. USERS MANAGEMENT VIEW
// ==========================================

@Composable
private fun UsersManagementView(
    viewModel: NarmadaViewModel,
    allUsers: List<AppUser>,
    customRoles: List<CustomRole>,
    isSuperAdmin: Boolean
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var searchQuery by remember { mutableStateOf("") }
    var roleFilter by remember { mutableStateOf<UserRole?>(null) }
    var showCreateUserDialog by remember { mutableStateOf(false) }
    var userToEditRoles by remember { mutableStateOf<AppUser?>(null) }
    var userToResetPassword by remember { mutableStateOf<AppUser?>(null) }
    var userToDelete by remember { mutableStateOf<AppUser?>(null) }

    val filteredUsers = remember(allUsers, searchQuery, roleFilter) {
        allUsers.filter { user ->
            val matchesQuery = searchQuery.isBlank() ||
                    user.fullName.contains(searchQuery, ignoreCase = true) ||
                    user.username.contains(searchQuery, ignoreCase = true) ||
                    user.mobileNumber.contains(searchQuery, ignoreCase = true) ||
                    user.email.contains(searchQuery, ignoreCase = true)
            val matchesRole = roleFilter == null || user.role == roleFilter
            matchesQuery && matchesRole
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header & Quick Stats
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SurfaceCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "उपयोगकर्ता एवं स्टाफ खाते (User Accounts)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaffronDark
                            )
                            Text(
                                text = "कुल ${allUsers.size} खाते | सक्रिय: ${allUsers.count { it.isActive }} | स्टाफ: ${allUsers.count { it.role != UserRole.PUBLIC_USER }}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Button(
                            onClick = { showCreateUserDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("create_user_button")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("नया खाता", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Field
                    NarmadaOutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("नाम / यूज़रनेम / मोबाइल खोजें...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "साफ करें")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Role Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = roleFilter == null,
                                onClick = { roleFilter = null },
                                label = { Text("सभी (${allUsers.size})", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SaffronPrimary,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                        items(UserRole.entries) { role ->
                            val count = allUsers.count { it.role == role }
                            FilterChip(
                                selected = roleFilter == role,
                                onClick = { roleFilter = if (roleFilter == role) null else role },
                                label = { Text("${role.labelHindi.split(" ")[0]} ($count)", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SaffronPrimary,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                    }
                }
            }
        }

        // Users List
        if (filteredUsers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("कोई उपयोगकर्ता नहीं मिला", color = TextSecondary, fontSize = 13.sp)
                }
            }
        } else {
            items(filteredUsers, key = { it.id }) { user ->
                UserCard(
                    user = user,
                    isSuperAdmin = isSuperAdmin,
                    onEditRoles = { userToEditRoles = user },
                    onToggleActive = { viewModel.toggleStaffActiveWithRbac(user) },
                    onResetPassword = { userToResetPassword = user },
                    onDelete = { userToDelete = user },
                    onShareCredentials = {
                        val shareText = "🚩 श्री मां नर्मदा जन्मोत्सव 2027\n\nआपकी अधिकृत लॉगिन क्रेडेंशियल्स:\nनाम: ${user.fullName}\nभूमिका: ${user.role.labelHindi}\nयूज़रनेम: ${user.username}\nपासवर्ड: ${user.password}\n\nकृपया सुरक्षित रखें।"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "क्रेडेंशियल साझा करें"))
                    },
                    onCopyCredentials = {
                        clipboardManager.setText(AnnotatedString("यूज़रनेम: ${user.username} | पासवर्ड: ${user.password}"))
                        Toast.makeText(context, "क्रेडेंशियल कॉपी किए गए", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // Dialogs
    if (showCreateUserDialog) {
        CreateUserWithRbacDialog(
            customRoles = customRoles,
            onDismiss = { showCreateUserDialog = false },
            onConfirm = { fullName, username, email, mobile, primaryRole, secondaryRoles, password ->
                viewModel.createStaffUserWithRbac(
                    fullName = fullName,
                    username = username,
                    email = email,
                    mobile = mobile,
                    primaryRole = primaryRole,
                    secondaryRolesCsv = secondaryRoles,
                    pass = password,
                    onSuccess = { showCreateUserDialog = false },
                    onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                )
            }
        )
    }

    userToEditRoles?.let { user ->
        EditUserRolesDialog(
            user = user,
            customRoles = customRoles,
            isSuperAdmin = isSuperAdmin,
            onDismiss = { userToEditRoles = null },
            onConfirm = { newPrimaryRole, secondaryCsv ->
                viewModel.assignUserRoles(
                    targetUserId = user.id,
                    newPrimaryRole = newPrimaryRole,
                    secondaryRolesCsv = secondaryCsv,
                    onSuccess = { userToEditRoles = null },
                    onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                )
            }
        )
    }

    userToResetPassword?.let { user ->
        ResetPasswordDialog(
            user = user,
            onDismiss = { userToResetPassword = null },
            onConfirm = { newPass ->
                viewModel.resetStaffPassword(user.id, newPass)
                userToResetPassword = null
            }
        )
    }

    userToDelete?.let { user ->
        ConfirmActionDialog(
            title = "स्थायी निष्कासन पुष्टि",
            message = "क्या आप वास्तव में '${user.fullName}' (${user.username}) का खाता हमेशा के लिए हटाना चाहते हैं?",
            confirmButtonText = "हां, खाता हटाएं",
            isDestructive = true,
            onDismiss = { userToDelete = null },
            onConfirm = {
                viewModel.deleteStaffUserWithRbac(user)
                userToDelete = null
            }
        )
    }
}

@Composable
private fun UserCard(
    user: AppUser,
    isSuperAdmin: Boolean,
    onEditRoles: () -> Unit,
    onToggleActive: () -> Unit,
    onResetPassword: () -> Unit,
    onDelete: () -> Unit,
    onShareCredentials: () -> Unit,
    onCopyCredentials: () -> Unit
) {
    val isMainSuperAdmin = user.email.equals("Deepak53802@gmail.com", ignoreCase = true) ||
            user.username.equals("Deepak53802@gmail.com", ignoreCase = true)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (user.isActive) PureWhite else PureWhite.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (isMainSuperAdmin) SacredGold else if (!user.isActive) StatusError.copy(alpha = 0.4f) else SurfaceCardBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top Row: Avatar + Name + Badges + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(user.role.badgeBgColor)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.fullName.take(1).ifBlank { "U" },
                        color = Color(user.role.badgeTextColor),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.fullName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (isMainSuperAdmin) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "👑 मुख्य", fontSize = 10.sp, color = SaffronDark, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "यूज़रनेम: ${user.username}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                // Status chip
                Surface(
                    color = if (user.isActive) StatusSuccess.copy(alpha = 0.15f) else StatusError.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (user.isActive) "सक्रिय" else "निष्क्रिय",
                        color = if (user.isActive) StatusSuccess else StatusError,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Role Badges (Multi-role support!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primary Role Badge
                RoleBadge(role = user.role)

                // Secondary Roles Badges
                val secondaries = user.secondaryRolesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                secondaries.forEach { sec ->
                    Surface(
                        color = SaffronContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, SaffronPrimary.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "+ $sec",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SaffronDark,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Contact info
            if (user.mobileNumber.isNotBlank() || user.email.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (user.mobileNumber.isNotBlank()) {
                        Text(
                            text = "📞 ${user.mobileNumber}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    if (user.email.isNotBlank()) {
                        Text(
                            text = "✉️ ${user.email}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = SurfaceCardBorder, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onShareCredentials, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "शेयर करें", tint = SaffronPrimary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onCopyCredentials, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "कॉपी करें", tint = NarmadaBlueDark, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onResetPassword, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Password, contentDescription = "पासवर्ड बदलें", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Edit Roles button
                    OutlinedButton(
                        onClick = onEditRoles,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.ManageAccounts, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("भूमिका बदलें", fontSize = 11.sp)
                    }

                    if (!isMainSuperAdmin) {
                        // Toggle Active/Inactive
                        Switch(
                            checked = user.isActive,
                            onCheckedChange = { onToggleActive() },
                            modifier = Modifier.height(24.dp)
                        )

                        // Delete button
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "हटाएं", tint = StatusError, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. ROLES MANAGEMENT VIEW
// ==========================================

@Composable
private fun RolesManagementView(
    viewModel: NarmadaViewModel,
    allUsers: List<AppUser>,
    customRoles: List<CustomRole>,
    isSuperAdmin: Boolean
) {
    var showCreateCustomRoleDialog by remember { mutableStateOf(false) }
    var selectedRoleToViewPermissions by remember { mutableStateOf<Pair<String, Set<String>>?>(null) }
    var roleToDelete by remember { mutableStateOf<CustomRole?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header Card with Create Custom Role Action
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SurfaceCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "भूमिका प्रबंधन (Role Hierarchy & Custom Roles)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaffronDark
                            )
                            Text(
                                text = "10 मानक भूमिकाएं + ${customRoles.size} कस्टम भूमिकाएं",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Button(
                            onClick = { showCreateCustomRoleDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("create_custom_role_button")
                        ) {
                            Icon(Icons.Default.AddModerator, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("नई कस्टम भूमिका", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: 10 Standard Built-in Roles
        item {
            Text(
                text = "मानक पदानुक्रम भूमिकाएं (Built-in Standard Roles):",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }

        items(UserRole.entries) { role ->
            val userCount = allUsers.count { it.role == role || it.secondaryRolesCsv.contains(role.name) }
            val permissions = RolePermissions.getDefaultPermissionsForRole(role)
            BuiltInRoleCard(
                role = role,
                userCount = userCount,
                permissionCount = permissions.size,
                onViewPermissions = { selectedRoleToViewPermissions = Pair(role.labelHindi, permissions) }
            )
        }

        // Section: Custom Roles
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "कस्टम भूमिकाएं (Custom Roles by Super Admin):",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }

        if (customRoles.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, SurfaceCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("कोई कस्टम भूमिका नहीं बनाई गई है। 'नई कस्टम भूमिका' पर क्लिक करें।", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        } else {
            items(customRoles, key = { it.id }) { cr ->
                val userCount = allUsers.count { it.secondaryRolesCsv.contains(cr.id) }
                val permissions = cr.permissionsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
                CustomRoleCard(
                    role = cr,
                    userCount = userCount,
                    permissionCount = permissions.size,
                    onViewPermissions = { selectedRoleToViewPermissions = Pair(cr.nameHindi, permissions) },
                    onDelete = { roleToDelete = cr }
                )
            }
        }
    }

    // View Permissions Dialog
    selectedRoleToViewPermissions?.let { (title, perms) ->
        AlertDialog(
            onDismissRequest = { selectedRoleToViewPermissions = null },
            title = {
                Text(
                    text = "$title की अनुमतियां (${perms.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaffronDark
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(perms.toList()) { permId ->
                        val def = AppPermissions.DEFINITION_MAP[permId]
                        Card(
                            colors = CardDefaults.cardColors(containerColor = WarmIvory),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = def?.titleHindi ?: permId,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = def?.categoryHindi?.split(" ")?.first() ?: "",
                                        fontSize = 9.sp,
                                        color = SaffronPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = def?.descriptionHindi ?: permId,
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedRoleToViewPermissions = null },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("बंद करें")
                }
            }
        )
    }

    // Create Custom Role Dialog
    if (showCreateCustomRoleDialog) {
        CreateCustomRoleDialog(
            onDismiss = { showCreateCustomRoleDialog = false },
            onConfirm = { nameHindi, desc, perms ->
                viewModel.createCustomRole(
                    nameHindi = nameHindi,
                    description = desc,
                    permissions = perms,
                    onSuccess = { showCreateCustomRoleDialog = false }
                )
            }
        )
    }

    // Delete Custom Role Confirmation Dialog
    roleToDelete?.let { cr ->
        ConfirmActionDialog(
            title = "कस्टम भूमिका निष्कासन पुष्टि",
            message = "क्या आप वास्तव में कस्टम भूमिका '${cr.nameHindi}' को हटाना चाहते हैं?",
            confirmButtonText = "हां, हटाएं",
            isDestructive = true,
            onDismiss = { roleToDelete = null },
            onConfirm = {
                viewModel.deleteCustomRole(cr.id)
                roleToDelete = null
            }
        )
    }
}

@Composable
private fun BuiltInRoleCard(
    role: UserRole,
    userCount: Int,
    permissionCount: Int,
    onViewPermissions: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, SurfaceCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                RoleBadge(role = role)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "आवंटित उपयोगकर्ता: $userCount | कुल अनुमतियां: $permissionCount",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            OutlinedButton(
                onClick = onViewPermissions,
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text("अनुमतियां देखें", fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun CustomRoleCard(
    role: CustomRole,
    userCount: Int,
    permissionCount: Int,
    onViewPermissions: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF6A1B9A),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "कस्टम भूमिका",
                            color = PureWhite,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = role.nameHindi,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }

                if (role.description.isNotBlank()) {
                    Text(
                        text = role.description,
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }

                Text(
                    text = "उपयोगकर्ता: $userCount | अनुमतियां: $permissionCount",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    onClick = onViewPermissions,
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("अनुमतियां", fontSize = 10.sp)
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "हटाएं", tint = StatusError, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ==========================================
// 3. PERMISSIONS MATRIX VIEW
// ==========================================

@Composable
private fun PermissionsMatrixView() {
    val categories = AppPermissions.CATEGORIES
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val filteredList = remember(selectedCategory) {
        if (selectedCategory == null) AppPermissions.ALL_DEFINITIONS
        else AppPermissions.ALL_DEFINITIONS.filter { it.categoryHindi == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SurfaceCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "सिस्टम अनुमतियां (Permissions Matrix)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDark
                    )
                    Text(
                        text = "कुल ${AppPermissions.ALL_DEFINITIONS.size} परिभाषित अनुमतियां | 11 श्रेणियां",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("सभी", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SaffronPrimary,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                                label = { Text(cat.split(" ")[0], fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SaffronPrimary,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                    }
                }
            }
        }

        items(filteredList, key = { it.id }) { perm ->
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.5.dp, SurfaceCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = perm.titleHindi,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (perm.isCritical) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = StatusError.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "⚠️ संवेदनशील",
                                        color = StatusError,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = perm.descriptionHindi,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Text(
                            text = "कोड: ${perm.id}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SaffronDark
                        )
                    }

                    Surface(
                        color = SaffronContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = perm.categoryHindi.split(" ").first(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. AUDIT LOGS VIEW
// ==========================================

@Composable
private fun AuditLogsView(
    viewModel: NarmadaViewModel,
    auditLogs: List<AuditLog>,
    isSuperAdmin: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val filteredLogs = remember(auditLogs, searchQuery) {
        if (searchQuery.isBlank()) auditLogs
        else {
            auditLogs.filter { log ->
                log.user.contains(searchQuery, ignoreCase = true) ||
                        log.action.contains(searchQuery, ignoreCase = true) ||
                        log.recordId.contains(searchQuery, ignoreCase = true) ||
                        log.role.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SurfaceCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "सुरक्षा एवं भूमिका ऑडिट लॉग्स (Security Audit)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaffronDark
                            )
                            Text(
                                text = "कुल ${auditLogs.size} प्रविष्टियां दर्ज",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        if (isSuperAdmin) {
                            OutlinedButton(
                                onClick = { showClearConfirmDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("लॉग साफ करें", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    NarmadaOutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("उपयोगकर्ता / एक्शन / आईडी खोजें...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        if (filteredLogs.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("कोई ऑडिट लॉग प्राप्त नहीं हुआ", fontSize = 13.sp, color = TextSecondary)
                }
            }
        } else {
            items(filteredLogs, key = { it.id }) { log ->
                AuditLogCard(log = log)
            }
        }
    }

    if (showClearConfirmDialog) {
        ConfirmActionDialog(
            title = "ऑडिट लॉग मिटाने की पुष्टि (सुपर एडमिन)",
            message = "क्या आप वास्तव में सभी सुरक्षा व गतिविधि ऑडिट लॉग्स मिटाना चाहते हैं? यह क्रिया अपरिवर्तनीय है।",
            confirmButtonText = "हां, सभी लॉग मिटाएं",
            isDestructive = true,
            onDismiss = { showClearConfirmDialog = false },
            onConfirm = {
                viewModel.clearAuditLogs()
                showClearConfirmDialog = false
            }
        )
    }
}

@Composable
private fun AuditLogCard(log: AuditLog) {
    val dateStr = remember(log.timestamp) {
        SimpleDateFormat("dd-MM-yyyy hh:mm:ss a", Locale.getDefault()).format(Date(log.timestamp))
    }
    val isDenied = log.status == "DENIED" || log.action.endsWith("_DENIED")

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDenied) StatusError.copy(alpha = 0.05f) else PureWhite
        ),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            1.dp,
            if (isDenied) StatusError.copy(alpha = 0.5f) else SurfaceCardBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isDenied) Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isDenied) StatusError else StatusSuccess,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = log.action,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isDenied) StatusError else TextPrimary
                    )
                }

                Surface(
                    color = if (isDenied) StatusError.copy(alpha = 0.15f) else StatusSuccess.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (isDenied) "अस्वीकृत (DENIED)" else "सफल (SUCCESS)",
                        color = if (isDenied) StatusError else StatusSuccess,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "उपयोगकर्ता: ${log.user} | भूमिका: ${log.role}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = SaffronDark
            )

            if (log.permissionUsed.isNotBlank()) {
                Text(
                    text = "प्रयुक्त अनुमति: ${log.permissionUsed}",
                    fontSize = 10.sp,
                    color = NarmadaBlueDark
                )
            }

            if (log.recordId.isNotBlank()) {
                Text(
                    text = "टारगेट रिकॉर्ड: [${log.recordType}] ID: ${log.recordId}",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }

            if (log.oldValue.isNotBlank() || log.newValue.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                if (log.oldValue.isNotBlank()) {
                    Text(text = "पुराना मान: ${log.oldValue}", fontSize = 10.sp, color = TextSecondary)
                }
                if (log.newValue.isNotBlank()) {
                    Text(text = "नया मान: ${log.newValue}", fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "दिनांक व समय: $dateStr",
                fontSize = 9.sp,
                color = TextSecondary
            )
        }
    }
}

// ==========================================
// ROLE BADGE COMPONENT (10 Accessible Styles)
// ==========================================

@Composable
fun RoleBadge(role: UserRole, modifier: Modifier = Modifier) {
    Surface(
        color = Color(role.badgeBgColor),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Text(
            text = role.labelHindi.split(" ").take(2).joinToString(" "),
            color = Color(role.badgeTextColor),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// ==========================================
// DIALOGS: CREATE USER, EDIT ROLES, CREATE ROLE
// ==========================================

@Composable
private fun CreateUserWithRbacDialog(
    customRoles: List<CustomRole>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, UserRole, String, String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var selectedPrimaryRole by remember { mutableStateOf(UserRole.REGISTRATION_OPERATOR) }
    var selectedSecondaryRoles by remember { mutableStateOf(setOf<String>()) }
    var password by remember { mutableStateOf("Narmada@${(1000..9999).random()}") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("नया उपयोगकर्ता / स्टाफ खाता बनाएं", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SaffronDark)
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("प्राथमिक भूमिका (Primary Role):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SaffronDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(UserRole.entries.filter { it != UserRole.SUPER_ADMIN }) { role ->
                            FilterChip(
                                selected = selectedPrimaryRole == role,
                                onClick = { selectedPrimaryRole = role },
                                label = { Text(role.labelHindi.split(" ")[0], fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SaffronPrimary,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                    }
                }

                // Secondary roles selection
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("अतिरिक्त भूमिकाएं (Multi-Role Support):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SaffronDark)
                    val availableSecondaries = UserRole.entries.filter { it != UserRole.SUPER_ADMIN && it != selectedPrimaryRole }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(availableSecondaries) { secRole ->
                            val isSelected = selectedSecondaryRoles.contains(secRole.name)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedSecondaryRoles = if (isSelected) selectedSecondaryRoles - secRole.name else selectedSecondaryRoles + secRole.name
                                },
                                label = { Text("+ ${secRole.labelHindi.split(" ")[0]}", fontSize = 9.sp) }
                            )
                        }
                    }
                }

                // Custom roles if any
                if (customRoles.isNotEmpty()) {
                    item {
                        Text("कस्टम भूमिकाएं जोड़ें:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SaffronDark)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(customRoles) { cr ->
                                val isSelected = selectedSecondaryRoles.contains(cr.id)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedSecondaryRoles = if (isSelected) selectedSecondaryRoles - cr.id else selectedSecondaryRoles + cr.id
                                    },
                                    label = { Text("+ ${cr.nameHindi}", fontSize = 9.sp) }
                                )
                            }
                        }
                    }
                }

                item {
                    NarmadaOutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it; errorMsg = null },
                        label = { Text("पूरा नाम (Full Name)") },
                        placeholder = { Text("जैसे कैलाश चंद्र") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    NarmadaOutlinedTextField(
                        value = username,
                        onValueChange = { username = it; errorMsg = null },
                        label = { Text("यूज़रनेम / लॉगिन ID") },
                        placeholder = { Text("जैसे event_kailash") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    NarmadaOutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = { Text("मोबाइल नंबर (WhatsApp)") },
                        placeholder = { Text("10 अंकों का नंबर") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    NarmadaOutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("ईमेल पता (वैकल्पिक)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NarmadaOutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("पासवर्ड") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(onClick = { password = "Narmada@${(1000..9999).random()}" }) {
                            Icon(Icons.Default.Refresh, contentDescription = "नया पासवर्ड", tint = SaffronPrimary)
                        }
                    }
                }

                if (errorMsg != null) {
                    item {
                        Text(text = errorMsg!!, color = StatusError, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isBlank() || username.isBlank() || password.isBlank()) {
                        errorMsg = "कृपया नाम, यूज़रनेम एवं पासवर्ड दर्ज करें।"
                        return@Button
                    }
                    val secCsv = selectedSecondaryRoles.joinToString(",")
                    onConfirm(fullName, username, email, mobile, selectedPrimaryRole, secCsv, password)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Text("खाता सुरक्षित करें")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("रद्द करें") }
        }
    )
}

@Composable
private fun EditUserRolesDialog(
    user: AppUser,
    customRoles: List<CustomRole>,
    isSuperAdmin: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (UserRole, String) -> Unit
) {
    var primaryRole by remember { mutableStateOf(user.role) }
    var secondaryRoles by remember {
        mutableStateOf(user.secondaryRolesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet())
    }

    val isMainSuperAdmin = user.email.equals("Deepak53802@gmail.com", ignoreCase = true) ||
            user.username.equals("Deepak53802@gmail.com", ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${user.fullName} की भूमिकाएं बदलें",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isMainSuperAdmin) {
                    item {
                        Text(
                            text = "सुरक्षा चेतावनी: मुख्य सुपर एडमिन (Deepak53802@gmail.com) की प्राथमिक भूमिका परिवर्तित नहीं की जा सकती।",
                            color = StatusError,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    item {
                        Text("प्राथमिक भूमिका चुनें:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SaffronDark)
                        val selectableRoles = if (isSuperAdmin) UserRole.entries else UserRole.entries.filter { it != UserRole.SUPER_ADMIN }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(selectableRoles) { r ->
                                FilterChip(
                                    selected = primaryRole == r,
                                    onClick = { primaryRole = r },
                                    label = { Text(r.labelHindi.split(" ")[0], fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SaffronPrimary,
                                        selectedLabelColor = PureWhite
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("अतिरिक्त भूमिकाएं (Multi-Roles):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SaffronDark)
                    val available = UserRole.entries.filter { it != UserRole.SUPER_ADMIN && it != primaryRole }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(available) { r ->
                            val isSel = secondaryRoles.contains(r.name)
                            FilterChip(
                                selected = isSel,
                                onClick = {
                                    secondaryRoles = if (isSel) secondaryRoles - r.name else secondaryRoles + r.name
                                },
                                label = { Text("+ ${r.labelHindi.split(" ")[0]}", fontSize = 9.sp) }
                            )
                        }
                    }
                }

                if (customRoles.isNotEmpty()) {
                    item {
                        Text("कस्टम भूमिकाएं:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SaffronDark)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(customRoles) { cr ->
                                val isSel = secondaryRoles.contains(cr.id)
                                FilterChip(
                                    selected = isSel,
                                    onClick = {
                                        secondaryRoles = if (isSel) secondaryRoles - cr.id else secondaryRoles + cr.id
                                    },
                                    label = { Text("+ ${cr.nameHindi}", fontSize = 9.sp) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(primaryRole, secondaryRoles.joinToString(","))
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Text("भूमिका अपडेट करें")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("रद्द करें") }
        }
    )
}

@Composable
private fun CreateCustomRoleDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, List<String>) -> Unit
) {
    var nameHindi by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val selectedPermissions = remember { mutableStateMapOf<String, Boolean>() }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "नई कस्टम भूमिका बनाएं (Custom Role)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    NarmadaOutlinedTextField(
                        value = nameHindi,
                        onValueChange = { nameHindi = it; errorMsg = null },
                        label = { Text("भूमिका का नाम (जैसे: कार्यक्रम प्रभारी)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    NarmadaOutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("संक्षिप्त विवरण (Description)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "अनुमतियां चुनें (Select Permissions):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDark
                    )
                }

                // Group permissions by category
                AppPermissions.CATEGORIES.forEach { category ->
                    val permsInCategory = AppPermissions.ALL_DEFINITIONS.filter { it.categoryHindi == category }
                    item {
                        Surface(
                            color = WarmIvory,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "📂 $category",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = SaffronDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    items(permsInCategory) { def ->
                        val isChecked = selectedPermissions[def.id] == true
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPermissions[def.id] = !isChecked }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { selectedPermissions[def.id] = it },
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(text = def.titleHindi, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = def.descriptionHindi, fontSize = 9.sp, color = TextSecondary)
                            }
                        }
                    }
                }

                if (errorMsg != null) {
                    item {
                        Text(text = errorMsg!!, color = StatusError, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameHindi.isBlank()) {
                        errorMsg = "कृपया भूमिका का नाम दर्ज करें।"
                        return@Button
                    }
                    val permsList = selectedPermissions.filter { it.value }.keys.toList()
                    if (permsList.isEmpty()) {
                        errorMsg = "कृपया कम से कम एक अनुमति चुनें।"
                        return@Button
                    }
                    onConfirm(nameHindi, description, permsList)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Text("भूमिका बनाएं")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("रद्द करें") }
        }
    )
}

@Composable
private fun ResetPasswordDialog(
    user: AppUser,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newPassword by remember { mutableStateOf("Narmada@${(1000..9999).random()}") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("${user.fullName} का पासवर्ड बदलें", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "यूज़रनेम: ${user.username}", fontSize = 12.sp, color = TextSecondary)
                NarmadaOutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("नया पासवर्ड") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newPassword) },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Text("पासवर्ड बदलें")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("रद्द करें") }
        }
    )
}

@Composable
private fun ConfirmActionDialog(
    title: String,
    message: String,
    confirmButtonText: String,
    isDestructive: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (isDestructive) StatusError else TextPrimary) },
        text = { Text(text = message, fontSize = 12.sp, color = TextSecondary) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) StatusError else SaffronPrimary
                )
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("रद्द करें") }
        }
    )
}
