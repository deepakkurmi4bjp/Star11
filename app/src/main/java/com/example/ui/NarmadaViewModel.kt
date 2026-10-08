package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.NarmadaRepository
import com.example.security.AppPermissions
import com.example.security.RbacAuthorizer
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.URLEncoder

enum class AppScreen {
    HOME,
    REGISTER_WIZARD,
    REGISTRATION_SUCCESS,
    MY_REGISTRATION,
    VERIFY_PASS,
    VERIFY_RECEIPT,
    SCHEDULE,
    DONATION,
    ABOUT_CONTACT,
    ADMIN_DASHBOARD
}

enum class AdminTab {
    OVERVIEW,
    DEVOTEES,
    SEATS,
    CHECKIN,
    FINANCE,
    CMS_NOTICES,
    STAFF_MANAGEMENT,
    DP_LOGO,
    AUDIT_LOGS,
    SETTINGS
}

data class WizardDraft(
    val step: Int = 1,
    // Step 1: Personal
    val fullName: String = "",
    val fatherHusbandName: String = "",
    val gender: String = "पुरुष",
    val age: String = "30",
    val mobileNumber: String = "",
    val whatsappNumber: String = "",
    val altMobileNumber: String = "",
    // Step 2: Address
    val address: String = "",
    val village: String = "",
    val post: String = "",
    val gramPanchayat: String = "",
    val tehsil: String = "तेंदूखेड़ा",
    val district: String = "दमोह",
    val state: String = "मध्य प्रदेश",
    val pinCode: String = "",
    // Step 3: Emergency & Identity
    val emergencyContactName: String = "",
    val emergencyContactNumber: String = "",
    val emergencyRelationship: String = "",
    val idProofLast4: String = "",
    // Step 4: Participation
    val isChunriYatraParticipant: Boolean = true,
    val preferredDate: String = "11-13 फ़रवरी 2027 (तीनों दिन)",
    val groupFamilyName: String = "",
    val villageOrganization: String = "",
    val isTransportRequired: Boolean = false,
    val isFoodRequired: Boolean = true,
    val isAccommodationRequired: Boolean = false,
    val specialRequirements: String = "",
    // Step 5: Additional Family Members
    val additionalMembers: List<GroupMember> = emptyList(),
    // Errors
    val errorMessage: String? = null,
    val duplicateWarning: Devotee? = null
)

class NarmadaViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    val repository = NarmadaRepository(
        devoteeDao = db.devoteeDao(),
        passSeatDao = db.passSeatDao(),
        eventProgramDao = db.eventProgramDao(),
        financeDao = db.financeDao(),
        cmsDao = db.cmsDao(),
        auditDao = db.auditDao(),
        userDao = db.userDao(),
        roleDao = db.roleDao()
    )

    // Current User & Authentication
    private val _currentUser = MutableStateFlow<AppUser?>(null)
    val currentUser: StateFlow<AppUser?> = _currentUser.asStateFlow()

    // Custom Roles State
    val allCustomRoles: StateFlow<List<CustomRole>> = repository.allCustomRoles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customRolesMap: StateFlow<Map<String, CustomRole>> = repository.allCustomRoles
        .map { list -> list.associateBy { it.id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _lastRegisteredCredentials = MutableStateFlow<AppUser?>(null)
    val lastRegisteredCredentials: StateFlow<AppUser?> = _lastRegisteredCredentials.asStateFlow()

    // Dynamic DP / Logo Configuration
    private val _logoConfig = MutableStateFlow(AppLogoConfig())
    val logoConfig: StateFlow<AppLogoConfig> = _logoConfig.asStateFlow()

    // Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _screenHistory = mutableListOf<AppScreen>()

    // Current Role
    private val _currentRole = MutableStateFlow(UserRole.PUBLIC)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // Granular RBAC Permission Checking Helpers
    fun hasPermission(permissionId: String): Boolean {
        return RbacAuthorizer.hasPermission(
            user = _currentUser.value,
            permissionId = permissionId,
            customRolesMap = customRolesMap.value
        )
    }

    fun hasAnyPermission(vararg permissionIds: String): Boolean {
        return RbacAuthorizer.hasAnyPermission(
            user = _currentUser.value,
            permissionIds = permissionIds,
            customRolesMap = customRolesMap.value
        )
    }

    fun getEffectivePermissions(): Set<String> {
        return RbacAuthorizer.getEffectivePermissions(
            user = _currentUser.value,
            customRolesMap = customRolesMap.value
        )
    }

    private val _adminTab = MutableStateFlow(AdminTab.OVERVIEW)
    val adminTab: StateFlow<AdminTab> = _adminTab.asStateFlow()

    // Wizard draft
    private val _wizardDraft = MutableStateFlow(WizardDraft())
    val wizardDraft: StateFlow<WizardDraft> = _wizardDraft.asStateFlow()

    // Last completed registration & pass
    private val _lastRegistered = MutableStateFlow<Pair<Devotee, PassItem>?>(null)
    val lastRegistered: StateFlow<Pair<Devotee, PassItem>?> = _lastRegistered.asStateFlow()

    // Public Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResultDevotee = MutableStateFlow<Devotee?>(null)
    val searchResultDevotee: StateFlow<Devotee?> = _searchResultDevotee.asStateFlow()

    private val _searchResultPass = MutableStateFlow<PassItem?>(null)
    val searchResultPass: StateFlow<PassItem?> = _searchResultPass.asStateFlow()

    private val _searchResultMembers = MutableStateFlow<List<GroupMember>>(emptyList())
    val searchResultMembers: StateFlow<List<GroupMember>> = _searchResultMembers.asStateFlow()

    private val _searchHasSearched = MutableStateFlow(false)
    val searchHasSearched: StateFlow<Boolean> = _searchHasSearched.asStateFlow()

    // Public Pass Verification
    private val _verifyPassQuery = MutableStateFlow("")
    val verifyPassQuery: StateFlow<String> = _verifyPassQuery.asStateFlow()

    private val _passVerificationResult = MutableStateFlow<NarmadaRepository.PublicPassVerification?>(null)
    val passVerificationResult: StateFlow<NarmadaRepository.PublicPassVerification?> = _passVerificationResult.asStateFlow()

    // Public Receipt Verification
    private val _verifyReceiptQuery = MutableStateFlow("")
    val verifyReceiptQuery: StateFlow<String> = _verifyReceiptQuery.asStateFlow()

    private val _receiptVerificationResult = MutableStateFlow<NarmadaRepository.PublicReceiptVerification?>(null)
    val receiptVerificationResult: StateFlow<NarmadaRepository.PublicReceiptVerification?> = _receiptVerificationResult.asStateFlow()

    // Scanner Checkin
    private val _scannerInput = MutableStateFlow("")
    val scannerInput: StateFlow<String> = _scannerInput.asStateFlow()

    private val _checkinResult = MutableStateFlow<NarmadaRepository.CheckinResult?>(null)
    val checkinResult: StateFlow<NarmadaRepository.CheckinResult?> = _checkinResult.asStateFlow()

    // Admin Filters
    private val _devoteeFilterStatus = MutableStateFlow("ALL") // ALL, CONFIRMED, WAITLISTED, CANCELLED
    val devoteeFilterStatus: StateFlow<String> = _devoteeFilterStatus.asStateFlow()

    private val _devoteeSearchText = MutableStateFlow("")
    val devoteeSearchText: StateFlow<String> = _devoteeSearchText.asStateFlow()

    // Seat Management Selected
    private val _selectedSeat = MutableStateFlow<SeatItem?>(null)
    val selectedSeat: StateFlow<SeatItem?> = _selectedSeat.asStateFlow()

    // UI feedback toast / snackbar
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDemoDataIfNeeded()
        }
        viewModelScope.launch {
            repository.getLogoConfig().collect {
                _logoConfig.value = it
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            _screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun handleBack(): Boolean {
        if (_currentScreen.value == AppScreen.HOME) {
            return false // Allow system to exit or handle
        }
        if (_screenHistory.isNotEmpty()) {
            _currentScreen.value = _screenHistory.removeAt(_screenHistory.size - 1)
            return true
        }
        _currentScreen.value = AppScreen.HOME
        return true
    }

    fun setRole(role: UserRole) {
        _currentRole.value = role
        showSnackbar("सक्रिय भूमिका: ${role.labelHindi}")
    }

    fun setAdminTab(tab: AdminTab) {
        _adminTab.value = tab
    }

    fun showSnackbar(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    // Wizard actions
    fun updateDraft(updater: (WizardDraft) -> WizardDraft) {
        _wizardDraft.value = updater(_wizardDraft.value)
    }

    fun resetWizard() {
        _wizardDraft.value = WizardDraft()
    }

    fun nextWizardStep() {
        val draft = _wizardDraft.value
        when (draft.step) {
            1 -> {
                if (draft.fullName.isBlank()) {
                    _wizardDraft.value = draft.copy(errorMessage = "कृपया पूरा नाम दर्ज करें")
                    return
                }
                if (draft.mobileNumber.length < 10) {
                    _wizardDraft.value = draft.copy(errorMessage = "कृपया 10 अंकों का वैध मोबाइल नंबर दर्ज करें")
                    return
                }
                // Check duplicate mobile
                viewModelScope.launch {
                    val existing = repository.isMobileRegistered(draft.mobileNumber)
                    if (existing != null) {
                        _wizardDraft.value = draft.copy(
                            duplicateWarning = existing,
                            errorMessage = "इस मोबाइल नंबर (${draft.mobileNumber}) से पहले से पंजीयन उपलब्ध है।"
                        )
                    } else {
                        _wizardDraft.value = draft.copy(step = 2, errorMessage = null, duplicateWarning = null)
                    }
                }
            }
            2 -> {
                if (draft.village.isBlank() && draft.address.isBlank()) {
                    _wizardDraft.value = draft.copy(errorMessage = "कृपया ग्राम/नगर अथवा पता दर्ज करें")
                    return
                }
                _wizardDraft.value = draft.copy(step = 3, errorMessage = null)
            }
            3 -> {
                _wizardDraft.value = draft.copy(step = 4, errorMessage = null)
            }
            4 -> {
                _wizardDraft.value = draft.copy(step = 5, errorMessage = null)
            }
            5 -> {
                _wizardDraft.value = draft.copy(step = 6, errorMessage = null)
            }
        }
    }

    fun prevWizardStep() {
        val current = _wizardDraft.value.step
        if (current > 1) {
            _wizardDraft.value = _wizardDraft.value.copy(step = current - 1, errorMessage = null)
        }
    }

    fun addFamilyMember(member: GroupMember) {
        val list = _wizardDraft.value.additionalMembers.toMutableList()
        list.add(member)
        _wizardDraft.value = _wizardDraft.value.copy(additionalMembers = list)
    }

    fun removeFamilyMember(index: Int) {
        val list = _wizardDraft.value.additionalMembers.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _wizardDraft.value = _wizardDraft.value.copy(additionalMembers = list)
        }
    }

    fun submitRegistration() {
        val draft = _wizardDraft.value
        viewModelScope.launch {
            val devotee = Devotee(
                registrationId = "",
                fullName = draft.fullName.trim(),
                fatherHusbandName = draft.fatherHusbandName.trim(),
                gender = draft.gender,
                age = draft.age.toIntOrNull() ?: 30,
                mobileNumber = draft.mobileNumber.trim(),
                whatsappNumber = draft.whatsappNumber.ifBlank { draft.mobileNumber }.trim(),
                altMobileNumber = draft.altMobileNumber.trim(),
                address = draft.address.trim(),
                village = draft.village.trim(),
                post = draft.post.trim(),
                gramPanchayat = draft.gramPanchayat.trim(),
                tehsil = draft.tehsil.trim(),
                district = draft.district.trim(),
                state = draft.state.trim(),
                pinCode = draft.pinCode.trim(),
                emergencyContactName = draft.emergencyContactName.trim(),
                emergencyContactNumber = draft.emergencyContactNumber.trim(),
                emergencyRelationship = draft.emergencyRelationship.trim(),
                idProofLast4 = draft.idProofLast4.trim(),
                isChunriYatraParticipant = draft.isChunriYatraParticipant,
                groupFamilyName = draft.groupFamilyName.trim(),
                villageOrganization = draft.villageOrganization.trim(),
                preferredDate = draft.preferredDate,
                isTransportRequired = draft.isTransportRequired,
                isFoodRequired = draft.isFoodRequired,
                isAccommodationRequired = draft.isAccommodationRequired,
                specialRequirements = draft.specialRequirements.trim()
            )

            val result = repository.registerDevoteeWithGroup(
                primaryDevotee = devotee,
                additionalMembers = draft.additionalMembers,
                autoAssignSeat = true
            )
            _lastRegistered.value = Pair(result.first, result.second)
            _lastRegisteredCredentials.value = result.third
            _currentScreen.value = AppScreen.REGISTRATION_SUCCESS
            resetWizard()
        }
    }

    // Public Registration Search
    fun searchRegistration(query: String) {
        _searchQuery.value = query
        _searchHasSearched.value = true
        if (query.isBlank()) {
            _searchResultDevotee.value = null
            _searchResultPass.value = null
            _searchResultMembers.value = emptyList()
            return
        }
        viewModelScope.launch {
            val q = query.trim()
            var devotee = repository.getDevoteeById(q)
            if (devotee == null) {
                devotee = repository.isMobileRegistered(q)
            }
            _searchResultDevotee.value = devotee
            if (devotee != null) {
                _searchResultPass.value = repository.getPassByRegistrationId(devotee.registrationId)
                _searchResultMembers.value = repository.getGroupMembers(devotee.groupId)
            } else {
                _searchResultPass.value = null
                _searchResultMembers.value = emptyList()
            }
        }
    }

    // Public Pass Verification
    fun verifyPass(query: String) {
        _verifyPassQuery.value = query
        viewModelScope.launch {
            val res = repository.verifyPassPublic(query)
            _passVerificationResult.value = res
        }
    }

    // Public Receipt Verification
    fun verifyReceipt(query: String) {
        _verifyReceiptQuery.value = query
        viewModelScope.launch {
            val res = repository.verifyReceiptPublic(query)
            _receiptVerificationResult.value = res
        }
    }

    // Check-in Scanner
    fun performCheckin(tokenOrId: String) {
        viewModelScope.launch {
            val res = repository.processCheckin(tokenOrId, scannedBy = _currentRole.value.name)
            _checkinResult.value = res
        }
    }

    fun dismissCheckinResult() {
        _checkinResult.value = null
        _scannerInput.value = ""
    }

    // Seat Management
    fun selectSeat(seat: SeatItem?) {
        _selectedSeat.value = seat
    }

    fun assignSeatToDevotee(seatNumber: String, devoteeId: String, devoteeName: String) {
        viewModelScope.launch {
            val ok = repository.assignSeatManually(
                seatNumber = seatNumber,
                devoteeId = devoteeId,
                devoteeName = devoteeName,
                performedBy = _currentRole.value.name,
                role = _currentRole.value.name
            )
            if (ok) {
                showSnackbar("सीट $seatNumber सफलतापूर्वक $devoteeName को आवंटित की गई।")
                _selectedSeat.value = null
            } else {
                showSnackbar("त्रुटि: यह सीट पहले से किसी अन्य भक्त को आवंटित है।")
            }
        }
    }

    fun releaseSeat(seatNumber: String) {
        viewModelScope.launch {
            repository.releaseSeat(seatNumber, _currentRole.value.name, _currentRole.value.name)
            showSnackbar("सीट $seatNumber खाली कर दी गई।")
            _selectedSeat.value = null
        }
    }

    fun toggleBlockSeat(seatNumber: String) {
        viewModelScope.launch {
            repository.blockSeat(seatNumber, _currentRole.value.name, _currentRole.value.name)
            showSnackbar("सीट $seatNumber की स्थिति बदली गई।")
            _selectedSeat.value = null
        }
    }

    // Filter controls
    fun setDevoteeFilter(status: String) {
        _devoteeFilterStatus.value = status
    }

    fun setDevoteeSearch(text: String) {
        _devoteeSearchText.value = text
    }

    // Donation & Expense
    fun recordDonation(
        name: String,
        mobile: String,
        amount: Double,
        mode: String,
        purpose: String,
        txId: String,
        onComplete: (DonationReceipt) -> Unit
    ) {
        viewModelScope.launch {
            val receipt = repository.recordDonation(
                donorName = name,
                mobileNumber = mobile,
                amount = amount,
                paymentMode = mode,
                purpose = purpose,
                collectorName = if (_currentRole.value == UserRole.COLLECTOR) "सहयोग संग्रहकर्ता" else "मुख्य कार्यालय",
                transactionId = txId
            )
            showSnackbar("दान रसीद ${receipt.receiptNumber} जारी हो गई।")
            onComplete(receipt)
        }
    }

    fun recordExpense(
        category: String,
        description: String,
        amount: Double,
        paidTo: String,
        paymentMode: String,
        billRef: String
    ) {
        viewModelScope.launch {
            repository.recordExpense(
                category = category,
                description = description,
                amount = amount,
                paidTo = paidTo,
                paymentMode = paymentMode,
                billRef = billRef,
                createdBy = _currentRole.value.name
            )
            showSnackbar("व्यय रिकॉर्ड सहेज लिया गया।")
        }
    }

    // WhatsApp Sharing deep link builder
    fun shareRegistrationOnWhatsApp(context: Context, devotee: Devotee, pass: PassItem?) {
        val msg = """
*॥ श्री नर्मदे हर ॥*
*श्री मां नर्मदा भक्त परिवार*
द्वितीय विशाल श्री मां नर्मदा जन्मोत्सव एवं चुनरी पदयात्रा महोत्सव 2027
दिनांक: 11–13 फरवरी 2027

*पंजीयन विवरण:*
नाम: ${devotee.fullName}
पंजीयन संख्या (Reg ID): ${devotee.registrationId}
${if (pass != null && pass.seatNumber.isNotBlank()) "आवंटित सीट: " + pass.seatNumber else "प्रवेश: सामान्य"}
${if (pass != null) "पास श्रेणी: " + pass.category else ""}
स्थान: मुख्य नर्मदा तट, नर्मदापुरम

_मां नर्मदा की कृपा आप एवं आपके परिवार पर बनी रहे।_
        """.trimIndent()

        val url = "https://api.whatsapp.com/send?text=${URLEncoder.encode(msg, "UTF-8")}"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to generic share
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, msg)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "साझा करें"))
        }
    }

    fun shareReceiptOnWhatsApp(context: Context, receipt: DonationReceipt) {
        val msg = """
*॥ श्री नर्मदे हर ॥*
*श्री मां नर्मदा भक्त परिवार — दान पावती*
रसीद संख्या: ${receipt.receiptNumber}
दानदाता: ${receipt.donorName}
सहयोग राशि: ₹${"%,.2f".format(receipt.amount)}
सहयोग उद्देश्य: ${receipt.purpose}
दिनांक: ${receipt.date}
सत्यापन कोड: ${receipt.verificationToken}
स्थिति: ✓ आधिकारिक सत्यापित रसीद

_मां नर्मदा के सेवा कार्य में आपके अमूल्य सहयोग हेतु हार्दिक धन्यवाद।_
        """.trimIndent()

        val url = "https://api.whatsapp.com/send?text=${URLEncoder.encode(msg, "UTF-8")}"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, msg)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "रसीद साझा करें"))
        }
    }

    // CSV Export
    fun exportRegistrationsCsv(context: Context, devotees: List<Devotee>) {
        val sb = StringBuilder()
        sb.append("Registration ID,Name,Father/Husband,Gender,Age,Mobile,Village,District,Status,Group ID\n")
        devotees.forEach { d ->
            sb.append("\"${d.registrationId}\",\"${d.fullName}\",\"${d.fatherHusbandName}\",\"${d.gender}\",${d.age},\"${d.mobileNumber}\",\"${d.village}\",\"${d.district}\",\"${d.status}\",\"${d.groupId}\"\n")
        }
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "Narmada_Devotees_List.csv")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/csv"
        }
        context.startActivity(Intent.createChooser(sendIntent, "पंजीयन सूची निर्यात करें"))
    }

    fun exportDonationsCsv(context: Context, donations: List<DonationReceipt>) {
        val sb = StringBuilder()
        sb.append("Receipt No,Donor Name,Amount,Payment Mode,Purpose,Collector,Date,Verification Token\n")
        donations.forEach { d ->
            sb.append("\"${d.receiptNumber}\",\"${d.donorName}\",${d.amount},\"${d.paymentMode}\",\"${d.purpose}\",\"${d.collectorName}\",\"${d.date}\",\"${d.verificationToken}\"\n")
        }
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "Narmada_Donations_Report.csv")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/csv"
        }
        context.startActivity(Intent.createChooser(sendIntent, "दान रसीद रिपोर्ट निर्यात करें"))
    }

    fun reseedDemoData() {
        viewModelScope.launch {
            repository.resetDemoData()
            showSnackbar("डेमो डेटा सफलतापूर्वक री-सीड (Re-seeded) किया गया।")
        }
    }

    fun launchIO(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }

    // Authentication and Session Management
    fun login(
        identifier: String,
        pass: String,
        onSuccess: (AppUser) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (identifier.isBlank() || pass.isBlank()) {
            onError("कृपया यूज़रनेम/ईमेल तथा पासवर्ड दोनों दर्ज करें")
            return
        }
        viewModelScope.launch {
            val user = repository.authenticate(identifier, pass)
            if (user != null) {
                _currentUser.value = user
                _currentRole.value = user.role
                showSnackbar("स्वागत है, ${user.fullName} (${user.role.labelHindi})")
                onSuccess(user)
            } else {
                onError("गलत क्रेडेंशियल अथवा खाता निष्क्रिय है।")
            }
        }
    }

    fun logout() {
        val oldName = _currentUser.value?.fullName ?: "उपयोगकर्ता"
        _currentUser.value = null
        _currentRole.value = UserRole.PUBLIC
        showSnackbar("$oldName लॉगआउट हो चुके हैं।")
    }

    fun loginAsDevotee(user: AppUser) {
        _currentUser.value = user
        _currentRole.value = user.role
        showSnackbar("स्वागत है, ${user.fullName}")
    }

    // Super Admin: Dynamic DP / Logo Update
    fun updateLogoConfig(config: AppLogoConfig) {
        viewModelScope.launch {
            val updatedBy = _currentUser.value?.email?.ifBlank { _currentUser.value?.username } ?: "Deepak53802@gmail.com"
            repository.saveLogoConfig(config, updatedBy)
            showSnackbar("डीपी / लोगो सफलतापूर्वक अद्यतन किया गया!")
        }
    }

    // Super Admin: Staff Credential Management
    fun createStaffUser(
        fullName: String,
        username: String,
        email: String,
        mobile: String,
        role: UserRole,
        pass: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (fullName.isBlank() || username.isBlank() || pass.isBlank()) {
            onError("कृपया नाम, यूज़रनेम एवं पासवर्ड दर्ज करें")
            return
        }
        viewModelScope.launch {
            val existing = repository.findUserForRecovery(username)
            if (existing != null) {
                onError("यह यूज़रनेम ($username) पहले से दर्ज है।")
                return@launch
            }
            val creator = _currentUser.value?.email ?: "Deepak53802@gmail.com"
            val newUser = AppUser(
                username = username.trim(),
                email = email.trim(),
                password = pass.trim(),
                fullName = fullName.trim(),
                mobileNumber = mobile.trim(),
                role = role,
                isActive = true,
                createdBy = creator
            )
            repository.createStaffUser(newUser, creator)
            showSnackbar("नया स्टाफ क्रेडेंशियल सफलतापूर्वक बनाया गया!")
            onSuccess()
        }
    }

    fun updateStaffUser(user: AppUser) {
        viewModelScope.launch {
            val updater = _currentUser.value?.email ?: "Deepak53802@gmail.com"
            repository.updateStaffUser(user, updater)
            showSnackbar("स्टाफ विवरण अद्यतन किया गया।")
        }
    }

    fun toggleStaffActive(user: AppUser) {
        viewModelScope.launch {
            val toggler = _currentUser.value?.email ?: "Deepak53802@gmail.com"
            repository.toggleStaffActive(user.id, !user.isActive, toggler)
            showSnackbar("खाते की स्थिति: ${if (!user.isActive) "सक्रिय" else "निष्क्रिय"}")
        }
    }

    fun deleteStaffUser(user: AppUser) {
        viewModelScope.launch {
            val deleter = _currentUser.value?.email ?: "Deepak53802@gmail.com"
            val ok = repository.deleteStaffUser(user, deleter)
            if (ok) {
                showSnackbar("स्टाफ खाता सफलतापूर्वक हटाया गया।")
            } else {
                showSnackbar("त्रुटि: मुख्य सुपर एडमिन (Deepak53802@gmail.com) को हटाया नहीं जा सकता।")
            }
        }
    }

    fun resetStaffPassword(userId: Long, newPass: String) {
        viewModelScope.launch {
            repository.resetPassword(userId, newPass)
            showSnackbar("पासवर्ड सफलतापूर्वक रीसेट किया गया!")
        }
    }

    fun recoverPassword(identifier: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        if (identifier.isBlank() || newPass.isBlank()) {
            onResult(false, "कृपया आवश्यक जानकारी दर्ज करें")
            return
        }
        viewModelScope.launch {
            val user = repository.findUserForRecovery(identifier)
            if (user != null) {
                repository.resetPassword(user.id, newPass)
                onResult(true, "पासवर्ड सफलतापूर्वक बदला गया! नए पासवर्ड से लॉगिन करें।")
            } else {
                onResult(false, "इस विवरण से कोई खाता प्राप्त नहीं हुआ।")
            }
        }
    }

    fun recoverUsername(mobile: String, onResult: (List<AppUser>) -> Unit) {
        if (mobile.isBlank()) {
            onResult(emptyList())
            return
        }
        viewModelScope.launch {
            val users = repository.findUsersByMobile(mobile)
            onResult(users)
        }
    }

    // ==========================================
    // RBAC & MULTI-ROLE METHODS
    // ==========================================

    fun createCustomRole(
        nameHindi: String,
        description: String,
        permissions: List<String>,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (nameHindi.isBlank() || permissions.isEmpty()) {
            onError("कृपया भूमिका का नाम एवं कम से कम एक अनुमति चुनें")
            return
        }
        viewModelScope.launch {
            val id = "ROLE_" + nameHindi.trim().replace("\\s+".toRegex(), "_").uppercase()
            val customRole = CustomRole(
                id = id,
                nameHindi = nameHindi.trim(),
                description = description.trim(),
                permissionsCsv = permissions.joinToString(","),
                createdBy = _currentUser.value?.username ?: "SUPER_ADMIN"
            )
            val res = repository.createCustomRole(customRole, _currentUser.value)
            if (res.isSuccess) {
                showSnackbar("कस्टम भूमिका '${nameHindi}' सफलतापूर्वक बनाई गई!")
                onSuccess()
            } else {
                val err = res.exceptionOrNull()?.message ?: "भूमिका निर्माण में त्रुटि"
                onError(err)
                showSnackbar(err)
            }
        }
    }

    fun deleteCustomRole(roleId: String) {
        viewModelScope.launch {
            val res = repository.deleteCustomRole(roleId, _currentUser.value)
            if (res.isSuccess) {
                showSnackbar("कस्टम भूमिका हटाई गई।")
            } else {
                showSnackbar(res.exceptionOrNull()?.message ?: "त्रुटि")
            }
        }
    }

    fun assignUserRoles(
        targetUserId: Long,
        newPrimaryRole: UserRole,
        secondaryRolesCsv: String,
        customPermsCsv: String = "",
        deniedPermsCsv: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val res = repository.assignUserRoles(
                targetUserId = targetUserId,
                newPrimaryRole = newPrimaryRole,
                secondaryRolesCsv = secondaryRolesCsv,
                customPermsCsv = customPermsCsv,
                deniedPermsCsv = deniedPermsCsv,
                actor = _currentUser.value
            )
            if (res.isSuccess) {
                showSnackbar("भूमिकाएं सफलतापूर्वक अद्यतन की गईं!")
                onSuccess()
            } else {
                val err = res.exceptionOrNull()?.message ?: "भूमिका आवंटन में त्रुटि"
                onError(err)
                showSnackbar(err)
            }
        }
    }

    fun createStaffUserWithRbac(
        fullName: String,
        username: String,
        email: String,
        mobile: String,
        primaryRole: UserRole,
        secondaryRolesCsv: String,
        pass: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (fullName.isBlank() || username.isBlank() || pass.isBlank()) {
            onError("कृपया नाम, यूज़रनेम एवं पासवर्ड दर्ज करें")
            return
        }
        viewModelScope.launch {
            val existing = repository.findUserForRecovery(username)
            if (existing != null) {
                onError("यह यूज़रनेम ($username) पहले से दर्ज है।")
                return@launch
            }
            val creator = _currentUser.value?.username ?: "Deepak53802@gmail.com"
            val newUser = AppUser(
                username = username.trim(),
                email = email.trim().ifBlank { "$username@narmada.org" },
                password = pass.trim(),
                fullName = fullName.trim(),
                mobileNumber = mobile.trim(),
                role = primaryRole,
                secondaryRolesCsv = secondaryRolesCsv.trim(),
                isActive = true,
                createdBy = creator
            )
            val res = repository.createStaffUserWithRbac(newUser, _currentUser.value)
            if (res.isSuccess) {
                showSnackbar("नया स्टाफ उपयोगकर्ता सफलतापूर्वक जोड़ा गया!")
                onSuccess()
            } else {
                val err = res.exceptionOrNull()?.message ?: "उपयोगकर्ता निर्माण में त्रुटि"
                onError(err)
                showSnackbar(err)
            }
        }
    }

    fun toggleStaffActiveWithRbac(user: AppUser) {
        viewModelScope.launch {
            val res = repository.toggleStaffActiveWithRbac(user.id, !user.isActive, _currentUser.value)
            if (res.isSuccess) {
                showSnackbar("खाते की स्थिति: ${if (!user.isActive) "सक्रिय" else "निष्क्रिय"}")
            } else {
                showSnackbar(res.exceptionOrNull()?.message ?: "त्रुटि")
            }
        }
    }

    fun deleteStaffUserWithRbac(user: AppUser) {
        viewModelScope.launch {
            val res = repository.deleteStaffUserWithRbac(user, _currentUser.value)
            if (res.isSuccess) {
                showSnackbar("उपयोगकर्ता खाता हटाया गया।")
            } else {
                showSnackbar(res.exceptionOrNull()?.message ?: "त्रुटि")
            }
        }
    }

    fun clearAuditLogs() {
        viewModelScope.launch {
            val res = repository.clearAuditLogsWithRbac(_currentUser.value)
            if (res.isSuccess) {
                showSnackbar("सभी ऑडिट लॉग्स सफलतापूर्वक साफ किए गए।")
            } else {
                showSnackbar(res.exceptionOrNull()?.message ?: "त्रुटि")
            }
        }
    }

    fun deleteDevoteeWithRbac(regId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.deleteRegistrationWithRbac(regId, _currentUser.value)
            if (res.isSuccess) {
                showSnackbar("पंजीयन रिकॉर्ड सफलतापूर्वक डिलीट किया गया।")
                onSuccess()
            } else {
                showSnackbar(res.exceptionOrNull()?.message ?: "त्रुटि")
            }
        }
    }

    fun cancelRegistrationWithRbac(regId: String, reason: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.cancelRegistrationWithRbac(regId, reason, _currentUser.value)
            if (res.isSuccess) {
                showSnackbar("पंजीयन रद्द किया गया।")
                onSuccess()
            } else {
                showSnackbar(res.exceptionOrNull()?.message ?: "त्रुटि")
            }
        }
    }

    fun blockSeatWithRbac(seatNumber: String, reason: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.blockSeatWithRbac(seatNumber, reason, _currentUser.value)
            if (res.isSuccess) {
                showSnackbar("सीट $seatNumber सफलतापूर्वक ब्लॉक की गई।")
                onSuccess()
            } else {
                showSnackbar(res.exceptionOrNull()?.message ?: "त्रुटि")
            }
        }
    }

    fun releaseSeatWithRbac(seatNumber: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.releaseSeatWithRbac(seatNumber, _currentUser.value)
            if (res.isSuccess) {
                showSnackbar("सीट $seatNumber मुक्त कर दी गई है।")
                onSuccess()
            } else {
                showSnackbar(res.exceptionOrNull()?.message ?: "त्रुटि")
            }
        }
    }

    fun cancelReceiptWithRbac(receiptNumber: String, reason: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.cancelReceiptWithRbac(receiptNumber, reason, _currentUser.value)
            if (res.isSuccess) {
                showSnackbar("रसीद निरस्त कर दी गई है (ऑडिट में दर्ज)।")
                onSuccess()
            } else {
                showSnackbar(res.exceptionOrNull()?.message ?: "त्रुटि")
            }
        }
    }

    fun verifyCheckinAttendee(tokenOrId: String, onResult: (com.example.security.CheckinAttendeeInfo) -> Unit) {
        viewModelScope.launch {
            val info = repository.verifyCheckinAttendee(tokenOrId, _currentUser.value)
            onResult(info)
        }
    }

    fun performCheckinWithRbac(tokenOrId: String, gate: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.performCheckinWithRbac(tokenOrId, gate, _currentUser.value)
            if (res.isSuccess) {
                showSnackbar("चेक-इन सफलतापूर्वक दर्ज हुआ!")
                onResult(true, "प्रवेश स्वीकृत (Gate $gate)")
            } else {
                val err = res.exceptionOrNull()?.message ?: "चेक-इन विफल"
                showSnackbar(err)
                onResult(false, err)
            }
        }
    }
}
