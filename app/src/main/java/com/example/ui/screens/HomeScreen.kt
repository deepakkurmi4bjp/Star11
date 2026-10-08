package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppLogoConfig
import com.example.data.model.NoticeItem
import com.example.data.model.UserRole
import com.example.ui.AppScreen
import com.example.ui.NarmadaViewModel
import com.example.ui.components.CountdownCard
import com.example.ui.components.CartoonMaaNarmadaThemeCard
import com.example.ui.components.SacredDpAvatar
import com.example.ui.components.SacredDpPickerModal
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: NarmadaViewModel,
    modifier: Modifier = Modifier
) {
    val notices by viewModel.repository.activeNotices.collectAsStateWithLifecycle(emptyList())
    val cmsContent by viewModel.repository.allCmsContent.collectAsStateWithLifecycle(emptyList())
    val logoConfig by viewModel.logoConfig.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()

    var showDpModal by remember { mutableStateOf(false) }

    val cmsMap = remember(cmsContent) { cmsContent.associate { it.key to it.value } }

    val eventName = cmsMap["event_name"] ?: "द्वितीय विशाल श्री मां नर्मदा जन्मोत्सव एवं चुनरी पदयात्रा महोत्सव 2027"
    val eventDates = cmsMap["event_dates"] ?: "11–13 फरवरी 2027"
    val venueName = cmsMap["venue_name"] ?: "मां नर्मदा पावन तट, मुख्य महाआरती घाट एवं भव्य पंडाल परिसर"
    val heroTagline = cmsMap["hero_tagline"] ?: "॥ हर हर नर्मदे — नर्मदे हर ॥"
    val developerCredit = cmsMap["developer_credit"] ?: "Deepak Patel"

    val isSuperAdmin = currentRole == UserRole.SUPER_ADMIN ||
            currentUser?.email.equals("Deepak53802@gmail.com", ignoreCase = true) ||
            currentUser?.username.equals("Deepak53802@gmail.com", ignoreCase = true)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Hero Section
        item {
            HeroSection(
                tagline = heroTagline,
                eventName = eventName,
                dates = eventDates,
                venue = venueName,
                logoConfig = logoConfig,
                isSuperAdmin = isSuperAdmin,
                onChangeDpClick = { showDpModal = true },
                onRegisterClick = { viewModel.navigateTo(AppScreen.REGISTER_WIZARD) }
            )
        }

        // Active Notice Banner
        if (notices.isNotEmpty()) {
            item {
                NoticeBanner(notices = notices)
            }
        }

        // Countdown Timer
        item {
            CountdownCard(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // New Cartoon Theme Maa Narmada Showcase & Direct Photo Upload Card
        item {
            CartoonMaaNarmadaThemeCard(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                onUploadPhotoClick = { showDpModal = true }
            )
        }

        // 6 Public Buttons Section
        item {
            QuickActionsSection(
                onAction = { screen -> viewModel.navigateTo(screen) }
            )
        }

        // Mahotsav Highlights
        item {
            EventHighlightsSection()
        }

        // About the Event Summary
        item {
            AboutSummaryCard(
                aboutText = cmsMap["about_event"] ?: "",
                aboutChunri = cmsMap["about_chunri"] ?: "",
                onMoreClick = { viewModel.navigateTo(AppScreen.ABOUT_CONTACT) }
            )
        }

        // Devotional Photo & Sacred Gallery
        item {
            SacredGallerySection()
        }

        // Developer & Organization Footer
        item {
            FooterSection(developerCredit = developerCredit)
        }
    }

    if (showDpModal) {
        SacredDpPickerModal(
            currentConfig = logoConfig,
            onSave = { updated -> viewModel.updateLogoConfig(updated) },
            onDismiss = { showDpModal = false }
        )
    }
}

@Composable
private fun HeroSection(
    tagline: String,
    eventName: String,
    dates: String,
    venue: String,
    logoConfig: AppLogoConfig,
    isSuperAdmin: Boolean,
    onChangeDpClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(SaffronPrimary, SaffronDark, Color(0xFF871E00))
                )
            )
    ) {
        // River wave animation canvas background
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            // Holy River waves
            val wavePath1 = Path().apply {
                moveTo(0f, h * 0.72f)
                cubicTo(w * 0.25f, h * 0.65f, w * 0.75f, h * 0.80f, w, h * 0.70f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(wavePath1, color = NarmadaBlue.copy(alpha = 0.35f))

            val wavePath2 = Path().apply {
                moveTo(0f, h * 0.82f)
                cubicTo(w * 0.3f, h * 0.88f, w * 0.7f, h * 0.76f, w, h * 0.84f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(wavePath2, color = NarmadaBlueLight.copy(alpha = 0.3f))

            // Floating Diya light aura circles
            drawCircle(
                color = SacredGold.copy(alpha = 0.18f),
                radius = 70.dp.toPx(),
                center = Offset(w * 0.85f, h * 0.25f)
            )
            drawCircle(
                color = SacredGold.copy(alpha = 0.12f),
                radius = 120.dp.toPx(),
                center = Offset(w * 0.85f, h * 0.25f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dynamic DP / Logo Avatar on Hero
            SacredDpAvatar(
                presetId = logoConfig.presetId,
                customUrl = if (logoConfig.type == "CUSTOM_URL") logoConfig.customUrl else "",
                size = 78.dp,
                showBorder = true,
                showBadge = true,
                badgeText = logoConfig.badgeText,
                onClick = onChangeDpClick
            )

            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                onClick = onChangeDpClick,
                color = PureWhite.copy(alpha = 0.25f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = PureWhite, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSuperAdmin) "डीपी / फोटो बदलें" else "डीपी दर्शन / फोटो लगाएं",
                        fontSize = 10.sp,
                        color = PureWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sacred Tagline Badge
            Surface(
                color = PureWhite.copy(alpha = 0.18f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SacredGold.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Brightness7,
                        contentDescription = null,
                        tint = SacredGoldLight,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tagline,
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "श्री मां नर्मदा भक्त परिवार",
                color = SacredGoldLight,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = eventName,
                color = PureWhite,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Organization Center Tendukheda Damoh Badge
            Surface(
                color = PureWhite.copy(alpha = 0.18f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "📍 मुख्य तहसील तेंदूखेड़ा, जिला दमोह (म.प्र.)",
                    color = PureWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = PureWhite,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = SaffronPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dates,
                        color = SaffronDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "कार्यालय: सांकला माता मंदिर, तारादेही, तेंदूखेड़ा (दमोह) | WhatsApp: 7440771076",
                color = PureWhite.copy(alpha = 0.9f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(48.dp)
                    .testTag("hero_register_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SacredGold,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(25.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HowToReg,
                    contentDescription = null,
                    tint = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "भक्त पंजीयन करें (बिना लॉगिन के)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun NoticeBanner(notices: List<NoticeItem>) {
    val topNotice = notices.firstOrNull() ?: return
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = StatusWarningBg),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StatusWarning.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = null,
                tint = StatusWarning,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topNotice.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = topNotice.description,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun QuickActionsSection(onAction: (AppScreen) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(
            text = "प्रमुख सेवाएं एवं विकल्प",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = SaffronDark,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 6 Main Buttons requested
        val actions = listOf(
            Triple("भक्त पंजीयन करें", Icons.Default.AppRegistration, AppScreen.REGISTER_WIZARD),
            Triple("अपना पंजीयन देखें", Icons.Default.Search, AppScreen.MY_REGISTRATION),
            Triple("पास / सीट देखें", Icons.Default.ConfirmationNumber, AppScreen.VERIFY_PASS),
            Triple("रसीद सत्यापित करें", Icons.Default.Verified, AppScreen.VERIFY_RECEIPT),
            Triple("कार्यक्रम देखें", Icons.Default.EventNote, AppScreen.SCHEDULE),
            Triple("संपर्क एवं जानकारी", Icons.Default.ContactSupport, AppScreen.ABOUT_CONTACT)
        )

        for (i in actions.indices step 2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val item1 = actions[i]
                QuickActionButton(
                    title = item1.first,
                    icon = item1.second,
                    onClick = { onAction(item1.third) },
                    modifier = Modifier.weight(1f),
                    color = if (i == 0) SaffronPrimary else NarmadaBlue
                )
                if (i + 1 < actions.size) {
                    val item2 = actions[i + 1]
                    QuickActionButton(
                        title = item2.first,
                        icon = item2.second,
                        onClick = { onAction(item2.third) },
                        modifier = Modifier.weight(1f),
                        color = NarmadaBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Secondary actions: Donation and Admin
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onAction(AppScreen.DONATION) },
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SacredGold)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.VolunteerActivism, contentDescription = null, tint = SaffronPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "सहयोग / दान", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SaffronDark)
                        Text(text = "पावती प्राप्त करें", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onAction(AppScreen.ADMIN_DASHBOARD) },
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = NarmadaBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "व्यवस्थापक पोर्टल", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NarmadaBlue)
                        Text(text = "Admin & Scanner", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color
) {
    Card(
        modifier = modifier
            .height(86.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun EventHighlightsSection() {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(
            text = "महोत्सव की प्रमुख विशेषताएं",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = SaffronDark,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
        )

        val highlights = listOf(
            Triple("255 मीटर चुनरी अर्पण", "मां नर्मदा को 255 मीटर की अखंड सुसज्जित चुनरी का पावन समर्पण", Icons.Default.Waves),
            Triple("भव्य नर्मदा महाआरती", "नर्मदा तट पर अद्भुत संगीतमय दिव्य महाआरती एवं पावन दीपदान दर्शन", Icons.Default.Brightness7),
            Triple("अखंड महाप्रसाद भंडारा", "तीनों दिवस लाखों श्रद्धालुओं हेतु अनवरत शुद्ध सात्विक महाप्रसाद", Icons.Default.Restaurant),
            Triple("पूज्य संत समागम", "भारत भर से पधारे शीर्ष संतों के आशीर्वचन एवं सत्संग प्रवचन", Icons.Default.People)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(highlights) { item ->
                Card(
                    modifier = Modifier.width(230.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SaffronContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.third,
                                contentDescription = null,
                                tint = SaffronPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = item.first,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.second,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutSummaryCard(
    aboutText: String,
    aboutChunri: String,
    onMoreClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = SaffronPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "श्री मां नर्मदा जन्मोत्सव एवं चुनरी यात्रा",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaffronDark
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = aboutText.ifBlank {
                    "मां रेवा के जन्मोत्सव के पावन अवसर पर श्री मां नर्मदा भक्त परिवार द्वारा 11 से 13 फ़रवरी 2027 तक भव्य तीन दिवसीय जन्मोत्सव एवं 255 मीटर की अखंड चुनरी पदयात्रा का महाआयोजन किया जा रहा है।"
                },
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = aboutChunri.ifBlank {
                    "नर्मदा तट पर लाखों भक्तों द्वारा मां नर्मदा को श्रद्धापूर्वक 255 मीटर की सुसज्जित चुनरी अर्पित की जाएगी।"
                },
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextButton(
                onClick = onMoreClick,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "विस्तृत जानकारी एवं संपर्क देखें →",
                    color = NarmadaBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SacredGallerySection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(
            text = "पवित्र उत्सव दर्शन (Gallery)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = SaffronDark,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        val galleryItems = listOf(
            Pair("मां नर्मदा महाआरती", SaffronPrimary),
            Pair("चुनरी पदयात्रा", SacredGold),
            Pair("भक्त समागम", NarmadaBlue),
            Pair("कलश शोभायात्रा", SaffronDark)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            galleryItems.forEach { item ->
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = item.second.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, item.second.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = item.second,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.first,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FooterSection(developerCredit: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(color = SurfaceCardBorder)
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "॥ नर्मदा त्वदीय पाद पंकजं नमामि देवि नर्मदे ॥",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SaffronDark
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "© 2027 श्री मां नर्मदा भक्त परिवार — समस्त अधिकार सुरक्षित",
            fontSize = 11.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Website/Admin system developed by $developerCredit",
            fontSize = 10.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
    }
}
