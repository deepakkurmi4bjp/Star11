package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.NarmadaViewModel
import com.example.ui.components.DevoteePassCard
import com.example.ui.components.NarmadaOutlinedTextField
import com.example.ui.theme.*

@Composable
fun MyRegistrationScreen(
    viewModel: NarmadaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var queryInput by remember { mutableStateOf("") }

    val devotee by viewModel.searchResultDevotee.collectAsStateWithLifecycle()
    val pass by viewModel.searchResultPass.collectAsStateWithLifecycle()
    val members by viewModel.searchResultMembers.collectAsStateWithLifecycle()
    val hasSearched by viewModel.searchHasSearched.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "अपना पंजीयन देखें एवं पास प्राप्त करें",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronDark
            )
            Text(
                text = "पंजीयन संख्या (जैसे NBF27-000001) अथवा अपना 10 अंकों का पंजीकृत मोबाइल नंबर दर्ज करें।",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        // Search Box
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    NarmadaOutlinedTextField(
                        value = queryInput,
                        onValueChange = { queryInput = it },
                        label = { Text("पंजीयन संख्या अथवा मोबाइल नंबर") },
                        placeholder = { Text("उदाहरण: 9826012345 या NBF27-000001") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SaffronPrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("my_reg_search_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { viewModel.searchRegistration(queryInput) })
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.searchRegistration(queryInput) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("my_reg_search_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "पंजीयन खोजें (Search)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Search Results
        if (hasSearched) {
            if (devotee == null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StatusWarningBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.SearchOff, contentDescription = null, tint = StatusWarning, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "कोई पंजीयन नहीं मिला",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "दर्ज किया गया विवरण सही नहीं है। कृपया मोबाइल नंबर या पंजीयन संख्या पुनः जांचें।",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                item {
                    if (pass != null) {
                        DevoteePassCard(
                            devotee = devotee!!,
                            pass = pass!!,
                            onShareWhatsApp = {
                                viewModel.shareRegistrationOnWhatsApp(context, devotee!!, pass)
                            }
                        )
                    }
                }

                if (members.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "समूह / परिवार के अन्य सदस्य (${members.size})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SaffronDark
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                members.forEachIndexed { i, m ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "${i + 1}. ${m.memberName}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text(text = "${m.relationship} (${m.age} वर्ष)", fontSize = 12.sp, color = TextSecondary)
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
