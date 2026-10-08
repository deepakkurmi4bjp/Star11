package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DonationReceipt
import com.example.ui.NarmadaViewModel
import com.example.ui.components.NarmadaOutlinedTextField
import com.example.ui.components.OfficialReceiptCard
import com.example.ui.theme.*

@Composable
fun DonationScreen(
    viewModel: NarmadaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var donorName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("1100") }
    var paymentMode by remember { mutableStateOf("UPI / QR") }
    var purpose by remember { mutableStateOf("255 मी. चुनरी अर्पण सेवा") }
    var txId by remember { mutableStateOf("") }
    var generatedReceipt by remember { mutableStateOf<DonationReceipt?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val presetAmounts = listOf("501", "1100", "2100", "5100", "11000", "21000")
    val purposes = listOf(
        "255 मी. चुनरी अर्पण सेवा",
        "अखंड महाप्रसाद व भंडारा सेवा",
        "भव्य नर्मदा महाआरती सेवा",
        "पंडाल व व्यवस्था सहयोग",
        "सामान्य सेवा सहयोग"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "मां नर्मदा सेवा सहयोग एवं दान (Donation)",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark
            )
            Text(
                text = "जन्मोत्सव एवं चुनरी पदयात्रा के पावन सेवा कार्यों में अपना स्वैच्छिक योगदान दें एवं तत्काल आधिकारिक पावती प्राप्त करें।",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        if (generatedReceipt != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StatusSuccessBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "सहयोग राशि सफलतापूर्वक दर्ज हुई! आपकी रसीद तैयार है।",
                            fontWeight = FontWeight.Bold,
                            color = StatusSuccess,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            item {
                OfficialReceiptCard(
                    receipt = generatedReceipt!!,
                    onShare = {
                        viewModel.shareReceiptOnWhatsApp(context, generatedReceipt!!)
                    }
                )
            }

            item {
                Button(
                    onClick = {
                        generatedReceipt = null
                        donorName = ""
                        mobile = ""
                        amountText = "1100"
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("अन्य सहयोग दर्ज करें")
                }
            }
        } else {
            // Form Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (errorMessage != null) {
                            Text(text = errorMessage!!, color = StatusError, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        NarmadaOutlinedTextField(
                            value = donorName,
                            onValueChange = { donorName = it; errorMessage = null },
                            label = { Text("दानदाता का पूरा नाम *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("donation_name_input")
                        )

                        NarmadaOutlinedTextField(
                            value = mobile,
                            onValueChange = { if (it.length <= 10) mobile = it },
                            label = { Text("मोबाइल नंबर (पावती हेतु)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Preset Amounts
                        Text(text = "सहयोग राशि (Amount in ₹) *", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            presetAmounts.take(3).forEach { p ->
                                FilterChip(
                                    selected = amountText == p,
                                    onClick = { amountText = p },
                                    label = { Text("₹$p", fontSize = 12.sp) }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            presetAmounts.drop(3).forEach { p ->
                                FilterChip(
                                    selected = amountText == p,
                                    onClick = { amountText = p },
                                    label = { Text("₹$p", fontSize = 12.sp) }
                                )
                            }
                        }

                        NarmadaOutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text("राशि (Amount) दर्ज करें") },
                            leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Purpose Dropdown / Selector
                        Text(text = "सहयोग उद्देश्य (Purpose):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        purposes.forEach { pur ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                RadioButton(
                                    selected = purpose == pur,
                                    onClick = { purpose = pur }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = pur, fontSize = 12.sp)
                            }
                        }

                        // Payment mode
                        Text(text = "भुगतान माध्यम (Payment Mode):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("UPI / QR", "नकद (Cash)", "बैंक ट्रांसफर").forEach { m ->
                                FilterChip(
                                    selected = paymentMode == m,
                                    onClick = { paymentMode = m },
                                    label = { Text(m, fontSize = 11.sp) }
                                )
                            }
                        }

                        if (paymentMode != "नकद (Cash)") {
                            NarmadaOutlinedTextField(
                                value = txId,
                                onValueChange = { txId = it },
                                label = { Text("UPI / बैंक ट्रांजेक्शन यूटीआर (UTR Ref)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                if (donorName.isBlank()) {
                                    errorMessage = "कृपया दानदाता का नाम दर्ज करें।"
                                    return@Button
                                }
                                val amt = amountText.toDoubleOrNull() ?: 0.0
                                if (amt <= 0) {
                                    errorMessage = "कृपया वैध राशि दर्ज करें।"
                                    return@Button
                                }

                                viewModel.recordDonation(
                                    name = donorName,
                                    mobile = mobile,
                                    amount = amt,
                                    mode = paymentMode,
                                    purpose = purpose,
                                    txId = txId,
                                    onComplete = { r -> generatedReceipt = r }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("donation_submit_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "सहयोग पावती जारी करें", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
