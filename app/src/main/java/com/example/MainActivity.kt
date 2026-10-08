package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.AppScreen
import com.example.ui.NarmadaViewModel
import com.example.ui.components.SacredAuthDialog
import com.example.ui.components.SacredDpPickerModal
import com.example.ui.components.SacredTopBar
import com.example.ui.screens.*
import com.example.ui.theme.NarmadaBlue
import com.example.ui.theme.NarmadaTheme
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SaffronPrimary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = androidx.activity.SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            NarmadaTheme {
                NarmadaApp()
            }
        }
    }
}

@Composable
fun NarmadaApp(viewModel: NarmadaViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val logoConfig by viewModel.logoConfig.collectAsStateWithLifecycle()
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAuthDialog by remember { mutableStateOf(false) }
    var showDpModal by remember { mutableStateOf(false) }

    LaunchedEffect(snackbarMsg) {
        if (snackbarMsg != null) {
            snackbarHostState.showSnackbar(snackbarMsg!!)
            viewModel.clearSnackbar()
        }
    }

    // Hardware & system Back button handling
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        viewModel.handleBack()
    }

    val screenTitle = when (currentScreen) {
        AppScreen.HOME -> "श्री मां नर्मदा जन्मोत्सव 2027"
        AppScreen.REGISTER_WIZARD -> "भक्त पंजीयन (Registration)"
        AppScreen.REGISTRATION_SUCCESS -> "पंजीयन सफल (Registration Pass)"
        AppScreen.MY_REGISTRATION -> "अपना पंजीयन देखें (My Pass)"
        AppScreen.VERIFY_PASS -> "पास सत्यापित करें (Verify Pass)"
        AppScreen.VERIFY_RECEIPT -> "रसीद सत्यापित करें (Verify Receipt)"
        AppScreen.SCHEDULE -> "महोत्सव कार्यक्रम (Program Schedule)"
        AppScreen.DONATION -> "सहयोग एवं दान (Donation Receipt)"
        AppScreen.ABOUT_CONTACT -> "आयोजन विवरण व संपर्क (About & Contact)"
        AppScreen.ADMIN_DASHBOARD -> "व्यवस्थापक पोर्टल (Admin Dashboard)"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SacredTopBar(
                title = screenTitle,
                currentRole = currentRole,
                onRoleSelect = { viewModel.setRole(it) },
                onBack = if (currentScreen != AppScreen.HOME) {
                    { viewModel.handleBack() }
                } else null,
                logoConfig = logoConfig,
                currentUser = currentUser,
                onLoginClick = { showAuthDialog = true },
                onLogoutClick = { viewModel.logout() },
                onChangeDpClick = { showDpModal = true }
            )
        },
        bottomBar = {
            NarmadaBottomNav(
                currentScreen = currentScreen,
                onNavigate = { screen ->
                    if (screen == AppScreen.ADMIN_DASHBOARD) {
                        if (currentUser == null) {
                            showAuthDialog = true
                            viewModel.showSnackbar("प्रशासक पोर्टल हेतु कृपया पहले लॉगिन करें।")
                        } else if (currentUser!!.role == UserRole.PUBLIC_USER) {
                            viewModel.showSnackbar("भक्त खाते से व्यवस्थापक पोर्टल अनुमत नहीं है। व्यवस्थापक क्रेडेंशियल से लॉगिन करें।")
                            showAuthDialog = true
                        } else {
                            viewModel.navigateTo(screen)
                        }
                    } else {
                        viewModel.navigateTo(screen)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                AppScreen.REGISTER_WIZARD -> RegistrationWizardScreen(viewModel = viewModel)
                AppScreen.REGISTRATION_SUCCESS -> RegistrationSuccessScreen(viewModel = viewModel)
                AppScreen.MY_REGISTRATION -> MyRegistrationScreen(viewModel = viewModel)
                AppScreen.VERIFY_PASS -> VerifyPassScreen(viewModel = viewModel)
                AppScreen.VERIFY_RECEIPT -> VerifyReceiptScreen(viewModel = viewModel)
                AppScreen.SCHEDULE -> ProgramScheduleScreen(viewModel = viewModel)
                AppScreen.DONATION -> DonationScreen(viewModel = viewModel)
                AppScreen.ABOUT_CONTACT -> ContactAboutScreen(viewModel = viewModel)
                AppScreen.ADMIN_DASHBOARD -> AdminDashboardScreen(viewModel = viewModel)
            }
        }
    }

    if (showAuthDialog) {
        SacredAuthDialog(
            viewModel = viewModel,
            onDismiss = { showAuthDialog = false }
        )
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
fun NarmadaBottomNav(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    data class NavItem(val screen: AppScreen, val label: String, val icon: ImageVector)

    val items = listOf(
        NavItem(AppScreen.HOME, "मुख्य", Icons.Default.Home),
        NavItem(AppScreen.REGISTER_WIZARD, "पंजीयन", Icons.Default.AppRegistration),
        NavItem(AppScreen.MY_REGISTRATION, "पास/सीट", Icons.Default.ConfirmationNumber),
        NavItem(AppScreen.SCHEDULE, "कार्यक्रम", Icons.Default.EventNote),
        NavItem(AppScreen.ADMIN_DASHBOARD, "प्रशासक", Icons.Default.AdminPanelSettings)
    )

    NavigationBar(
        containerColor = PureWhite,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentScreen == item.screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SaffronPrimary,
                    selectedTextColor = SaffronPrimary,
                    indicatorColor = SaffronPrimary.copy(alpha = 0.15f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_${item.screen.name.lowercase()}")
            )
        }
    }
}
