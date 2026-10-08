package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GroupMember
import com.example.ui.AppScreen
import com.example.ui.NarmadaViewModel
import com.example.ui.components.NarmadaOutlinedTextField
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationWizardScreen(
    viewModel: NarmadaViewModel,
    modifier: Modifier = Modifier
) {
    val draft by viewModel.wizardDraft.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
    ) {
        // Wizard Step Progress Indicator
        WizardStepIndicator(currentStep = draft.step)

        // Error or Duplicate Warning Banner
        if (draft.errorMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = StatusErrorBg),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = StatusError)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = draft.errorMessage ?: "",
                            color = StatusError,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (draft.duplicateWarning != null) {
                            TextButton(
                                onClick = {
                                    viewModel.searchRegistration(draft.duplicateWarning?.registrationId ?: "")
                                    viewModel.navigateTo(AppScreen.MY_REGISTRATION)
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "पंजीयन देखें (View Existing Registration) →",
                                    color = NarmadaBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Form Content based on Step
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (draft.step) {
                1 -> Step1Personal(draft = draft, viewModel = viewModel)
                2 -> Step2Address(draft = draft, viewModel = viewModel)
                3 -> Step3Emergency(draft = draft, viewModel = viewModel)
                4 -> Step4Participation(draft = draft, viewModel = viewModel)
                5 -> Step5GroupMembers(draft = draft, viewModel = viewModel)
                6 -> Step6Review(draft = draft, viewModel = viewModel)
            }
        }

        // Bottom Navigation Buttons
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = PureWhite
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (draft.step > 1) {
                    OutlinedButton(
                        onClick = { viewModel.prevWizardStep() },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "पिछला (Back)")
                    }
                } else {
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "रद्द करें")
                    }
                }

                if (draft.step < 6) {
                    Button(
                        onClick = { viewModel.nextWizardStep() },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("wizard_next_btn")
                    ) {
                        Text(text = "आगे बढ़ें (Next)", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Button(
                        onClick = { viewModel.submitRegistration() },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("wizard_submit_btn")
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "पंजीयन पूर्ण करें (Submit)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun WizardStepIndicator(currentStep: Int) {
    val steps = listOf("व्यक्तिगत", "पता", "आपातकालीन", "सहभागिता", "सदस्य", "पुष्टि")
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = PureWhite,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "चरण $currentStep / 6: ${steps[currentStep - 1]}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaffronDark
                )
                Text(
                    text = "${(currentStep * 100) / 6}% पूर्ण",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { currentStep / 6f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = SaffronPrimary,
                trackColor = SaffronContainer
            )
        }
    }
}

@Composable
private fun Step1Personal(draft: com.example.ui.WizardDraft, viewModel: NarmadaViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "चरण 1: व्यक्तिगत जानकारी (Personal Information)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark
            )
        }
        item {
            NarmadaOutlinedTextField(
                value = draft.fullName,
                onValueChange = { v -> viewModel.updateDraft { it.copy(fullName = v, errorMessage = null) } },
                label = { Text("भक्त का पूरा नाम (Full Name) *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_full_name"),
                singleLine = true
            )
        }
        item {
            NarmadaOutlinedTextField(
                value = draft.fatherHusbandName,
                onValueChange = { v -> viewModel.updateDraft { it.copy(fatherHusbandName = v) } },
                label = { Text("पिता / पति का नाम (Father/Husband Name)") },
                leadingIcon = { Icon(Icons.Default.FamilyRestroom, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "लिंग (Gender)", fontSize = 12.sp, color = TextSecondary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        listOf("पुरुष", "महिला").forEach { g ->
                            FilterChip(
                                selected = draft.gender == g,
                                onClick = { viewModel.updateDraft { it.copy(gender = g) } },
                                label = { Text(g, fontSize = 12.sp) },
                                modifier = Modifier.padding(end = 6.dp)
                            )
                        }
                    }
                }
                NarmadaOutlinedTextField(
                    value = draft.age,
                    onValueChange = { v -> if (v.length <= 3) viewModel.updateDraft { it.copy(age = v) } },
                    label = { Text("आयु (Age)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.7f),
                    singleLine = true
                )
            }
        }
        item {
            NarmadaOutlinedTextField(
                value = draft.mobileNumber,
                onValueChange = { v ->
                    if (v.length <= 10) viewModel.updateDraft { it.copy(mobileNumber = v, errorMessage = null) }
                },
                label = { Text("मोबाइल नंबर (10 अंक) *") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_mobile"),
                singleLine = true
            )
        }
        item {
            NarmadaOutlinedTextField(
                value = draft.whatsappNumber,
                onValueChange = { v -> viewModel.updateDraft { it.copy(whatsappNumber = v) } },
                label = { Text("WhatsApp नंबर (पास प्राप्त करने हेतु)") },
                leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            NarmadaOutlinedTextField(
                value = draft.altMobileNumber,
                onValueChange = { v -> viewModel.updateDraft { it.copy(altMobileNumber = v) } },
                label = { Text("वैकल्पिक मोबाइल नंबर (Alternate Mobile)") },
                leadingIcon = { Icon(Icons.Default.PhoneIphone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
private fun Step2Address(draft: com.example.ui.WizardDraft, viewModel: NarmadaViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "चरण 2: पता विवरण (Address Information)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Surface(
                color = SaffronContainer.copy(alpha = 0.35f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🚩 मुख्य केंद्र: तेंदूखेड़ा (दमोह)। दमोह सहित आसपास के सभी जिलों (जबलपुर, नरसिंहपुर, सागर, कटनी आदि) के भक्त सादर आमंत्रित हैं।",
                    fontSize = 11.sp,
                    color = SaffronDark,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        item {
            NarmadaOutlinedTextField(
                value = draft.village,
                onValueChange = { v -> viewModel.updateDraft { it.copy(village = v, errorMessage = null) } },
                label = { Text("ग्राम / नगर (Village / Town) *") },
                leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            NarmadaOutlinedTextField(
                value = draft.address,
                onValueChange = { v -> viewModel.updateDraft { it.copy(address = v) } },
                label = { Text("गली / मोहल्ला / मकान नंबर (Street/Address)") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NarmadaOutlinedTextField(
                    value = draft.post,
                    onValueChange = { v -> viewModel.updateDraft { it.copy(post = v) } },
                    label = { Text("डाकघर (Post)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                NarmadaOutlinedTextField(
                    value = draft.gramPanchayat,
                    onValueChange = { v -> viewModel.updateDraft { it.copy(gramPanchayat = v) } },
                    label = { Text("ग्राम पंचायत") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NarmadaOutlinedTextField(
                    value = draft.tehsil,
                    onValueChange = { v -> viewModel.updateDraft { it.copy(tehsil = v) } },
                    label = { Text("तहसील (Tehsil)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                NarmadaOutlinedTextField(
                    value = draft.district,
                    onValueChange = { v -> viewModel.updateDraft { it.copy(district = v) } },
                    label = { Text("जिला (District) *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NarmadaOutlinedTextField(
                    value = draft.state,
                    onValueChange = { v -> viewModel.updateDraft { it.copy(state = v) } },
                    label = { Text("राज्य (State)") },
                    modifier = Modifier.weight(1.2f),
                    singleLine = true
                )
                NarmadaOutlinedTextField(
                    value = draft.pinCode,
                    onValueChange = { v -> if (v.length <= 6) viewModel.updateDraft { it.copy(pinCode = v) } },
                    label = { Text("पिन कोड (PIN)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.8f),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
private fun Step3Emergency(draft: com.example.ui.WizardDraft, viewModel: NarmadaViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "चरण 3: आपातकालीन संपर्क एवं पहचान (Emergency & ID)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark
            )
            Text(
                text = "यात्रा में श्रद्धालुओं की सुरक्षा एवं सुविधा हेतु आवश्यक जानकारी।",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
        item {
            NarmadaOutlinedTextField(
                value = draft.emergencyContactName,
                onValueChange = { v -> viewModel.updateDraft { it.copy(emergencyContactName = v) } },
                label = { Text("आपातकालीन संपर्क व्यक्ति का नाम") },
                leadingIcon = { Icon(Icons.Default.ContactPhone, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            NarmadaOutlinedTextField(
                value = draft.emergencyContactNumber,
                onValueChange = { v -> viewModel.updateDraft { it.copy(emergencyContactNumber = v) } },
                label = { Text("आपातकालीन मोबाइल नंबर") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            NarmadaOutlinedTextField(
                value = draft.emergencyRelationship,
                onValueChange = { v -> viewModel.updateDraft { it.copy(emergencyRelationship = v) } },
                label = { Text("संबंध (Relationship: जैसे भाई, पुत्र, मित्र)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NarmadaBlueContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "पहचान संदर्भ (ऐच्छिक - Optional)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NarmadaBlue
                    )
                    Text(
                        text = "गोपनीयता नीति अनुसार कोई भी संवेदनशील आईडी डाटा स्टोर नहीं किया जाता।",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NarmadaOutlinedTextField(
                        value = draft.idProofLast4,
                        onValueChange = { v -> if (v.length <= 4) viewModel.updateDraft { it.copy(idProofLast4 = v) } },
                        label = { Text("आधार कार्ड के अंतिम 4 अंक (Last 4 Digits)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }
    }
}

@Composable
private fun Step4Participation(draft: com.example.ui.WizardDraft, viewModel: NarmadaViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "चरण 4: यात्रा एवं सहभागिता विवरण (Participation Details)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SaffronContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "क्या आप चुनरी पदयात्रा में भाग लेंगे?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = SaffronDark
                        )
                        Text(
                            text = "12 फ़रवरी को 255 मीटर अखंड चुनरी अर्पण",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = draft.isChunriYatraParticipant,
                        onCheckedChange = { v -> viewModel.updateDraft { it.copy(isChunriYatraParticipant = v) } }
                    )
                }
            }
        }
        item {
            Text(text = "उपस्थिति की प्राथमिकता दिनांक", fontSize = 12.sp, color = TextSecondary)
            val dates = listOf(
                "11-13 फ़रवरी 2027 (तीनों दिन)",
                "11 फ़रवरी (शुभारंभ व कलश यात्रा)",
                "12 फ़रवरी (चुनरी पदयात्रा व महाआरती)",
                "13 फ़रवरी (जन्मोत्सव व महाप्रसाद)"
            )
            dates.forEach { d ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.updateDraft { it.copy(preferredDate = d) } }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = draft.preferredDate == d,
                        onClick = { viewModel.updateDraft { it.copy(preferredDate = d) } }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = d, fontSize = 12.sp, fontWeight = if (draft.preferredDate == d) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        item {
            NarmadaOutlinedTextField(
                value = draft.groupFamilyName,
                onValueChange = { v -> viewModel.updateDraft { it.copy(groupFamilyName = v) } },
                label = { Text("परिवार / समूह का नाम (Group/Family Name)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            NarmadaOutlinedTextField(
                value = draft.villageOrganization,
                onValueChange = { v -> viewModel.updateDraft { it.copy(villageOrganization = v) } },
                label = { Text("मंडल / संस्था का नाम (Organization/Mandal)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            Text(text = "विशेष व्यवस्था आवश्यकताएं:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                FilterChip(
                    selected = draft.isFoodRequired,
                    onClick = { viewModel.updateDraft { it.copy(isFoodRequired = !it.isFoodRequired) } },
                    label = { Text("महाप्रसाद (Food)") }
                )
                FilterChip(
                    selected = draft.isTransportRequired,
                    onClick = { viewModel.updateDraft { it.copy(isTransportRequired = !it.isTransportRequired) } },
                    label = { Text("परिवहन (Transport)") }
                )
                FilterChip(
                    selected = draft.isAccommodationRequired,
                    onClick = { viewModel.updateDraft { it.copy(isAccommodationRequired = !it.isAccommodationRequired) } },
                    label = { Text("विश्राम (Stay)") }
                )
            }
        }
        item {
            NarmadaOutlinedTextField(
                value = draft.specialRequirements,
                onValueChange = { v -> viewModel.updateDraft { it.copy(specialRequirements = v) } },
                label = { Text("अन्य कोई विशेष टिप्पणी या आवश्यकता") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )
        }
    }
}

@Composable
private fun Step5GroupMembers(draft: com.example.ui.WizardDraft, viewModel: NarmadaViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "चरण 5: परिवार / समूह के अतिरिक्त सदस्य (Family Members)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark
            )
            Text(
                text = "एक ही पंजीयन में परिवार के अन्य सदस्यों को जोड़ें। सभी को साथ बैठने हेतु निकटवर्ती सीटें आवंटित की जाएंगी।",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        item {
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = NarmadaBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "नया सदस्य जोड़ें (Add Member)")
            }
        }

        if (draft.additionalMembers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.GroupAdd, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "यदि आप अकेले आ रहे हैं, तो सीधे 'आगे बढ़ें' पर क्लिक करें।",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            itemsIndexed(draft.additionalMembers) { index, member ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SaffronContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "${index + 1}", fontWeight = FontWeight.Bold, color = SaffronDark)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = member.memberName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "संबंध: ${member.relationship} | आयु: ${member.age} वर्ष | ${member.gender}", fontSize = 11.sp, color = TextSecondary)
                        }
                        IconButton(onClick = { viewModel.removeFamilyMember(index) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "हटाएं", tint = StatusError)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMemberDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { member ->
                viewModel.addFamilyMember(member)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddMemberDialog(
    onDismiss: () -> Unit,
    onAdd: (GroupMember) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var rel by remember { mutableStateOf("पत्नी") }
    var age by remember { mutableStateOf("28") }
    var gender by remember { mutableStateOf("महिला") }
    var mobile by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "परिवार / समूह सदस्य जोड़ें", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                NarmadaOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("सदस्य का पूरा नाम *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                NarmadaOutlinedTextField(
                    value = rel,
                    onValueChange = { rel = it },
                    label = { Text("संबंध (जैसे पत्नी, पुत्र, माता, मित्र)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NarmadaOutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("आयु") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "लिंग", fontSize = 11.sp)
                        Row {
                            listOf("पुरुष", "महिला").forEach { g ->
                                FilterChip(
                                    selected = gender == g,
                                    onClick = { gender = g },
                                    label = { Text(g, fontSize = 11.sp) },
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                        }
                    }
                }
                NarmadaOutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("मोबाइल नंबर (ऐच्छिक)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(
                            GroupMember(
                                groupId = "",
                                individualRegistrationId = "",
                                memberName = name.trim(),
                                relationship = rel.trim(),
                                age = age.toIntOrNull() ?: 25,
                                gender = gender,
                                mobile = mobile.trim()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Text("जोड़ें (Add)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("रद्द करें") }
        }
    )
}

@Composable
private fun Step6Review(draft: com.example.ui.WizardDraft, viewModel: NarmadaViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "चरण 6: समीक्षा एवं अंतिम पुष्टि (Review & Submit)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark
            )
            Text(
                text = "कृपया विवरण की पुष्टि करें। 'पंजीयन पूर्ण करें' दबाते ही डिजिटल पास एवं सीट स्वतः आवंटित हो जाएगी।",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, SacredGold)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "॥ श्री मां नर्मदा जन्मोत्सव 2027 ॥",
                        color = SaffronDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ReviewRow("मुख्य भक्त", draft.fullName)
                    ReviewRow("पिता/पति", draft.fatherHusbandName.ifBlank { "-" })
                    ReviewRow("मोबाइल नंबर", draft.mobileNumber)
                    ReviewRow("ग्राम / जिला", "${draft.village}, ${draft.district}")
                    ReviewRow("चुनरी पदयात्रा", if (draft.isChunriYatraParticipant) "हाँ (सक्रिय सहभागिता)" else "नहीं")
                    ReviewRow("उपस्थिति दिनांक", draft.preferredDate)
                    ReviewRow("साथी सदस्य संख्या", "${draft.additionalMembers.size} सदस्य")
                    ReviewRow("महाप्रसाद व्यवस्था", if (draft.isFoodRequired) "आवश्यक" else "नहीं")
                }
            }
        }

        if (draft.additionalMembers.isNotEmpty()) {
            item {
                Text(text = "समूह सदस्य सूची:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                draft.additionalMembers.forEachIndexed { i, m ->
                    Text(
                        text = "${i + 1}. ${m.memberName} (${m.relationship}, ${m.age} वर्ष)",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
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
