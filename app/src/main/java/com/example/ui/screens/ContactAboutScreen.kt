package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FaqItem
import com.example.ui.NarmadaViewModel
import com.example.ui.theme.*

@Composable
fun ContactAboutScreen(
    viewModel: NarmadaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cmsContent by viewModel.repository.allCmsContent.collectAsStateWithLifecycle(emptyList())
    val cmsMap = remember(cmsContent) { cmsContent.associate { it.key to it.value } }
    val faqs by viewModel.repository.allFaqs.collectAsStateWithLifecycle(emptyList())

    val orgName = cmsMap["org_name"] ?: "श्री मां नर्मदा भक्त परिवार"
    val contactPhone = cmsMap["contact_phone"] ?: "7440771076"
    val contactWhatsapp = cmsMap["contact_whatsapp"] ?: "7440771076"
    val contactEmail = cmsMap["contact_email"] ?: "Deepak53802@gmail.com"
    val contactAddress = cmsMap["contact_address"] ?: "सांकला माता मंदिर, तारादेही, तहसील तेंदूखेड़ा, जिला दमोह (म.प्र.)"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Text(
                text = "आयोजन परिचय एवं संपर्क (About & Contact)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark
            )
        }

        // About Organization & Event
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "श्री मां नर्मदा जन्मोत्सव 2027",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = cmsMap["about_event"] ?: "मां रेवा के जन्मोत्सव के पावन अवसर पर श्री मां नर्मदा भक्त परिवार द्वारा 11 से 13 फ़रवरी 2027 तक भव्य तीन दिवसीय जन्मोत्सव एवं 255 मीटर की अखंड चुनरी पदयात्रा का महाआयोजन किया जा रहा है।",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "255 मीटर अखंड चुनरी पदयात्रा",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NarmadaBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = cmsMap["about_chunri"] ?: "नर्मदा तट पर लाखों भक्तों द्वारा मां नर्मदा को श्रद्धापूर्वक 255 मीटर की सुसज्जित चुनरी अर्पित की जाएगी। यह यात्रा भक्तों के सामूहिक संकल्प और भक्ति का अद्वितीय संगम है।",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Organization Center Info Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SaffronContainer.copy(alpha = 0.35f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = SaffronDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "मुख्य केंद्र: तेंदूखेड़ा, जिला दमोह (म.प्र.)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "हमारा मुख्य कार्यालय सांकला माता मंदिर, तारादेही, तहसील तेंदूखेड़ा, जिला दमोह में स्थित है। इस पावन चुनरी पदयात्रा एवं जन्मोत्सव में मुख्य केंद्र तेंदूखेड़ा-दमोह के साथ-साथ आसपास के सभी पड़ोसी जिलों (जबलपुर, नरसिंहपुर, सागर, कटनी, नर्मदापुरम, पन्ना आदि) के लाखों श्रद्धालु श्रद्धापूर्वक सम्मिलित होते हैं।",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Contact Info Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "कार्यालय एवं संपर्क सूत्र (Contact Details)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDark
                    )

                    ContactRow(
                        icon = Icons.Default.LocationOn,
                        title = "पता (Address)",
                        value = contactAddress
                    )

                    ContactRow(
                        icon = Icons.Default.Phone,
                        title = "हेल्पलाइन नंबर",
                        value = contactPhone,
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$contactPhone"))
                            context.startActivity(intent)
                        }
                    )

                    ContactRow(
                        icon = Icons.Default.Chat,
                        title = "WhatsApp सहायता",
                        value = contactWhatsapp,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=${contactWhatsapp.replace(" ", "")}"))
                            context.startActivity(intent)
                        }
                    )

                    ContactRow(
                        icon = Icons.Default.Email,
                        title = "ईमेल (Email)",
                        value = contactEmail
                    )
                }
            }
        }

        // FAQs Section
        item {
            Text(
                text = "अक्सर पूछे जाने वाले प्रश्न (FAQ)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(faqs) { faq ->
            FaqAccordionItem(faq = faq)
        }
    }
}

@Composable
private fun ContactRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SaffronContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 11.sp, color = TextMuted)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
    }
}

@Composable
private fun FaqAccordionItem(faq: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = SaffronPrimary
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    HorizontalDivider(color = SurfaceCardBorder)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = faq.answer,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
