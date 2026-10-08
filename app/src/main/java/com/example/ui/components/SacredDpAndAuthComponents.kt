package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.AppLogoConfig
import com.example.data.model.AppUser
import com.example.data.model.UserRole
import com.example.ui.NarmadaViewModel
import com.example.ui.theme.*

// Preset Definitions with Cartoon Maa Narmada Theme
data class DpPresetInfo(
    val id: String,
    val nameHindi: String,
    val description: String,
    val badgeLabel: String,
    val primaryColor: Color,
    val secondaryColor: Color
)

val SACRED_DP_PRESETS = listOf(
    DpPresetInfo(
        id = "CARTOON_MAA_NARMADA",
        nameHindi = "बाल रूप मां नर्मदा (नवीन कार्टून थीम)",
        description = "सुंदर कार्टून स्वरूप मां नर्मदा - मुकुट, कमल, पावन जल व दीप",
        badgeLabel = "🌸 बाल रूप मां नर्मदा",
        primaryColor = Color(0xFFFF7043),
        secondaryColor = Color(0xFFD84315)
    ),
    DpPresetInfo(
        id = "MAA_NARMADA",
        nameHindi = "मां नर्मदा दिव्य स्वरूप",
        description = "पावन नर्मदा मैया एवं कमल पुष्प का दिव्य स्वरूप",
        badgeLabel = "🚩 पावन जन्मोत्सव",
        primaryColor = SaffronPrimary,
        secondaryColor = SaffronDark
    ),
    DpPresetInfo(
        id = "SHIVLING",
        nameHindi = "नर्मदेश्वर ओंकारेश्वर ज्योतिर्लिंग",
        description = "पवित्र शिवलिंग, भस्म त्रिपुंड एवं बेलपत्र",
        badgeLabel = "🔱 हर हर महादेव",
        primaryColor = Color(0xFF2C3E50),
        secondaryColor = Color(0xFF1A252F)
    ),
    DpPresetInfo(
        id = "SACRED_OM",
        nameHindi = "दिव्य ॐ एवं सूर्य तेज",
        description = "प्रणव ओंकार एवं स्वर्णिम सूर्य का आभा मंडल",
        badgeLabel = "🕉 ॐ नमः शिवाय",
        primaryColor = SacredGold,
        secondaryColor = SaffronDark
    ),
    DpPresetInfo(
        id = "KALASH",
        nameHindi = "मंगल कलश एवं श्रीफल",
        description = "शुभ मांगलिक कलश, आम्रपल्लव एवं स्वास्तिक",
        badgeLabel = "🪔 शुभ जन्मोत्सव",
        primaryColor = Color(0xFFD35400),
        secondaryColor = Color(0xFFA04000)
    ),
    DpPresetInfo(
        id = "TRISHUL",
        nameHindi = "महादेव त्रिशूल व डमरू",
        description = "महाकाल त्रिशूल, पावन डमरू एवं रुद्राक्ष",
        badgeLabel = "🔱 नर्मदे हर",
        primaryColor = Color(0xFF962D00),
        secondaryColor = Color(0xFF5D1000)
    ),
    DpPresetInfo(
        id = "DEEPAM",
        nameHindi = "पावन महाआरती दीप",
        description = "नर्मदा तट पर प्रज्ज्वलित दीप एवं आरती ज्योत",
        badgeLabel = "✨ महाआरती पर्व",
        primaryColor = SaffronPrimary,
        secondaryColor = Color(0xFFB7410E)
    ),
    DpPresetInfo(
        id = "CHUNRI_YATRA",
        nameHindi = "भव्य 255m चुनरी पदयात्रा",
        description = "255 मीटर अखंड चुनरी पदयात्रा का आधिकारिक प्रतीक",
        badgeLabel = "🚩 चुनरी पदयात्रा 2027",
        primaryColor = SacredCrimson,
        secondaryColor = Color(0xFF8B0000)
    )
)

/**
 * Dynamic DP / Logo Avatar Component.
 * Supports preset canvas vectors, custom URL with Coil, golden border, and badge.
 */
@Composable
fun SacredDpAvatar(
    presetId: String,
    customUrl: String = "",
    size: Dp = 44.dp,
    showBorder: Boolean = true,
    showBadge: Boolean = false,
    badgeText: String = "",
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .size(size)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        val shape = CircleShape
        val borderModifier = if (showBorder) {
            Modifier.border(2.dp, Brush.linearGradient(listOf(SacredGoldLight, SacredGold, SaffronPrimary)), shape)
        } else Modifier

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .then(borderModifier),
            contentAlignment = Alignment.Center
        ) {
            if (customUrl.isNotBlank()) {
                AsyncImage(
                    model = customUrl,
                    contentDescription = "संस्था का लोगो / डीपी",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                PresetVectorGraphic(presetId = presetId, size = size)
            }
        }

        if (showBadge && badgeText.isNotBlank()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 6.dp),
                shape = RoundedCornerShape(8.dp),
                color = SacredCrimson,
                shadowElevation = 2.dp
            ) {
                Text(
                    text = badgeText,
                    color = PureWhite,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun PresetVectorGraphic(presetId: String, size: Dp) {
    val preset = SACRED_DP_PRESETS.firstOrNull { it.id == presetId } ?: SACRED_DP_PRESETS[0]

    if (preset.id == "CARTOON_MAA_NARMADA") {
        Image(
            painter = painterResource(id = R.drawable.ic_cartoon_maa_narmada),
            contentDescription = "बाल रूप मां नर्मदा",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(preset.primaryColor, preset.secondaryColor)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            when (preset.id) {
                "MAA_NARMADA" -> {
                    // Holy river waves and lotus glow
                    val wavePath = Path().apply {
                        moveTo(0f, h * 0.65f)
                        cubicTo(w * 0.25f, h * 0.55f, w * 0.75f, h * 0.75f, w, h * 0.65f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(wavePath, color = NarmadaBlue.copy(alpha = 0.55f))
                    // Crown aura
                    drawCircle(color = SacredGoldLight.copy(alpha = 0.4f), radius = w * 0.28f, center = Offset(w * 0.5f, h * 0.38f))
                }
                "SHIVLING" -> {
                    // Shivling silhouette & tripundra
                    val lingamPath = Path().apply {
                        moveTo(w * 0.38f, h * 0.75f)
                        lineTo(w * 0.38f, h * 0.42f)
                        cubicTo(w * 0.38f, h * 0.22f, w * 0.62f, h * 0.22f, w * 0.62f, h * 0.42f)
                        lineTo(w * 0.62f, h * 0.75f)
                        close()
                    }
                    drawPath(lingamPath, color = Color(0xFF111111))
                    // Jaladhari base
                    drawRoundRect(
                        color = Color(0xFF1E272E),
                        topLeft = Offset(w * 0.22f, h * 0.70f),
                        size = Size(w * 0.56f, h * 0.12f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )
                    // White Chandan Tripundra lines
                    drawLine(color = Color.White.copy(alpha = 0.9f), start = Offset(w * 0.42f, h * 0.38f), end = Offset(w * 0.58f, h * 0.38f), strokeWidth = 2.5f)
                    drawLine(color = Color.White.copy(alpha = 0.9f), start = Offset(w * 0.42f, h * 0.43f), end = Offset(w * 0.58f, h * 0.43f), strokeWidth = 2.5f)
                    drawLine(color = Color.White.copy(alpha = 0.9f), start = Offset(w * 0.42f, h * 0.48f), end = Offset(w * 0.58f, h * 0.48f), strokeWidth = 2.5f)
                    drawCircle(color = Color(0xFFD63031), radius = 2.5f, center = Offset(w * 0.5f, h * 0.43f))
                }
                "SACRED_OM" -> {
                    // Sun rays aura
                    drawCircle(color = SacredGoldLight, radius = w * 0.35f, style = Stroke(width = 3f))
                }
                "KALASH" -> {
                    // Kalash vessel
                    val kalashPath = Path().apply {
                        moveTo(w * 0.35f, h * 0.42f)
                        cubicTo(w * 0.22f, h * 0.55f, w * 0.24f, h * 0.78f, w * 0.5f, h * 0.82f)
                        cubicTo(w * 0.76f, h * 0.78f, w * 0.78f, h * 0.55f, w * 0.65f, h * 0.42f)
                        close()
                    }
                    drawPath(kalashPath, color = SacredGoldLight)
                    // Coconut
                    drawCircle(color = Color(0xFF5D4037), radius = w * 0.14f, center = Offset(w * 0.5f, h * 0.32f))
                }
                "TRISHUL" -> {
                    // Center Trishul rod
                    drawLine(color = SacredGoldLight, start = Offset(w * 0.5f, h * 0.18f), end = Offset(w * 0.5f, h * 0.82f), strokeWidth = 4f)
                    // Left and right prongs
                    drawLine(color = SacredGoldLight, start = Offset(w * 0.32f, h * 0.28f), end = Offset(w * 0.5f, h * 0.42f), strokeWidth = 3f)
                    drawLine(color = SacredGoldLight, start = Offset(w * 0.68f, h * 0.28f), end = Offset(w * 0.5f, h * 0.42f), strokeWidth = 3f)
                }
                "DEEPAM" -> {
                    // Diya bowl
                    val diyaPath = Path().apply {
                        moveTo(w * 0.25f, h * 0.60f)
                        cubicTo(w * 0.30f, h * 0.82f, w * 0.70f, h * 0.82f, w * 0.75f, h * 0.60f)
                        close()
                    }
                    drawPath(diyaPath, color = Color(0xFFD35400))
                    // Flame
                    val flamePath = Path().apply {
                        moveTo(w * 0.5f, h * 0.26f)
                        cubicTo(w * 0.62f, h * 0.42f, w * 0.56f, h * 0.56f, w * 0.5f, h * 0.56f)
                        cubicTo(w * 0.44f, h * 0.56f, w * 0.38f, h * 0.42f, w * 0.5f, h * 0.26f)
                        close()
                    }
                    drawPath(flamePath, color = SacredGoldLight)
                }
                "CHUNRI_YATRA" -> {
                    // Waving sacred Chunri flag
                    val flagPath = Path().apply {
                        moveTo(w * 0.3f, h * 0.25f)
                        cubicTo(w * 0.55f, h * 0.2f, w * 0.65f, h * 0.4f, w * 0.8f, h * 0.35f)
                        lineTo(w * 0.8f, h * 0.65f)
                        cubicTo(w * 0.65f, h * 0.7f, w * 0.55f, h * 0.5f, w * 0.3f, h * 0.55f)
                        close()
                    }
                    drawPath(flagPath, color = PureWhite.copy(alpha = 0.95f))
                    drawLine(color = SacredGoldLight, start = Offset(w * 0.28f, h * 0.20f), end = Offset(w * 0.28f, h * 0.82f), strokeWidth = 3.5f)
                }
            }
        }

        // Overlay text / emblem icon
        when (preset.id) {
            "MAA_NARMADA" -> {
                Icon(
                    imageVector = Icons.Default.Brightness7,
                    contentDescription = null,
                    tint = SacredGoldLight,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
            "SACRED_OM" -> {
                Text(
                    text = "ॐ",
                    color = PureWhite,
                    fontSize = (size.value * 0.52f).sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            "CHUNRI_YATRA" -> {
                Text(
                    text = "255m",
                    color = SacredCrimson,
                    fontSize = (size.value * 0.22f).sp,
                    fontWeight = FontWeight.Black
                )
            }
            else -> {}
        }
    }
}

/**
 * Super Admin DP Customization Dialog.
 * Allows Super Admin to choose any of the 7 sacred presets or enter a custom photo/URL.
 */
@Composable
fun SacredDpPickerModal(
    currentConfig: AppLogoConfig,
    onSave: (AppLogoConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(currentConfig.type) }
    var selectedPreset by remember { mutableStateOf(currentConfig.presetId) }
    var customUrl by remember { mutableStateOf(currentConfig.customUrl) }
    var festivalTitle by remember { mutableStateOf(currentConfig.festivalTitle) }
    var festivalSubtitle by remember { mutableStateOf(currentConfig.festivalSubtitle) }
    var badgeText by remember { mutableStateOf(currentConfig.badgeText) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PureWhite,
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "संस्था का लोगो / डीपी बदलें",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark
                        )
                        Text(
                            text = "सोशल मीडिया की तरह अपनी पसंद की डीपी लगाएं",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "बंद करें")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = SurfaceCardBorder)

                // Live Preview
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SaffronContainer.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SacredDpAvatar(
                            presetId = selectedPreset,
                            customUrl = if (selectedType == "CUSTOM_URL") customUrl else "",
                            size = 54.dp,
                            showBadge = true,
                            badgeText = badgeText
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = festivalTitle.ifBlank { "श्री मां नर्मदा जन्मोत्सव" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = festivalSubtitle.ifBlank { "तेंदूखेड़ा, जिला दमोह (म.प्र.)" },
                                fontSize = 10.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mode Tabs: Sacred Presets vs Direct Photo Upload
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
                        label = { Text("📸 सीधे फोटो अपलोड") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SaffronPrimary,
                            selectedLabelColor = PureWhite
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedType == "PRESET") {
                    Text(text = "पसंद का दिव्य स्वरूप चुनें:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SACRED_DP_PRESETS.forEach { preset ->
                            val isChosen = selectedPreset == preset.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedPreset = preset.id
                                        badgeText = preset.badgeLabel
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isChosen) SaffronContainer.copy(alpha = 0.4f) else WarmIvory,
                                border = if (isChosen) androidx.compose.foundation.BorderStroke(1.5.dp, SaffronPrimary) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SacredDpAvatar(presetId = preset.id, size = 36.dp, showBorder = false)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = preset.nameHindi, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(text = preset.description, fontSize = 9.sp, color = TextSecondary, maxLines = 1)
                                    }
                                    if (isChosen) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

                Spacer(modifier = Modifier.height(10.dp))

                // Festival Title & Badge
                NarmadaOutlinedTextField(
                    value = festivalTitle,
                    onValueChange = { festivalTitle = it },
                    label = { Text("महोत्सव शीर्षक (Event Title)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("रद्द करें")
                    }
                    Button(
                        onClick = {
                            val updated = currentConfig.copy(
                                type = selectedType,
                                presetId = selectedPreset,
                                customUrl = customUrl.trim(),
                                festivalTitle = festivalTitle.trim(),
                                festivalSubtitle = festivalSubtitle.trim(),
                                badgeText = badgeText.trim(),
                                updatedAt = System.currentTimeMillis()
                            )
                            onSave(updated)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("डीपी लगाएं")
                    }
                }
            }
        }
    }
}

/**
 * Universal Authentication & Password Recovery Dialog.
 * Devotee & Staff Login, Forgot Password, and Forgot Username.
 */
@Composable
fun SacredAuthDialog(
    viewModel: NarmadaViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedTab by remember { mutableStateOf(0) } // 0: Login, 1: Forgot Password, 2: Forgot Username

    // Login Form State
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Forgot Password Form State
    var recoveryIdentifier by remember { mutableStateOf("") }
    var recoveryNewPass by remember { mutableStateOf("") }
    var recoveryConfirmPass by remember { mutableStateOf("") }
    var recoveryMessage by remember { mutableStateOf<String?>(null) }
    var recoverySuccess by remember { mutableStateOf(false) }

    // Forgot Username Form State
    var searchMobile by remember { mutableStateOf("") }
    var foundUsers by remember { mutableStateOf<List<AppUser>?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PureWhite,
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when (selectedTab) {
                                0 -> "व्यवस्थापक व भक्त लॉगिन"
                                1 -> "पासवर्ड रिकवरी (Forgot Password)"
                                else -> "यूज़रनेम खोजें (Forgot Username)"
                            },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark
                        )
                        Text(
                            text = "श्री मां नर्मदा भक्त परिवार — तेंदूखेड़ा (दमोह)",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "बंद करें")
                    }
                }

                // Tab Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0; loginError = null },
                        label = { Text("लॉगिन", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SaffronPrimary,
                            selectedLabelColor = PureWhite
                        )
                    )
                    FilterChip(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; recoveryMessage = null },
                        label = { Text("पासवर्ड भूल गए?", fontSize = 11.sp) },
                        modifier = Modifier.weight(1.3f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SaffronPrimary,
                            selectedLabelColor = PureWhite
                        )
                    )
                    FilterChip(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        label = { Text("यूज़रनेम भूल गए?", fontSize = 11.sp) },
                        modifier = Modifier.weight(1.3f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SaffronPrimary,
                            selectedLabelColor = PureWhite
                        )
                    )
                }

                HorizontalDivider(color = SurfaceCardBorder, modifier = Modifier.padding(bottom = 12.dp))

                // TAB 0: LOGIN
                if (selectedTab == 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        NarmadaOutlinedTextField(
                            value = loginIdentifier,
                            onValueChange = { loginIdentifier = it; loginError = null },
                            label = { Text("ईमेल / यूज़रनेम / मोबाइल / Reg ID") },
                            placeholder = { Text("अपना ईमेल या यूज़रनेम दर्ज करें") },
                            modifier = Modifier.fillMaxWidth().testTag("login_identifier_input"),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SaffronPrimary) }
                        )

                        NarmadaOutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it; loginError = null },
                            label = { Text("पासवर्ड दर्ज करें") },
                            placeholder = { Text("पासवर्ड") },
                            modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SaffronPrimary) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "पासवर्ड देखें/छिपाएं"
                                    )
                                }
                            }
                        )

                        if (loginError != null) {
                            Text(
                                text = loginError!!,
                                color = StatusError,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Secure login note: confidential credentials
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "सुरक्षित लॉगिन: कृपया अपने अधिकृत क्रेडेंशियल दर्ज करें।",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }

                        Button(
                            onClick = {
                                isLoading = true
                                viewModel.login(
                                    identifier = loginIdentifier,
                                    pass = loginPassword,
                                    onSuccess = {
                                        isLoading = false
                                        onDismiss()
                                    },
                                    onError = { err ->
                                        isLoading = false
                                        loginError = err
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("login_submit_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "लॉगिन करें", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        // Devotee info note
                        Text(
                            text = "✨ भक्त सूचना: पंजीयन बिना लॉगिन के भी संभव है। पंजीयन पूरा होने पर भक्त को ऑटोमेटिकली क्रेडेंशियल दिया जाता है।",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // TAB 1: FORGOT PASSWORD
                if (selectedTab == 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "अपना पंजीकृत ईमेल, मोबाइल नंबर अथवा पंजीयन ID दर्ज करके नया पासवर्ड बनाएं:",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        NarmadaOutlinedTextField(
                            value = recoveryIdentifier,
                            onValueChange = { recoveryIdentifier = it; recoveryMessage = null },
                            label = { Text("ईमेल / मोबाइल / पंजीयन ID") },
                            placeholder = { Text("जैसे 7440771076 या NBF27-XXXXXX") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        NarmadaOutlinedTextField(
                            value = recoveryNewPass,
                            onValueChange = { recoveryNewPass = it; recoveryMessage = null },
                            label = { Text("नया पासवर्ड दर्ज करें") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        NarmadaOutlinedTextField(
                            value = recoveryConfirmPass,
                            onValueChange = { recoveryConfirmPass = it; recoveryMessage = null },
                            label = { Text("नए पासवर्ड की पुष्टि करें") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        if (recoveryMessage != null) {
                            Text(
                                text = recoveryMessage!!,
                                color = if (recoverySuccess) StatusSuccess else StatusError,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                if (recoveryNewPass != recoveryConfirmPass) {
                                    recoveryMessage = "दोनों पासवर्ड समान नहीं हैं!"
                                    recoverySuccess = false
                                    return@Button
                                }
                                viewModel.recoverPassword(recoveryIdentifier, recoveryNewPass) { ok, msg ->
                                    recoverySuccess = ok
                                    recoveryMessage = msg
                                    if (ok) {
                                        loginIdentifier = recoveryIdentifier
                                        loginPassword = recoveryNewPass
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("पासवर्ड रीसेट करें")
                        }

                        if (recoverySuccess) {
                            OutlinedButton(
                                onClick = { selectedTab = 0 },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("लॉगिन पर जाएं")
                            }
                        }
                    }
                }

                // TAB 2: FORGOT USERNAME
                if (selectedTab == 2) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "अपना पंजीकृत 10 अंकों का मोबाइल नंबर दर्ज करें:",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        NarmadaOutlinedTextField(
                            value = searchMobile,
                            onValueChange = { searchMobile = it },
                            label = { Text("पंजीकृत मोबाइल नंबर") },
                            placeholder = { Text("10 अंकों का नंबर") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                viewModel.recoverUsername(searchMobile) { users ->
                                    foundUsers = users
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("यूज़रनेम खोजें")
                        }

                        if (foundUsers != null) {
                            if (foundUsers!!.isEmpty()) {
                                Text(
                                    text = "✕ इस मोबाइल नंबर से कोई खाता नहीं मिला।",
                                    color = StatusError,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "✓ पाए गए खाते (${foundUsers!!.size}):",
                                    color = StatusSuccess,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    foundUsers!!.forEach { user ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = WarmIvory,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = user.fullName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Text(text = "यूज़रनेम: ${user.username}", fontSize = 11.sp, color = NarmadaBlue, fontWeight = FontWeight.Bold)
                                                    Text(text = "भूमिका: ${user.role.labelHindi}", fontSize = 10.sp, color = TextSecondary)
                                                }
                                                IconButton(
                                                    onClick = {
                                                        clipboardManager.setText(AnnotatedString(user.username))
                                                        loginIdentifier = user.username
                                                        Toast.makeText(context, "यूज़रनेम कॉपी किया गया!", Toast.LENGTH_SHORT).show()
                                                        selectedTab = 0
                                                    }
                                                ) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = "कॉपी करें", tint = SaffronPrimary)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
