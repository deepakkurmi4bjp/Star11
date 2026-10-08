package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.AppLogoConfig
import com.example.data.model.AppUser
import com.example.data.model.UserRole
import com.example.ui.AppScreen
import com.example.ui.NarmadaViewModel
import com.example.ui.components.DirectPhotoUploaderSection
import com.example.ui.components.NarmadaOutlinedTextField
import com.example.ui.components.SACRED_DP_PRESETS
import com.example.ui.components.SacredDpAvatar
import com.example.ui.theme.*

/**
 * Super Admin Staff & Credentials Management Tab.
 * Super Admin can create Admin, Operator, Volunteer, Collector, and view/manage accounts.
 */
@Composable
fun AdminStaffManagementTab(viewModel: NarmadaViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allUsers by viewModel.repository.allUsers.collectAsStateWithLifecycle(emptyList())

    val isSuperAdmin = currentRole == UserRole.SUPER_ADMIN ||
            currentUser?.email.equals("Deepak53802@gmail.com", ignoreCase = true) ||
            currentUser?.username.equals("Deepak53802@gmail.com", ignoreCase = true)

    var showCreateDialog by remember { mutableStateOf(false) }
    var userToResetPassword by remember { mutableStateOf<AppUser?>(null) }
    var userToDelete by remember { mutableStateOf<AppUser?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    if (!isSuperAdmin) {
        // Access Denied / Lock view
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
                        text = "आरक्षित: केवल मुख्य सुपर एडमिन",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "स्टाफ व अन्य एडमिन के क्रेडेंशियल बनाने एवं नियंत्रित करने का अधिकार केवल मुख्य सुपर एडमिन को है।",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = SaffronContainer.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = SaffronDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "सुरक्षा नीति: गोपनीय क्रेडेंशियल केवल अधिकृत मुख्य सुपर एडमिन के पास सुरक्षित हैं।",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = SaffronDark,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("मुख्य पृष्ठ पर जाएं")
                    }
                }
            }
        }
        return
    }

    // Filter staff users (excluding pure public devotees if desired, or showing staff first)
    val filteredUsers = remember(allUsers, searchQuery) {
        allUsers.filter { user ->
            if (searchQuery.isBlank()) true
            else {
                user.fullName.contains(searchQuery, ignoreCase = true) ||
                        user.username.contains(searchQuery, ignoreCase = true) ||
                        user.mobileNumber.contains(searchQuery) ||
                        user.role.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val staffCount = allUsers.count { it.role != UserRole.PUBLIC }
    val adminCount = allUsers.count { it.role == UserRole.ADMIN }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "स्टाफ व क्रेडेंशियल्स नियंत्रण",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDark
                    )
                    Text(
                        text = "सुपर एडमिन द्वारा नए एडमिन, ऑपरेटर व स्टाफ का निर्माण",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("नया क्रेडेंशियल", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, SurfaceCardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("कुल स्टाफ", fontSize = 10.sp, color = TextSecondary)
                        Text("$staffCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SaffronPrimary)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, SurfaceCardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("सक्रिय एडमिन", fontSize = 10.sp, color = TextSecondary)
                        Text("$adminCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NarmadaBlue)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, SurfaceCardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("मुख्य केंद्र", fontSize = 10.sp, color = TextSecondary)
                        Text("तेंदूखेड़ा", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SaffronDark)
                    }
                }
            }
        }

        // Search bar
        item {
            NarmadaOutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("स्टाफ खोजें (नाम / यूज़रनेम / मोबाइल)") },
                placeholder = { Text("खोज शब्द दर्ज करें...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SaffronPrimary) }
            )
        }

        // Users List
        items(filteredUsers) { user ->
            val isMasterSuperAdmin = user.email.equals("Deepak53802@gmail.com", ignoreCase = true) ||
                    user.username.equals("Deepak53802@gmail.com", ignoreCase = true)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMasterSuperAdmin) SaffronContainer.copy(alpha = 0.25f) else PureWhite
                ),
                border = BorderStroke(
                    1.dp,
                    if (isMasterSuperAdmin) SaffronPrimary else SurfaceCardBorder
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isMasterSuperAdmin) Icons.Default.Stars else Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = if (isMasterSuperAdmin) SacredGold else SaffronPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = user.fullName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isMasterSuperAdmin) SaffronDark else TextPrimary
                                )
                                Text(
                                    text = "यूज़रनेम: ${user.username}",
                                    fontSize = 11.sp,
                                    color = NarmadaBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Surface(
                            color = when (user.role) {
                                UserRole.SUPER_ADMIN -> SacredGold
                                UserRole.ADMIN -> SaffronContainer
                                UserRole.OPERATOR -> NarmadaBlueLight.copy(alpha = 0.3f)
                                else -> StatusSuccessBg
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = user.role.labelHindi.split(" ")[0],
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (user.mobileNumber.isNotBlank()) {
                            Text(text = "मो: ${user.mobileNumber}", fontSize = 11.sp, color = TextSecondary)
                        }
                        if (user.email.isNotBlank()) {
                            Text(text = user.email, fontSize = 11.sp, color = TextMuted)
                        }
                        Text(
                            text = if (user.isActive) "✓ सक्रिय" else "✕ निष्क्रिय",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (user.isActive) StatusSuccess else StatusError
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SurfaceCardBorder)

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reset Password Button
                        OutlinedButton(
                            onClick = { userToResetPassword = user },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("पासवर्ड", fontSize = 10.sp)
                        }

                        // WhatsApp Share Credentials
                        Button(
                            onClick = {
                                val msg = """
*॥ श्री मां नर्मदा भक्त परिवार ॥*
*कार्यालय:* सांकला माता मंदिर, तारादेही, तेंदूखेड़ा (दमोह)
*हेल्पलाइन:* 7440771076

*आपके अधिकृत क्रेडेंशियल्स:*
नाम: ${user.fullName}
भूमिका: ${user.role.labelHindi}
यूज़रनेम: ${user.username}
पासवर्ड: ${user.password}
लॉगिन लिंक: https://ais-pre-ufnuqcimidjtcheuxy4lna-577656774271.asia-east1.run.app

_कृपया इस पासवर्ड को सुरक्षित रखें।_
                                """.trimIndent()
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, msg)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "क्रेडेंशियल भेजें"))
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("WhatsApp", fontSize = 10.sp)
                        }

                        if (!isMasterSuperAdmin) {
                            // Toggle Active
                            OutlinedButton(
                                onClick = { viewModel.toggleStaffActive(user) },
                                modifier = Modifier.weight(0.9f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(if (user.isActive) "रोकें" else "चालू", fontSize = 10.sp)
                            }

                            // Delete button
                            IconButton(
                                onClick = { userToDelete = user },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "हटाएं", tint = StatusError, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // CREATE STAFF DIALOG
    if (showCreateDialog) {
        CreateStaffDialog(
            viewModel = viewModel,
            onDismiss = { showCreateDialog = false }
        )
    }

    // RESET PASSWORD DIALOG
    if (userToResetPassword != null) {
        ResetStaffPasswordDialog(
            user = userToResetPassword!!,
            onConfirm = { newPass ->
                viewModel.resetStaffPassword(userToResetPassword!!.id, newPass)
                userToResetPassword = null
            },
            onDismiss = { userToResetPassword = null }
        )
    }

    // DELETE CONFIRM DIALOG
    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("खाता हटाने की पुष्टि करें") },
            text = { Text("क्या आप वास्तव में ${userToDelete!!.fullName} (${userToDelete!!.username}) का खाता हटाना चाहते हैं?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStaffUser(userToDelete!!)
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                ) {
                    Text("हाँ, हटाएं")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) { Text("रद्द करें") }
            }
        )
    }
}

/**
 * Super Admin Dialog to create new Admin, Operator, Volunteer, Collector credentials.
 */
@Composable
private fun CreateStaffDialog(
    viewModel: NarmadaViewModel,
    onDismiss: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.ADMIN) }
    var password by remember { mutableStateOf("Narmada@${(1000..9999).random()}") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val roles = listOf(
        UserRole.ADMIN,
        UserRole.OPERATOR,
        UserRole.SEAT_MANAGER,
        UserRole.VOLUNTEER,
        UserRole.COLLECTOR
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("नया स्टाफ क्रेडेंशियल बनाएं", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SaffronDark)
                Text("सुपर एडमिन द्वारा अधिकृत", fontSize = 10.sp, color = TextSecondary)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Role Selector
                Text("भूमिका (Role):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    roles.take(3).forEach { role ->
                        FilterChip(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role },
                            label = { Text(role.labelHindi.split(" ")[0], fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SaffronPrimary,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    roles.drop(3).forEach { role ->
                        FilterChip(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role },
                            label = { Text(role.labelHindi.split(" ")[0], fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SaffronPrimary,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                }

                NarmadaOutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it; errorMsg = null },
                    label = { Text("पूरा नाम (Full Name)") },
                    placeholder = { Text("जैसे राजेश शर्मा") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                NarmadaOutlinedTextField(
                    value = username,
                    onValueChange = { username = it; errorMsg = null },
                    label = { Text("यूज़रनेम / ईमेल ID") },
                    placeholder = { Text("जैसे admin_tendukheda") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                NarmadaOutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("मोबाइल नंबर (WhatsApp)") },
                    placeholder = { Text("10 अंकों का नंबर") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )

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
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { password = "Narmada@${(1000..9999).random()}" }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "नया पासवर्ड बनाएं", tint = SaffronPrimary)
                    }
                }

                if (errorMsg != null) {
                    Text(text = errorMsg!!, color = StatusError, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                    viewModel.createStaffUser(
                        fullName = fullName,
                        username = username,
                        email = email.ifBlank { "$username@narmada.org" },
                        mobile = mobile,
                        role = selectedRole,
                        pass = password,
                        onSuccess = onDismiss,
                        onError = { errorMsg = it }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Text("क्रेडेंशियल सुरक्षित करें")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("रद्द करें") }
        }
    )
}

@Composable
private fun ResetStaffPasswordDialog(
    user: AppUser,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newPassword by remember { mutableStateOf("Narmada@${(1000..9999).random()}") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${user.fullName} का पासवर्ड बदलें", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
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

/**
 * Super Admin DP / Logo Settings Tab.
 * Live preview, 7 sacred presets, custom URL, and instant application across app.
 */
@Composable
fun AdminDpLogoTab(viewModel: NarmadaViewModel) {
    val logoConfig by viewModel.logoConfig.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()

    var selectedType by remember(logoConfig) { mutableStateOf(logoConfig.type) }
    var selectedPreset by remember(logoConfig) { mutableStateOf(logoConfig.presetId) }
    var customUrl by remember(logoConfig) { mutableStateOf(logoConfig.customUrl) }
    var festivalTitle by remember(logoConfig) { mutableStateOf(logoConfig.festivalTitle) }
    var festivalSubtitle by remember(logoConfig) { mutableStateOf(logoConfig.festivalSubtitle) }
    var badgeText by remember(logoConfig) { mutableStateOf(logoConfig.badgeText) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "संस्था का लोगो / डीपी विन्यास (DP Management)",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDark
                    )
                    Text(
                        text = "सोशल मीडिया की तरह अपनी पसंद की डीपी कभी भी बदलें",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    color = SaffronContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "⚡ सुपर एडमिन पावर",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Live Preview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.5.dp, SaffronPrimary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("लाइव डीपी पूर्वावलोकन (Live App Preview):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SaffronDark)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SaffronPrimary)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SacredDpAvatar(
                            presetId = selectedPreset,
                            customUrl = if (selectedType == "CUSTOM_URL") customUrl else "",
                            size = 50.dp,
                            showBadge = true,
                            badgeText = badgeText
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = festivalTitle.ifBlank { "श्री मां नर्मदा जन्मोत्सव" },
                                color = PureWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "तेंदूखेड़ा, जिला दमोह (म.प्र.)",
                                color = PureWhite.copy(alpha = 0.9f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Mode switch
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == "PRESET",
                    onClick = { selectedType = "PRESET" },
                    label = { Text("🌸 कार्टून व पावन प्रतीक") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SaffronPrimary,
                        selectedLabelColor = PureWhite
                    )
                )
                FilterChip(
                    selected = selectedType == "CUSTOM_URL",
                    onClick = { selectedType = "CUSTOM_URL" },
                    label = { Text("📸 सीधे फोटो अपलोड करें") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SaffronPrimary,
                        selectedLabelColor = PureWhite
                    )
                )
            }
        }

        if (selectedType == "PRESET") {
            item {
                Text(
                    text = "पवित्र डीपी प्रीसेट्स (कार्टून थीम सहित) में से चुनें:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
            }

            items(SACRED_DP_PRESETS) { preset ->
                val isSelected = selectedPreset == preset.id
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedPreset = preset.id
                            badgeText = preset.badgeLabel
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) SaffronContainer.copy(alpha = 0.4f) else PureWhite,
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) SaffronPrimary else SurfaceCardBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SacredDpAvatar(presetId = preset.id, size = 44.dp, showBorder = false)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = preset.nameHindi, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = preset.description, fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "बैज: ${preset.badgeLabel}", fontSize = 10.sp, color = SaffronDark, fontWeight = FontWeight.SemiBold)
                        }
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        } else {
            item {
                DirectPhotoUploaderSection(
                    currentPhotoUrl = customUrl,
                    onPhotoSelected = { localUri ->
                        customUrl = localUri
                        selectedType = "CUSTOM_URL"
                    },
                    onPhotoRemoved = {
                        customUrl = ""
                    }
                )
            }
        }

        // Festival title & badge text
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("शीर्षक एवं बैज अनुकूलन (Customization):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    NarmadaOutlinedTextField(
                        value = festivalTitle,
                        onValueChange = { festivalTitle = it },
                        label = { Text("महोत्सव शीर्षक") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    NarmadaOutlinedTextField(
                        value = badgeText,
                        onValueChange = { badgeText = it },
                        label = { Text("डीपी बैज टेक्स्ट") },
                        placeholder = { Text("जैसे 🚩 पावन जन्मोत्सव") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    val updated = logoConfig.copy(
                        type = selectedType,
                        presetId = selectedPreset,
                        customUrl = customUrl.trim(),
                        festivalTitle = festivalTitle.trim(),
                        festivalSubtitle = festivalSubtitle.trim(),
                        badgeText = badgeText.trim(),
                        updatedAt = System.currentTimeMillis()
                    )
                    viewModel.updateLogoConfig(updated)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("डीपी तुरंत अपडेट करें (Apply DP Everywhere)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
