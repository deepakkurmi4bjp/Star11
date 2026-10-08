package com.example.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Cartoon-themed Maa Narmada Illustration & Artwork Composable.
 * Features animated glowing halo, divine blessings, vibrant sacred colors,
 * lotus seat, and river ripples.
 */
@Composable
fun CartoonMaaNarmadaArtwork(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    showAuraAnimation: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AuraAnimation")
    val pulseScale by if (showAuraAnimation) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    Box(
        modifier = modifier
            .size(size)
            .scale(pulseScale),
        contentAlignment = Alignment.Center
    ) {
        // Glowing Outer Radial Ring
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            SacredGold.copy(alpha = 0.35f),
                            SaffronPrimary.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Vector Cartoon Illustration
        Image(
            painter = painterResource(id = R.drawable.ic_cartoon_maa_narmada),
            contentDescription = "बाल / कार्टून रूप मां नर्मदा",
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * Dedicated Cartoon Theme Hero Card for Maa Narmada.
 * Provides a joyful, friendly devotional visual for devotees.
 */
@Composable
fun CartoonMaaNarmadaThemeCard(
    modifier: Modifier = Modifier,
    onUploadPhotoClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(SaffronPrimary, SacredGold, Color(0xFFFF8A65))))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cartoon Maa Narmada Illustration Avatar
                CartoonMaaNarmadaArtwork(
                    size = 72.dp,
                    showAuraAnimation = true
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = Color(0xFFFFE0B2),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "🌸 नवीन कार्टून थीम दर्शन",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "॥ त्वदीय पाद पंकजं नमामि देवि नर्मदे ॥",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = "बाल एवं कार्टून स्वरूप मां नर्मदा - पावन जल, दीप एवं कमल पुष्प संग",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }

            if (onUploadPhotoClick != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = SaffronContainer.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onUploadPhotoClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "सीधे फोन से अपनी फोटो या लोगो लगाएं",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Modern Direct Photo Uploader Component.
 * Uses Android Photo Picker (zero-permission ActivityResultContracts.PickVisualMedia).
 * Persists selected photo into local app files directory so it permanently survives reboots.
 */
@Composable
fun DirectPhotoUploaderSection(
    currentPhotoUrl: String,
    onPhotoSelected: (String) -> Unit,
    onPhotoRemoved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploading = true
            uploadError = null
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        val fileName = "custom_logo_${System.currentTimeMillis()}.jpg"
                        val destFile = File(context.filesDir, fileName)
                        destFile.outputStream().use { output ->
                            inputStream.copyTo(output)
                        }
                        val localFileUri = Uri.fromFile(destFile).toString()
                        withContext(Dispatchers.Main) {
                            onPhotoSelected(localFileUri)
                            isUploading = false
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            uploadError = "फोटो लोड नहीं हो सकी, कृपया पुनः प्रयास करें।"
                            isUploading = false
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        uploadError = "त्रुटि: ${e.localizedMessage ?: "फोटो सहेजने में विफल"}"
                        isUploading = false
                    }
                }
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.5.dp, if (currentPhotoUrl.isNotBlank()) SaffronPrimary else SaffronContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📸 सीधे फोन की गैलरी से फोटो अपलोड करें",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark
            )
            Text(
                text = "किसी URL की आवश्यकता नहीं — 1-क्लिक में अपने फोन से फोटो चुनें",
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (currentPhotoUrl.isNotBlank()) {
                // Photo is selected: Show preview & actions
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(
                            3.dp,
                            Brush.sweepGradient(listOf(SacredGold, SaffronPrimary, SacredCrimson, SacredGold)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = currentPhotoUrl,
                        contentDescription = "अपलोड की गई फोटो",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "आपकी फोटो सफलतापूर्वक चुनी गई है",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusSuccess
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SaffronPrimary)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("फोटो बदलें", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onPhotoRemoved,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("हटाएं", fontSize = 12.sp)
                    }
                }
            } else {
                // No photo yet: prominent upload CTA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SaffronContainer.copy(alpha = 0.35f))
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(SaffronPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(
                                    color = PureWhite,
                                    modifier = Modifier.size(26.dp),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "फोटो अपलोड करें",
                                    tint = PureWhite,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isUploading) "फोटो लोड हो रही है..." else "यहाँ दबाकर गैलरी से फोटो चुनें",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark
                        )

                        Text(
                            text = "PNG, JPG या JPEG फोटो (गैलरी से चयन करें)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            AnimatedVisibility(visible = uploadError != null) {
                uploadError?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = err,
                        color = StatusError,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
