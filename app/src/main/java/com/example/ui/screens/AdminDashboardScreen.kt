package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.data.repository.NarmadaRepository
import com.example.ui.AdminTab
import com.example.ui.NarmadaViewModel
import com.example.ui.components.NarmadaOutlinedTextField
import com.example.ui.components.StatusChip
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminDashboardScreen(
    viewModel: NarmadaViewModel,
    modifier: Modifier = Modifier
) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val currentTab by viewModel.adminTab.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
    ) {
        // Horizontal Tab Bar
        AdminTabBar(
            currentTab = currentTab,
            onTabSelected = { viewModel.setAdminTab(it) }
        )

        // Tab Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentTab) {
                AdminTab.OVERVIEW -> AdminOverviewTab(viewModel = viewModel)
                AdminTab.DEVOTEES -> AdminDevoteesTab(viewModel = viewModel)
                AdminTab.SEATS -> AdminSeatsTab(viewModel = viewModel)
                AdminTab.CHECKIN -> AdminCheckinTab(viewModel = viewModel)
                AdminTab.FINANCE -> AdminFinanceTab(viewModel = viewModel)
                AdminTab.CMS_NOTICES -> AdminCmsTab(viewModel = viewModel)
                AdminTab.STAFF_MANAGEMENT -> AdminUsersAndRolesTab(viewModel = viewModel)
                AdminTab.DP_LOGO -> AdminDpLogoTab(viewModel = viewModel)
                AdminTab.AUDIT_LOGS -> AdminAuditTab(viewModel = viewModel)
                AdminTab.SETTINGS -> AdminSettingsTab(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun AdminTabBar(
    currentTab: AdminTab,
    onTabSelected: (AdminTab) -> Unit
) {
    val tabs = listOf(
        Pair(AdminTab.OVERVIEW, "डैशबोर्ड"),
        Pair(AdminTab.DEVOTEES, "पंजीयन सूची"),
        Pair(AdminTab.SEATS, "सीट व्यवस्था"),
        Pair(AdminTab.CHECKIN, "प्रवेश स्कैनर"),
        Pair(AdminTab.FINANCE, "वित्तीय लेजर"),
        Pair(AdminTab.CMS_NOTICES, "CMS व सूचनाएं"),
        Pair(AdminTab.STAFF_MANAGEMENT, "उपयोगकर्ता व भूमिकाएं"),
        Pair(AdminTab.DP_LOGO, "डीपी / लोगो विन्यास"),
        Pair(AdminTab.AUDIT_LOGS, "ऑडिट लॉग"),
        Pair(AdminTab.SETTINGS, "सेटिंग्स")
    )

    Surface(
        color = PureWhite,
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(tabs) { (tab, label) ->
                FilterChip(
                    selected = currentTab == tab,
                    onClick = { onTabSelected(tab) },
                    label = {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SaffronPrimary,
                        selectedLabelColor = PureWhite
                    )
                )
            }
        }
    }
}

// 1. OVERVIEW TAB
@Composable
private fun AdminOverviewTab(viewModel: NarmadaViewModel) {
    val devotees by viewModel.repository.allDevotees.collectAsStateWithLifecycle(emptyList())
    val seats by viewModel.repository.allSeats.collectAsStateWithLifecycle(emptyList())
    val checkins by viewModel.repository.allCheckins.collectAsStateWithLifecycle(emptyList())
    val totalDonations by viewModel.repository.totalDonationsSum.collectAsStateWithLifecycle(0.0)
    val totalExpenses by viewModel.repository.totalExpensesSum.collectAsStateWithLifecycle(0.0)

    val confirmedCount = devotees.count { it.status == "CONFIRMED" }
    val waitlistCount = devotees.count { it.status == "WAITLISTED" }
    val checkedInCount = checkins.count { it.status == "CHECKED_IN" }
    val assignedSeatsCount = seats.count { it.status == "ASSIGNED" || it.status == "OCCUPIED" }
    val availableSeatsCount = seats.count { it.status == "AVAILABLE" }

    val netBalance = (totalDonations ?: 0.0) - (totalExpenses ?: 0.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "महोत्सव सांख्यिकी एवं विश्लेषण (Analytics Overview)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark
            )
        }

        // Primary Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "कुल पंजीकृत भक्त",
                    value = "${devotees.size}",
                    subtext = "$confirmedCount सत्यापित | $waitlistCount प्रतीक्षारत",
                    icon = Icons.Default.People,
                    color = SaffronPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "सत्यापित प्रवेश (Checked In)",
                    value = "$checkedInCount",
                    subtext = "उपस्थिति दर: ${if (confirmedCount > 0) (checkedInCount * 100) / confirmedCount else 0}%",
                    icon = Icons.Default.QrCodeScanner,
                    color = NarmadaBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "पंडाल सीटें",
                    value = "$assignedSeatsCount / ${seats.size}",
                    subtext = "$availableSeatsCount उपलब्ध सीटें शेष",
                    icon = Icons.Default.Chair,
                    color = SacredGold,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "नेट वित्तीय शेष",
                    value = "₹${"%,.0f".format(netBalance)}",
                    subtext = "दान: ₹${"%,.0f".format(totalDonations ?: 0.0)} | व्यय: ₹${"%,.0f".format(totalExpenses ?: 0.0)}",
                    icon = Icons.Default.AccountBalanceWallet,
                    color = StatusSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Attendance Progress Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "उपस्थिति एवं चेक-इन प्रगति",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { if (confirmedCount > 0) checkedInCount.toFloat() / confirmedCount else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = StatusSuccess,
                        trackColor = SaffronContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "प्रवेशित: $checkedInCount भक्त", fontSize = 11.sp, color = TextSecondary)
                        Text(text = "शेष अपेक्षित: ${confirmedCount - checkedInCount}", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }

        // District Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "जिलावार भक्त सहभागिता",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val districtCounts = devotees.groupingBy { it.district.ifBlank { "नर्मदापुरम" } }.eachCount()
                    districtCounts.entries.sortedByDescending { it.value }.take(5).forEach { (dist, count) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = dist, fontSize = 12.sp, color = TextPrimary)
                            Text(text = "$count भक्त", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SaffronPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 11.sp, color = TextMuted)
            Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtext, fontSize = 9.sp, color = TextSecondary)
        }
    }
}

// 2. DEVOTEES MANAGEMENT TAB
@Composable
private fun AdminDevoteesTab(viewModel: NarmadaViewModel) {
    val context = LocalContext.current
    val devotees by viewModel.repository.allDevotees.collectAsStateWithLifecycle(emptyList())
    val filterStatus by viewModel.devoteeFilterStatus.collectAsStateWithLifecycle()
    val searchText by viewModel.devoteeSearchText.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()

    val filteredList = remember(devotees, filterStatus, searchText) {
        devotees.filter { d ->
            val matchStatus = if (filterStatus == "ALL") true else d.status == filterStatus
            val matchSearch = if (searchText.isBlank()) true else {
                d.fullName.contains(searchText, ignoreCase = true) ||
                d.registrationId.contains(searchText, ignoreCase = true) ||
                d.mobileNumber.contains(searchText) ||
                d.village.contains(searchText, ignoreCase = true)
            }
            matchStatus && matchSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "भक्त पंजीयन सूची (${filteredList.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark
            )
            Button(
                onClick = { viewModel.exportRegistrationsCsv(context, filteredList) },
                colors = ButtonDefaults.buttonColors(containerColor = NarmadaBlue),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "CSV निर्यात", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar
        NarmadaOutlinedTextField(
            value = searchText,
            onValueChange = { viewModel.setDevoteeSearch(it) },
            placeholder = { Text("नाम, मोबाइल अथवा ID से खोजें...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ALL" to "सभी", "CONFIRMED" to "सत्यापित", "WAITLISTED" to "प्रतीक्षारत", "CANCELLED" to "रद्द").forEach { (st, label) ->
                FilterChip(
                    selected = filterStatus == st,
                    onClick = { viewModel.setDevoteeFilter(st) },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredList) { devotee ->
                DevoteeAdminItem(
                    devotee = devotee,
                    onStatusChange = { newSt ->
                        viewModel.launchIO {
                            viewModel.repository.updateDevoteeStatus(
                                regId = devotee.registrationId,
                                newStatus = newSt,
                                user = currentRole.name,
                                role = currentRole.name
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DevoteeAdminItem(
    devotee: Devotee,
    onStatusChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = devotee.fullName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = "ID: ${devotee.registrationId} | मो: ${devotee.mobileNumber}", fontSize = 11.sp, color = TextMuted)
                }
                StatusChip(status = devotee.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "ग्राम: ${devotee.village} | जिला: ${devotee.district} | साथी: ${devotee.accompanyingPersonsCount}",
                fontSize = 11.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = SurfaceCardBorder)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (devotee.status == "WAITLISTED") {
                    TextButton(onClick = { onStatusChange("CONFIRMED") }) {
                        Text(text = "पुष्टि करें (Promote)", color = StatusSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (devotee.status != "CANCELLED") {
                    TextButton(onClick = { onStatusChange("CANCELLED") }) {
                        Text(text = "रद्द करें", color = StatusError, fontSize = 11.sp)
                    }
                }
                if (devotee.status == "CANCELLED") {
                    TextButton(onClick = { onStatusChange("CONFIRMED") }) {
                        Text(text = "पुनः बहाल करें", color = NarmadaBlue, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// 3. SEATS MANAGEMENT TAB
@Composable
private fun AdminSeatsTab(viewModel: NarmadaViewModel) {
    val seats by viewModel.repository.allSeats.collectAsStateWithLifecycle(emptyList())
    val selectedSeat by viewModel.selectedSeat.collectAsStateWithLifecycle()
    val devotees by viewModel.repository.allDevotees.collectAsStateWithLifecycle(emptyList())

    val sectionA = remember(seats) { seats.filter { it.section.contains("Section A") } }
    val sectionB = remember(seats) { seats.filter { it.section.contains("Section B") } }

    var selectedSectionName by remember { mutableStateOf("Section A") }
    val currentSeats = if (selectedSectionName == "Section A") sectionA else sectionB

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "मुख्य पंडाल सीट विन्यास (Seating Grid)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark
            )
            Row {
                FilterChip(
                    selected = selectedSectionName == "Section A",
                    onClick = { selectedSectionName = "Section A" },
                    label = { Text("सेक्शन A (50)", fontSize = 11.sp) },
                    modifier = Modifier.padding(end = 6.dp)
                )
                FilterChip(
                    selected = selectedSectionName == "Section B",
                    onClick = { selectedSectionName = "Section B" },
                    label = { Text("सेक्शन B (50)", fontSize = 11.sp) }
                )
            }
        }

        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SeatLegend(label = "उपलब्ध", color = Color(0xFFE8F5E9))
            SeatLegend(label = "आवंटित", color = SaffronContainer)
            SeatLegend(label = "प्रवेशित", color = NarmadaBlueContainer)
            SeatLegend(label = "ब्लॉक", color = Color(0xFFFFCDD2))
        }

        // Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(currentSeats) { seat ->
                val (bg, textColor) = when (seat.status) {
                    "AVAILABLE" -> Pair(Color(0xFFE8F5E9), StatusSuccess)
                    "ASSIGNED" -> Pair(SaffronContainer, SaffronDark)
                    "OCCUPIED" -> Pair(NarmadaBlueContainer, NarmadaBlue)
                    "BLOCKED" -> Pair(Color(0xFFFFCDD2), StatusError)
                    else -> Pair(Color(0xFFF5F5F5), Color.DarkGray)
                }

                Box(
                    modifier = Modifier
                        .height(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bg)
                        .border(1.dp, textColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable { viewModel.selectSeat(seat) }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = seat.seatNumber, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                        if (seat.assignedDevoteeName != null) {
                            Text(
                                text = seat.assignedDevoteeName.split(" ").firstOrNull() ?: "",
                                fontSize = 9.sp,
                                maxLines = 1,
                                color = TextPrimary
                            )
                        } else {
                            Text(text = seat.status.take(4), fontSize = 8.sp, color = textColor.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }
    }

    // Seat Action Dialog
    if (selectedSeat != null) {
        val seat = selectedSeat!!
        var assignDevoteeId by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { viewModel.selectSeat(null) },
            title = { Text(text = "सीट विवरण: ${seat.seatNumber}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "सेक्शन: ${seat.section} | पंक्ति: ${seat.row}")
                    Text(text = "स्थिति: ${seat.status}", fontWeight = FontWeight.Bold)
                    if (seat.assignedDevoteeName != null) {
                        Text(text = "आवंटित भक्त: ${seat.assignedDevoteeName} (${seat.assignedDevoteeId})")
                    } else {
                        Text(text = "सीट खाली है। किसी भी भक्त को आवंटित करें:")
                        NarmadaOutlinedTextField(
                            value = assignDevoteeId,
                            onValueChange = { assignDevoteeId = it },
                            label = { Text("भक्त पंजीयन ID (जैसे NBF27-000010)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                if (seat.assignedDevoteeId == null && seat.status != "BLOCKED") {
                    Button(
                        onClick = {
                            val devotee = devotees.find { it.registrationId.equals(assignDevoteeId.trim(), ignoreCase = true) }
                            if (devotee != null) {
                                viewModel.assignSeatToDevotee(seat.seatNumber, devotee.registrationId, devotee.fullName)
                            } else {
                                viewModel.showSnackbar("अमान्य पंजीयन संख्या।")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                    ) {
                        Text("आवंटित करें")
                    }
                } else if (seat.assignedDevoteeId != null) {
                    Button(
                        onClick = { viewModel.releaseSeat(seat.seatNumber) },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusWarning)
                    ) {
                        Text("सीट खाली करें")
                    }
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { viewModel.toggleBlockSeat(seat.seatNumber) }) {
                        Text(if (seat.status == "BLOCKED") "अनब्लॉक करें" else "सीट ब्लॉक करें", color = StatusError)
                    }
                    TextButton(onClick = { viewModel.selectSeat(null) }) {
                        Text("बंद करें")
                    }
                }
            }
        )
    }
}

@Composable
private fun SeatLegend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
                .border(1.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, color = TextSecondary)
    }
}

// 4. CHECK-IN SCANNER TAB
@Composable
private fun AdminCheckinTab(viewModel: NarmadaViewModel) {
    var scannerInput by remember { mutableStateOf("") }
    val checkinResult by viewModel.checkinResult.collectAsStateWithLifecycle()
    val allCheckins by viewModel.repository.allCheckins.collectAsStateWithLifecycle(emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "प्रवेश द्वार चेक-इन स्कैनर (Volunteer Scanner)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark
            )
            Text(
                text = "क्यूआर टोकन, पास आईडी (जैसे PAS27-000001) अथवा पंजीयन संख्या दर्ज/स्कैन करें।",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        // Scanner Input Box
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    NarmadaOutlinedTextField(
                        value = scannerInput,
                        onValueChange = { scannerInput = it },
                        label = { Text("पास आईडी अथवा क्यूआर कोड टोकन") },
                        placeholder = { Text("उदा. PAS27-000001 या NBF27-000001") },
                        leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = SaffronPrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_scanner_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.performCheckin(scannerInput) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_scanner_checkin_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "सत्यापित प्रवेश (CHECK IN)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Result Banner
        if (checkinResult != null) {
            item {
                when (val res = checkinResult!!) {
                    is NarmadaRepository.CheckinResult.Success -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = StatusSuccessBg),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, StatusSuccess)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(text = "✓ प्रवेश स्वीकृत (Check-in Successful)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StatusSuccess)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "भक्त का नाम: ${res.pass.devoteeName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "पास आईडी: ${res.pass.passId} | श्रेणी: ${res.pass.category}")
                                Text(text = "आवंटित सीट: ${res.pass.seatNumber.ifBlank { "सामान्य प्रवेश" }}", fontWeight = FontWeight.Bold, color = SaffronDark)
                                Text(text = "चेक-इन समय: ${res.checkinTime}", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }
                    is NarmadaRepository.CheckinResult.AlreadyCheckedIn -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = StatusWarningBg),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, StatusWarning)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = StatusWarning, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(text = "पहले से प्रवेशित (Already Checked In)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StatusWarning)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "यह पास पहले ही उपयोग किया जा चुका है।", color = TextPrimary)
                                Text(text = "भक्त: ${res.pass.devoteeName} (${res.pass.seatNumber})")
                                Text(text = "पूर्व चेक-इन समय: ${res.previousTime}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SaffronDark)
                            }
                        }
                    }
                    is NarmadaRepository.CheckinResult.InvalidOrCancelled -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = StatusErrorBg),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, StatusError)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "✕ प्रवेश अस्वीकृत (Entry Denied)", fontWeight = FontWeight.Bold, color = StatusError)
                                Text(text = res.reason, fontSize = 12.sp, color = TextPrimary)
                            }
                        }
                    }
                    NarmadaRepository.CheckinResult.NotFound -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = StatusErrorBg),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "✕ कोई वैध पास नहीं मिला।", fontWeight = FontWeight.Bold, color = StatusError)
                            }
                        }
                    }
                }
            }
        }

        // Recent Checkins
        item {
            Text(
                text = "हाल ही में हुए प्रवेश (${allCheckins.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(allCheckins.take(15)) { c ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = c.devoteeName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "पास: ${c.passId} | सीट: ${c.seatNumber}", fontSize = 11.sp, color = TextSecondary)
                    }
                    Text(
                        text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(c.checkinTimestamp)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NarmadaBlue
                    )
                }
            }
        }
    }
}

// 5. FINANCE TAB
@Composable
private fun AdminFinanceTab(viewModel: NarmadaViewModel) {
    val context = LocalContext.current
    val donations by viewModel.repository.allDonations.collectAsStateWithLifecycle(emptyList())
    val expenses by viewModel.repository.allExpenses.collectAsStateWithLifecycle(emptyList())
    val totalDonations by viewModel.repository.totalDonationsSum.collectAsStateWithLifecycle(0.0)
    val totalExpenses by viewModel.repository.totalExpensesSum.collectAsStateWithLifecycle(0.0)

    var selectedSubTab by remember { mutableStateOf("DONATIONS") } // DONATIONS, EXPENSES
    var showExpenseDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "महोत्सव वित्तीय लेखा (Financial Ledger)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SaffronDark)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = "कुल दान प्राप्ति", fontSize = 11.sp, color = TextMuted)
                        Text(text = "₹${"%,.0f".format(totalDonations ?: 0.0)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StatusSuccess)
                    }
                    Column {
                        Text(text = "कुल व्यय", fontSize = 11.sp, color = TextMuted)
                        Text(text = "₹${"%,.0f".format(totalExpenses ?: 0.0)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StatusError)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "शुद्ध शेष (Balance)", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = "₹${"%,.0f".format((totalDonations ?: 0.0) - (totalExpenses ?: 0.0))}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NarmadaBlue
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                FilterChip(
                    selected = selectedSubTab == "DONATIONS",
                    onClick = { selectedSubTab = "DONATIONS" },
                    label = { Text("दान पावती (${donations.size})", fontSize = 11.sp) },
                    modifier = Modifier.padding(end = 6.dp)
                )
                FilterChip(
                    selected = selectedSubTab == "EXPENSES",
                    onClick = { selectedSubTab = "EXPENSES" },
                    label = { Text("व्यय बिल (${expenses.size})", fontSize = 11.sp) }
                )
            }

            if (selectedSubTab == "EXPENSES") {
                Button(
                    onClick = { showExpenseDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("व्यय जोड़ें", fontSize = 11.sp)
                }
            } else {
                Button(
                    onClick = { viewModel.exportDonationsCsv(context, donations) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NarmadaBlue),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("CSV", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Ledger list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (selectedSubTab == "DONATIONS") {
                items(donations) { d ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = d.donorName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "${d.receiptNumber} | ${d.purpose} (${d.paymentMode})", fontSize = 10.sp, color = TextSecondary)
                                Text(text = "संग्रहकर्ता: ${d.collectorName} | ${d.date}", fontSize = 10.sp, color = TextMuted)
                            }
                            Text(
                                text = "₹${"%,.0f".format(d.amount)}",
                                fontWeight = FontWeight.ExtraBold,
                                color = StatusSuccess,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            } else {
                items(expenses) { exp ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = exp.category, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SaffronDark)
                                Text(text = exp.description, fontSize = 11.sp, color = TextPrimary)
                                Text(text = "भुगतान: ${exp.paidTo} (${exp.paymentMode}) | ${exp.date}", fontSize = 10.sp, color = TextMuted)
                            }
                            Text(
                                text = "₹${"%,.0f".format(exp.amount)}",
                                fontWeight = FontWeight.ExtraBold,
                                color = StatusError,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showExpenseDialog = false },
            onSubmit = { cat, desc, amt, to, mode, ref ->
                viewModel.recordExpense(cat, desc, amt, to, mode, ref)
                showExpenseDialog = false
            }
        )
    }
}

@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, Double, String, String, String) -> Unit
) {
    var category by remember { mutableStateOf("मंडप एवं पंडाल") }
    var desc by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var paidTo by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf("नकद") }
    var ref by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("नया व्यय दर्ज करें", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NarmadaOutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("श्रेणी (Category)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                NarmadaOutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("व्यय विवरण (Description)") },
                    modifier = Modifier.fillMaxWidth()
                )
                NarmadaOutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("राशि (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                NarmadaOutlinedTextField(
                    value = paidTo,
                    onValueChange = { paidTo = it },
                    label = { Text("किसको भुगतान किया (Paid To)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                NarmadaOutlinedTextField(
                    value = ref,
                    onValueChange = { ref = it },
                    label = { Text("बिल / वाउचर संदर्भ संख्या") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (desc.isNotBlank() && amt > 0) {
                        onSubmit(category, desc, amt, paidTo, mode, ref)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Text("सहेजें")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("रद्द करें") }
        }
    )
}

// 6. CMS & NOTICES TAB
@Composable
private fun AdminCmsTab(viewModel: NarmadaViewModel) {
    val cmsContent by viewModel.repository.allCmsContent.collectAsStateWithLifecycle(emptyList())
    val cmsMap = remember(cmsContent) { cmsContent.associate { it.key to it.value } }
    val notices by viewModel.repository.allNotices.collectAsStateWithLifecycle(emptyList())

    var newNoticeTitle by remember { mutableStateOf("") }
    var newNoticeDesc by remember { mutableStateOf("") }
    var isBannerNotice by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "वेबसाइट सामग्री प्रबंधन (CMS & Content)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark
            )
            Text(
                text = "बिना कोड बदले वेबसाइट के मुख्य शीर्षक, तिथियां एवं विवरण यहां से अपडेट करें।",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        // Editable CMS Fields
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "मूलभूत विवरण (Editable Titles):", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    CmsFieldEditor(
                        label = "आयोजन का नाम",
                        value = cmsMap["event_name"] ?: "",
                        onSave = { v -> viewModel.launchIO { viewModel.repository.setCmsValue("event_name", v, "Admin") } }
                    )

                    CmsFieldEditor(
                        label = "आयोजन तिथियां",
                        value = cmsMap["event_dates"] ?: "",
                        onSave = { v -> viewModel.launchIO { viewModel.repository.setCmsValue("event_dates", v, "Admin") } }
                    )

                    CmsFieldEditor(
                        label = "हेल्पलाइन मोबाइल",
                        value = cmsMap["contact_phone"] ?: "",
                        onSave = { v -> viewModel.launchIO { viewModel.repository.setCmsValue("contact_phone", v, "Admin") } }
                    )
                }
            }
        }

        // Add Notice Form
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "नई सूचना / घोषणा जारी करें", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SaffronDark)

                    NarmadaOutlinedTextField(
                        value = newNoticeTitle,
                        onValueChange = { newNoticeTitle = it },
                        label = { Text("सूचना का शीर्षक *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    NarmadaOutlinedTextField(
                        value = newNoticeDesc,
                        onValueChange = { newNoticeDesc = it },
                        label = { Text("विस्तृत विवरण *") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isBannerNotice, onCheckedChange = { isBannerNotice = it })
                        Text(text = "मुख्य पृष्ठ पर हाईलाइट बैनर के रूप में दिखाएं", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            if (newNoticeTitle.isNotBlank()) {
                                viewModel.launchIO {
                                    viewModel.repository.addNotice(
                                        NoticeItem(
                                            title = newNoticeTitle.trim(),
                                            description = newNoticeDesc.trim(),
                                            date = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date()),
                                            priority = if (isBannerNotice) "HIGH" else "NORMAL",
                                            isActive = true,
                                            isBanner = isBannerNotice
                                        ),
                                        user = "Admin"
                                    )
                                    newNoticeTitle = ""
                                    newNoticeDesc = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("सूचना प्रकाशित करें")
                    }
                }
            }
        }

        // Existing Notices
        item {
            Text(text = "सक्रिय सूचनाएं (${notices.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        items(notices) { notice ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = notice.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = notice.description, fontSize = 11.sp, color = TextSecondary)
                        Text(text = "दिनांक: ${notice.date} | प्राथमिकता: ${notice.priority}", fontSize = 10.sp, color = TextMuted)
                    }
                    IconButton(
                        onClick = {
                            viewModel.launchIO {
                                viewModel.repository.deleteNotice(notice, "Admin")
                            }
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "हटाएं", tint = StatusError)
                    }
                }
            }
        }
    }
}

@Composable
private fun CmsFieldEditor(
    label: String,
    value: String,
    onSave: (String) -> Unit
) {
    var text by remember(value) { mutableStateOf(value) }
    var isEditing by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (!isEditing) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, fontSize = 10.sp, color = TextMuted)
                Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            IconButton(onClick = { isEditing = true }) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = NarmadaBlue, modifier = Modifier.size(16.dp))
            }
        } else {
            NarmadaOutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(label) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    onSave(text)
                    isEditing = false
                }
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = StatusSuccess)
            }
        }
    }
}

// 7. AUDIT LOGS TAB
@Composable
private fun AdminAuditTab(viewModel: NarmadaViewModel) {
    val logs by viewModel.repository.allAuditLogs.collectAsStateWithLifecycle(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Text(
            text = "सिस्टम ऑडिट एवं सुरक्षा लॉग (Audit Logs)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = SaffronDark
        )
        Text(
            text = "सभी संवेदनशील गतिविधियों एवं परिवर्तनों का अपरिवर्तनीय रिकॉर्ड।",
            fontSize = 11.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(logs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NarmadaBlue)
                            Text(
                                text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(log.timestamp)),
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                        Text(text = "कर्ता: ${log.user} (${log.role}) | रिकॉर्ड: ${log.recordType} - ${log.recordId}", fontSize = 10.sp, color = TextSecondary)
                        if (log.newValue.isNotBlank()) {
                            Text(text = "परिवर्तन: ${log.newValue}", fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

// 8. SETTINGS & DEMO RESET TAB
@Composable
private fun AdminSettingsTab(viewModel: NarmadaViewModel) {
    var showResetConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "प्रशासनिक सेटिंग्स (Admin Settings)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "सिस्टम एवं प्रीफिक्स विन्यास", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "• पंजीयन संख्या प्रीफिक्स: NBF27-XXXXXX", fontSize = 12.sp, color = TextSecondary)
                    Text(text = "• पास संख्या प्रीफिक्स: PAS27-XXXXXX", fontSize = 12.sp, color = TextSecondary)
                    Text(text = "• रसीद संख्या प्रीफिक्स: NBF-DON-2027-XXXXXX", fontSize = 12.sp, color = TextSecondary)
                    Text(text = "• सुरक्षा टोकन: SHA-256 क्रिप्टोग्राफिक हैश", fontSize = 12.sp, color = TextSecondary)
                    Text(text = "• ऑटो सीट आवंटन: सक्रिय (निकटवर्ती समूह आवंटन सहित)", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = StatusWarningBg),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusWarning.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "डेमो डेटा री-सीड / रीसेट", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "मूल 20 भक्तों, 100 सीटों, 7 कार्यक्रमों, 5 दान पावती एवं सूचनाओं को पुनः री-लोड करें।",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showResetConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("डेमो डेटा रीसेट करें (Reset)")
                    }
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("पुष्टि करें", fontWeight = FontWeight.Bold) },
            text = { Text("क्या आप वास्तव में डेमो डेटा को रीसेट करना चाहते हैं?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reseedDemoData()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("हाँ, रीसेट करें")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("रद्द करें") }
            }
        )
    }
}
