package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.EventProgram
import com.example.ui.NarmadaViewModel
import com.example.ui.theme.*

@Composable
fun ProgramScheduleScreen(
    viewModel: NarmadaViewModel,
    modifier: Modifier = Modifier
) {
    val programs by viewModel.repository.allPrograms.collectAsStateWithLifecycle(emptyList())

    val dateOptions = listOf("सभी दिन", "11 फ़रवरी 2027", "12 फ़रवरी 2027", "13 फ़रवरी 2027")
    var selectedDate by remember { mutableStateOf("सभी दिन") }

    val filteredPrograms = remember(programs, selectedDate) {
        if (selectedDate == "सभी दिन") programs
        else programs.filter { it.date == selectedDate }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "महोत्सव कार्यक्रम सारिणी (Schedule)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark
            )
            Text(
                text = "11 से 13 फ़रवरी 2027 — मां नर्मदा जन्मोत्सव एवं चुनरी पदयात्रा कार्यक्रम",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        // Date Filter Pills
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(dateOptions) { date ->
                    FilterChip(
                        selected = selectedDate == date,
                        onClick = { selectedDate = date },
                        label = {
                            Text(
                                text = date,
                                fontWeight = if (selectedDate == date) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
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

        // Programs List
        if (filteredPrograms.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(text = "इस तिथि के लिए कोई कार्यक्रम निर्धारित नहीं है।", color = TextSecondary)
                    }
                }
            }
        } else {
            items(filteredPrograms) { program ->
                ProgramCard(program = program)
            }
        }
    }
}

@Composable
private fun ProgramCard(program: EventProgram) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = SaffronContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = program.category,
                        color = SaffronDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = NarmadaBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${program.date} | ${program.time}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NarmadaBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = program.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = program.venue,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = program.description,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )

            if (program.chiefGuest.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = SacredGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "पावन सानिध्य: ${program.chiefGuest}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
