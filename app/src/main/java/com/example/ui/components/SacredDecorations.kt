package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLogoConfig
import com.example.data.model.AppUser
import com.example.data.model.Devotee
import com.example.data.model.DonationReceipt
import com.example.data.model.PassItem
import com.example.data.model.UserRole
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SacredTopBar(
    title: String,
    currentRole: UserRole,
    onRoleSelect: (UserRole) -> Unit,
    onBack: (() -> Unit)? = null,
    logoConfig: AppLogoConfig = AppLogoConfig(),
    currentUser: AppUser? = null,
    onLoginClick: (() -> Unit)? = null,
    onLogoutClick: (() -> Unit)? = null,
    onChangeDpClick: (() -> Unit)? = null
) {
    var showRoleMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SaffronPrimary,
        shadowElevation = 6.dp
    ) {
        Column {
            // Sacred Tagline with Tendukheda Damoh office center
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SaffronDark)
                    .padding(vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "॥ श्री नर्मदे हर ॥ — सांकला माता मंदिर, तारादेही, तेंदूखेड़ा (दमोह)",
                    color = SacredGoldLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "वापस जाएं",
                            tint = PureWhite
                        )
                    }
                } else {
                    // Dynamic DP / Logo Avatar
                    SacredDpAvatar(
                        presetId = logoConfig.presetId,
                        customUrl = if (logoConfig.type == "CUSTOM_URL") logoConfig.customUrl else "",
                        size = 40.dp,
                        showBorder = true,
                        onClick = if (currentRole == UserRole.SUPER_ADMIN) onChangeDpClick else null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = PureWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "श्री मां नर्मदा भक्त परिवार — तेंदूखेड़ा (दमोह)",
                        color = PureWhite.copy(alpha = 0.9f),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (currentUser == null) {
                    // Quick Login Button for Guest/Devotees/Staff
                    Button(
                        onClick = { onLoginClick?.invoke() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite.copy(alpha = 0.25f),
                            contentColor = PureWhite
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = "लॉगिन",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "लॉगिन",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Logged in User Role Pill
                    Button(
                        onClick = { showRoleMenu = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentRole == UserRole.SUPER_ADMIN) SacredGold else PureWhite.copy(alpha = 0.22f),
                            contentColor = if (currentRole == UserRole.SUPER_ADMIN) Color(0xFF3E2723) else PureWhite
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = if (currentRole == UserRole.SUPER_ADMIN) Icons.Default.Stars else Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (currentRole) {
                                UserRole.SUPER_ADMIN -> "सुपर एडमिन"
                                UserRole.ADMIN -> "प्रशासक"
                                UserRole.EVENT_MANAGER -> "कार्यक्रम प्रबंधक"
                                UserRole.REGISTRATION_OPERATOR -> "पंजीयन ऑपरेटर"
                                UserRole.SEAT_MANAGER -> "सीट प्रबंधक"
                                UserRole.COLLECTION_OPERATOR -> "संग्रहकर्ता"
                                UserRole.CHECK_IN_VOLUNTEER -> "स्वयंसेवक"
                                UserRole.CONTENT_MANAGER -> "सामग्री प्रबंधक"
                                UserRole.REPORT_VIEWER -> "रिपोर्ट समीक्षक"
                                UserRole.PUBLIC_USER -> "भक्त"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showRoleMenu) {
        ModalBottomSheet(
            onDismissRequest = { showRoleMenu = false },
            containerColor = PureWhite,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = currentUser?.fullName ?: "उपयोगकर्ता प्रोफ़ाइल",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark
                        )
                        Text(
                            text = "यूज़रनेम: ${currentUser?.username ?: "Guest"} | भूमिका: ${currentRole.labelHindi}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = { showRoleMenu = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "बंद करें")
                    }
                }

                HorizontalDivider(color = SurfaceCardBorder, modifier = Modifier.padding(vertical = 8.dp))

                if (currentRole == UserRole.SUPER_ADMIN && onChangeDpClick != null) {
                    Button(
                        onClick = {
                            showRoleMenu = false
                            onChangeDpClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("संस्था का लोगो / डीपी बदलें (Change DP)")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Text(
                    text = "सक्रिय भूमिका स्विच करें (Switch Role):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                UserRole.entries.forEach { role ->
                    val isSelected = currentRole == role
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable {
                                onRoleSelect(role)
                                showRoleMenu = false
                            },
                        color = if (isSelected) SaffronContainer.copy(alpha = 0.35f) else PureWhite,
                        shape = RoundedCornerShape(10.dp),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, SaffronPrimary) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    onRoleSelect(role)
                                    showRoleMenu = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = SaffronPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = role.labelHindi,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SaffronDark else Color(0xFF1A1A1A)
                            )
                        }
                    }
                }

                if (currentUser != null && onLogoutClick != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            showRoleMenu = false
                            onLogoutClick()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("लॉगआउट करें (Logout)")
                    }
                }
            }
        }
    }
}

// Guaranteed crisp, high-contrast dark text styling for all input fields across the app
@Composable
fun narmadaTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color(0xFF1A1A1A),
    unfocusedTextColor = Color(0xFF1A1A1A),
    disabledTextColor = Color(0xFF757575),
    errorTextColor = StatusError,
    focusedContainerColor = PureWhite,
    unfocusedContainerColor = PureWhite,
    disabledContainerColor = Color(0xFFF5F5F5),
    errorContainerColor = PureWhite,
    cursorColor = SaffronPrimary,
    errorCursorColor = StatusError,
    selectionColors = androidx.compose.foundation.text.selection.TextSelectionColors(
        handleColor = SaffronPrimary,
        backgroundColor = SaffronContainer.copy(alpha = 0.6f)
    ),
    focusedBorderColor = SaffronPrimary,
    unfocusedBorderColor = Color(0xFFBCAAA4),
    disabledBorderColor = Color(0xFFE0E0E0),
    errorBorderColor = StatusError,
    focusedLabelColor = SaffronDark,
    unfocusedLabelColor = Color(0xFF5D4037),
    disabledLabelColor = Color(0xFF757575),
    errorLabelColor = StatusError,
    focusedPlaceholderColor = Color(0xFF8D6E63),
    unfocusedPlaceholderColor = Color(0xFF8D6E63),
    disabledPlaceholderColor = Color(0xFF9E9E9E),
    errorPlaceholderColor = StatusError,
    focusedLeadingIconColor = SaffronPrimary,
    unfocusedLeadingIconColor = Color(0xFF5D4037),
    disabledLeadingIconColor = Color(0xFF9E9E9E),
    errorLeadingIconColor = StatusError,
    focusedTrailingIconColor = SaffronPrimary,
    unfocusedTrailingIconColor = Color(0xFF5D4037),
    disabledTrailingIconColor = Color(0xFF9E9E9E),
    errorTrailingIconColor = StatusError,
    focusedPrefixColor = Color(0xFF1A1A1A),
    unfocusedPrefixColor = Color(0xFF1A1A1A),
    focusedSuffixColor = Color(0xFF1A1A1A),
    unfocusedSuffixColor = Color(0xFF1A1A1A)
)

@Composable
fun CountdownCard(modifier: Modifier = Modifier) {
    // Target: 11 February 2027 00:00:00 GMT+5:30
    val targetTime = 1799625600000L // 11 Feb 2027 approx
    var remainingMillis by remember { mutableStateOf(targetTime - System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            remainingMillis = (targetTime - System.currentTimeMillis()).coerceAtLeast(0L)
            delay(1000L)
        }
    }

    val seconds = (remainingMillis / 1000) % 60
    val minutes = (remainingMillis / (1000 * 60)) % 60
    val hours = (remainingMillis / (1000 * 60 * 60)) % 24
    val days = remainingMillis / (1000 * 60 * 60 * 24)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = NarmadaBlue),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = SacredGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "महोत्सव शुभारंभ में शेष समय",
                    color = SacredGoldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CountdownUnit(value = days.toString(), label = "दिन")
                CountdownUnit(value = "%02d".format(hours), label = "घंटे")
                CountdownUnit(value = "%02d".format(minutes), label = "मिनट")
                CountdownUnit(value = "%02d".format(seconds), label = "सेकंड")
            }
        }
    }
}

@Composable
private fun CountdownUnit(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PureWhite.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                color = PureWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = PureWhite.copy(alpha = 0.8f),
            fontSize = 10.sp
        )
    }
}

// Crisp Vector QR Code Generator Canvas
@Composable
fun QrCodeCanvas(
    token: String,
    modifier: Modifier = Modifier,
    sizeDp: Int = 160
) {
    // Deterministic 21x21 QR-like matrix derived from SHA/string hash
    val matrixSize = 21
    val isFilled = remember(token) {
        val hash = (token.hashCode() xor 0x5A5A5A5A).toLong()
        val grid = Array(matrixSize) { BooleanArray(matrixSize) }

        // Finder patterns (top-left, top-right, bottom-left 7x7)
        fun setFinder(r0: Int, c0: Int) {
            for (r in 0 until 7) {
                for (c in 0 until 7) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isInner = r in 2..4 && c in 2..4
                    grid[r0 + r][c0 + c] = isBorder || isInner
                }
            }
        }
        setFinder(0, 0)
        setFinder(0, matrixSize - 7)
        setFinder(matrixSize - 7, 0)

        // Timing patterns
        for (i in 8 until matrixSize - 8) {
            grid[6][i] = (i % 2 == 0)
            grid[i][6] = (i % 2 == 0)
        }

        // Fill remaining with hash bits
        var bitIndex = 0
        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                val inTopLeft = r < 8 && c < 8
                val inTopRight = r < 8 && c >= matrixSize - 8
                val inBottomLeft = r >= matrixSize - 8 && c < 8
                val inCenter = r in 9..11 && c in 9..11
                if (!inTopLeft && !inTopRight && !inBottomLeft && !inCenter) {
                    val shift = (bitIndex + (r * 13) + (c * 17)) % 62
                    grid[r][c] = ((hash shr shift) and 1L) == 1L
                    bitIndex++
                }
            }
        }
        grid
    }

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PureWhite)
            .border(2.dp, SaffronContainer, RoundedCornerShape(12.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellSize = size.width / matrixSize
            for (r in 0 until matrixSize) {
                for (c in 0 until matrixSize) {
                    if (isFilled[r][c]) {
                        drawRect(
                            color = Color(0xFF1A1A1A),
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize * 0.95f, cellSize * 0.95f)
                        )
                    }
                }
            }
            // Sacred center emblem
            val center = Offset(size.width / 2f, size.height / 2f)
            val emblemRadius = cellSize * 2f
            drawCircle(
                color = PureWhite,
                radius = emblemRadius,
                center = center
            )
            drawCircle(
                color = SaffronPrimary,
                radius = emblemRadius * 0.85f,
                center = center
            )
            drawCircle(
                color = SacredGold,
                radius = emblemRadius * 0.45f,
                center = center
            )
        }
    }
}

// Devotional Pass Card
@Composable
fun DevoteePassCard(
    devotee: Devotee,
    pass: PassItem,
    onShareWhatsApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .border(1.5.dp, SacredGold, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(SaffronPrimary, SaffronDark)
                        )
                    )
                    .padding(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "॥ श्री नर्मदे हर ॥",
                        color = SacredGoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "श्री मां नर्मदा भक्त परिवार",
                        color = PureWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "द्वितीय विशाल जन्मोत्सव एवं चुनरी पदयात्रा 2027",
                        color = PureWhite.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = SacredGold,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = pass.category,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Body
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = devotee.fullName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark
                        )
                        if (devotee.fatherHusbandName.isNotBlank()) {
                            Text(
                                text = "आत्मज/पति: ${devotee.fatherHusbandName}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = "पंजीयन ID: ${pass.registrationId}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NarmadaBlue
                        )
                        Text(
                            text = "पास ID: ${pass.passId}",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }

                    // QR code
                    QrCodeCanvas(token = pass.qrToken, sizeDp = 105)
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = SurfaceCardBorder)
                Spacer(modifier = Modifier.height(12.dp))

                // Seat & Venue Details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SaffronContainer.copy(alpha = 0.35f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "स्थान (Venue)", fontSize = 10.sp, color = TextSecondary)
                        Text(text = pass.venue, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "आवंटित सीट", fontSize = 10.sp, color = TextSecondary)
                        Text(
                            text = if (pass.seatNumber.isNotBlank()) pass.seatNumber else "सामान्य प्रवेश",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (pass.seatNumber.isNotBlank()) SaffronDark else NarmadaBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusChip(status = pass.status)
                    Text(
                        text = "दिनांक: 11–13 फ़रवरी 2027",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onShareWhatsApp,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "WhatsApp पर साझा करें",
                        tint = PureWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "WhatsApp पर पास साझा करें", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Official Receipt Card
@Composable
fun OfficialReceiptCard(
    receipt: DonationReceipt,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .border(1.5.dp, SacredGold, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "श्री मां नर्मदा भक्त परिवार",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDark
                    )
                    Text(
                        text = "आधिकारिक दान पावती (Donation Receipt)",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Surface(
                    color = StatusSuccessBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = StatusSuccess,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "सत्यापित",
                            fontSize = 11.sp,
                            color = StatusSuccess,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = SurfaceCardBorder)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "रसीद संख्या", fontSize = 10.sp, color = TextMuted)
                    Text(text = receipt.receiptNumber, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NarmadaBlue)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "दानदाता", fontSize = 10.sp, color = TextMuted)
                    Text(text = receipt.donorName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "सहयोग उद्देश्य", fontSize = 10.sp, color = TextMuted)
                    Text(text = receipt.purpose, fontSize = 12.sp, color = TextSecondary)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "सहयोग राशि", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = "₹${"%,.0f".format(receipt.amount)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = StatusSuccess
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "भुगतान माध्यम", fontSize = 10.sp, color = TextMuted)
                    Text(text = receipt.paymentMode, fontSize = 12.sp, fontWeight = FontWeight.Medium)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "दिनांक", fontSize = 10.sp, color = TextMuted)
                    Text(text = receipt.date, fontSize = 12.sp, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF7F7F7))
                    .padding(8.dp)
            ) {
                Text(
                    text = "सुरक्षा टोकन (SHA-256): ${receipt.verificationToken}",
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onShare,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "WhatsApp पर रसीद साझा करें", color = PureWhite, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (bgColor, textColor, label) = when (status.uppercase()) {
        "CONFIRMED", "ACTIVE", "VERIFIED", "APPROVED" -> Triple(StatusSuccessBg, StatusSuccess, "सत्यापित / सक्रिय")
        "USED" -> Triple(StatusInfoBg, StatusInfo, "उपयोग हुआ (Used)")
        "WAITLISTED" -> Triple(StatusWarningBg, StatusWarning, "प्रतीक्षारत (Waitlist)")
        "CANCELLED", "BLOCKED", "REJECTED" -> Triple(StatusErrorBg, StatusError, "रद्द / निरस्त")
        else -> Triple(Color(0xFFEEEEEE), Color.DarkGray, status)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun NarmadaOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    keyboardActions: androidx.compose.foundation.text.KeyboardActions = androidx.compose.foundation.text.KeyboardActions.Default
) {
    CompositionLocalProvider(
        LocalContentColor provides Color(0xFF1A1A1A),
        LocalTextStyle provides androidx.compose.ui.text.TextStyle(
            color = Color(0xFF1A1A1A),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            visualTransformation = visualTransformation,
            label = label?.let { l ->
                {
                    CompositionLocalProvider(LocalContentColor provides Color(0xFF5D4037)) {
                        l()
                    }
                }
            },
            placeholder = placeholder?.let { p ->
                {
                    CompositionLocalProvider(LocalContentColor provides Color(0xFF8D6E63)) {
                        p()
                    }
                }
            },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = narmadaTextFieldColors(),
            textStyle = androidx.compose.ui.text.TextStyle(
                color = Color(0xFF1A1A1A),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            ),
            shape = RoundedCornerShape(10.dp)
        )
    }
}
