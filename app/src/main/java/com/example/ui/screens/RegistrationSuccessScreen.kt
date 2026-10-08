package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreen
import com.example.ui.NarmadaViewModel
import com.example.ui.components.DevoteePassCard
import com.example.ui.theme.*

@Composable
fun RegistrationSuccessScreen(
    viewModel: NarmadaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lastRegistered by viewModel.lastRegistered.collectAsStateWithLifecycle()

    val devotee = lastRegistered?.first
    val pass = lastRegistered?.second

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StatusSuccessBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = StatusSuccess,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "पंजीयन सफल!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = StatusSuccess
                )
                Text(
                    text = "मां नर्मदा जन्मोत्सव 2027 में आपका स्वागत है।",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = PureWhite,
                    shape = RoundedCornerShape(10.dp),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = "पंजीयन ID: ${devotee?.registrationId ?: "NBF27-XXXXXX"}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NarmadaBlue,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Automatic Credentials Card for Devotee
        val credentials by viewModel.lastRegisteredCredentials.collectAsStateWithLifecycle()
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, SaffronPrimary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = null,
                        tint = SaffronPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "आपका स्वतः निर्मित लॉगिन क्रेडेंशियल",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SaffronDark
                        )
                        Text(
                            text = "भविष्य में पास देखने व अपडेट हेतु सुरक्षित रखें",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                    Surface(
                        color = StatusSuccessBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "स्वचालित (Auto)",
                            color = StatusSuccess,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = WarmIvory,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "यूज़र ID (पंजीयन संख्या)", fontSize = 10.sp, color = TextSecondary)
                                Text(
                                    text = credentials?.username ?: (devotee?.registrationId ?: "NBF27-XXXXXX"),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NarmadaBlue
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "पंजीकृत मोबाइल", fontSize = 10.sp, color = TextSecondary)
                                Text(
                                    text = devotee?.mobileNumber ?: "",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        HorizontalDivider(color = SurfaceCardBorder)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "लॉगिन पासवर्ड (Auto-Generated Password)", fontSize = 10.sp, color = TextSecondary)
                                Text(
                                    text = credentials?.password ?: "Narmada@1234",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SaffronDark,
                                    letterSpacing = 1.sp
                                )
                            }
                            Button(
                                onClick = {
                                    val textToCopy = "पंजीयन ID / यूज़रनेम: ${credentials?.username ?: devotee?.registrationId}\nपासवर्ड: ${credentials?.password}\nमोबाइल: ${devotee?.mobileNumber}"
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(textToCopy))
                                    Toast.makeText(context, "क्रेडेंशियल कॉपी कर लिया गया!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronContainer),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = SaffronDark, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "कॉपी करें", color = SaffronDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (credentials != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            viewModel.loginAsDevotee(credentials!!)
                            Toast.makeText(context, "स्वागत है! आप लॉगिन हो चुके हैं।", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NarmadaBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "इस क्रेडेंशियल से तुरंत लॉगिन करें", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Digital Pass Card
        if (devotee != null && pass != null) {
            DevoteePassCard(
                devotee = devotee,
                pass = pass,
                onShareWhatsApp = {
                    val passMsg = """
*॥ श्री नर्मदे हर ॥*
*श्री मां नर्मदा भक्त परिवार — तेंदूखेड़ा (दमोह)*
सांकला माता मंदिर, तारादेही, तहसील तेंदूखेड़ा, जिला दमोह (म.प्र.)
हेल्पलाइन: 7440771076

*आपका पंजीयन एवं लॉगिन क्रेडेंशियल:*
नाम: ${devotee.fullName}
पंजीयन ID / यूज़रनेम: ${credentials?.username ?: devotee.registrationId}
लॉगिन पासवर्ड: ${credentials?.password ?: "Narmada@1234"}
आवंटित सीट: ${if (pass.seatNumber.isNotBlank()) pass.seatNumber else "सामान्य प्रवेश"}
पास ID: ${pass.passId}
दिनांक: 11–13 फरवरी 2027
_मां नर्मदा की कृपा आप पर सदा बनी रहे।_
                    """.trimIndent()
                    val sendIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, passMsg)
                        type = "text/plain"
                    }
                    context.startActivity(android.content.Intent.createChooser(sendIntent, "क्रेडेंशियल व पास साझा करें"))
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    Toast.makeText(context, "पास पीडीएफ आपके डिवाइस में सुरक्षित कर लिया गया है।", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "डाउनलोड पास")
            }

            Button(
                onClick = {
                    viewModel.navigateTo(AppScreen.HOME)
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("success_home_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "मुख्य पृष्ठ")
            }
        }
    }
}
