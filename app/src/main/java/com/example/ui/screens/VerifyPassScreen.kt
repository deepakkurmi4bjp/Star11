package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.NarmadaViewModel
import com.example.ui.components.NarmadaOutlinedTextField
import com.example.ui.theme.*

@Composable
fun VerifyPassScreen(
    viewModel: NarmadaViewModel,
    modifier: Modifier = Modifier
) {
    var passQueryInput by remember { mutableStateOf("") }
    val result by viewModel.passVerificationResult.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "डिजिटल पास सत्यापन (Verify Pass)",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark
            )
            Text(
                text = "पास आईडी (जैसे PAS27-000001) अथवा क्यूआर सुरक्षा कोड दर्ज करके आधिकारिक पास की प्रामाणिकता जांचें।",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        // Search Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    NarmadaOutlinedTextField(
                        value = passQueryInput,
                        onValueChange = { passQueryInput = it },
                        label = { Text("पास आईडी अथवा क्यूआर टोकन") },
                        placeholder = { Text("उदाहरण: PAS27-000001") },
                        leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = SaffronPrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("verify_pass_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { viewModel.verifyPass(passQueryInput) })
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.verifyPass(passQueryInput) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("verify_pass_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Verified, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "पास सत्यापित करें", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Result Card
        if (result != null) {
            val r = result!!
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (r.isValid) StatusSuccess else StatusError
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (r.isValid) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (r.isValid) StatusSuccess else StatusError,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (r.isValid) "✓ आधिकारिक पास (Official Pass)" else "✕ अमान्य / रद्द पास",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (r.isValid) StatusSuccess else StatusError
                                )
                                Text(
                                    text = "श्री मां नर्मदा भक्त परिवार",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = SurfaceCardBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        if (r.isValid) {
                            PassDetailRow("भक्त का नाम", r.devoteeName)
                            PassDetailRow("पास आईडी", r.passId)
                            PassDetailRow("पंजीयन संख्या", r.registrationId)
                            PassDetailRow("पास श्रेणी", r.category)
                            PassDetailRow("सीट विवरण", r.seatNumber)
                            PassDetailRow("स्थान (Venue)", r.venue)
                            PassDetailRow("वैधता", r.eventDates)
                            PassDetailRow("स्थिति", r.status)
                        } else {
                            Text(
                                text = r.message,
                                fontSize = 13.sp,
                                color = StatusError,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PassDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = TextMuted)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}
