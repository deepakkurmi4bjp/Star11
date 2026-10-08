package com.example.security

import com.example.data.model.AppUser
import com.example.data.model.CustomRole
import com.example.data.model.UserRole

/**
 * Definition of a granular permission in the RBAC matrix.
 */
data class PermissionDef(
    val id: String,
    val categoryHindi: String,
    val titleHindi: String,
    val descriptionHindi: String,
    val isCritical: Boolean = false
)

/**
 * Standard Permission Identifiers matching the project RBAC requirements.
 */
object AppPermissions {
    // 1. Registrations
    const val REGISTRATION_VIEW = "registration.view"
    const val REGISTRATION_CREATE = "registration.create"
    const val REGISTRATION_EDIT = "registration.edit"
    const val REGISTRATION_APPROVE = "registration.approve"
    const val REGISTRATION_REJECT = "registration.reject"
    const val REGISTRATION_CANCEL = "registration.cancel"
    const val REGISTRATION_DELETE = "registration.delete"

    // 2. Groups
    const val GROUP_VIEW = "group.view"
    const val GROUP_CREATE = "group.create"
    const val GROUP_EDIT = "group.edit"
    const val GROUP_DELETE = "group.delete"

    // 3. Passes
    const val PASS_VIEW = "pass.view"
    const val PASS_CREATE = "pass.create"
    const val PASS_EDIT = "pass.edit"
    const val PASS_CANCEL = "pass.cancel"
    const val PASS_VERIFY = "pass.verify"

    // 4. Seating
    const val SEAT_VIEW = "seat.view"
    const val SEAT_CREATE = "seat.create"
    const val SEAT_ASSIGN = "seat.assign"
    const val SEAT_CHANGE = "seat.change"
    const val SEAT_RELEASE = "seat.release"
    const val SEAT_BLOCK = "seat.block"

    // 5. Events & Programs
    const val EVENT_VIEW = "event.view"
    const val EVENT_CREATE = "event.create"
    const val EVENT_EDIT = "event.edit"
    const val EVENT_DELETE = "event.delete"
    const val EVENT_PUBLISH = "event.publish"

    // 6. Check-in
    const val CHECKIN_VIEW = "checkin.view"
    const val CHECKIN_CREATE = "checkin.create"
    const val CHECKIN_REVERSE = "checkin.reverse"

    // 7. Donations
    const val DONATION_VIEW = "donation.view"
    const val DONATION_CREATE = "donation.create"
    const val DONATION_EDIT = "donation.edit"
    const val DONATION_CANCEL = "donation.cancel"

    // 8. Receipts
    const val RECEIPT_VIEW = "receipt.view"
    const val RECEIPT_CREATE = "receipt.create"
    const val RECEIPT_VERIFY = "receipt.verify"
    const val RECEIPT_CANCEL = "receipt.cancel"

    // 9. Expenses
    const val EXPENSE_VIEW = "expense.view"
    const val EXPENSE_CREATE = "expense.create"
    const val EXPENSE_EDIT = "expense.edit"
    const val EXPENSE_APPROVE = "expense.approve"
    const val EXPENSE_CANCEL = "expense.cancel"

    // 10. CMS & Notices
    const val CMS_VIEW = "cms.view"
    const val CMS_CREATE = "cms.create"
    const val CMS_EDIT = "cms.edit"
    const val CMS_PUBLISH = "cms.publish"
    const val GALLERY_MANAGE = "gallery.manage"
    const val NOTICE_MANAGE = "notice.manage"
    const val FAQ_MANAGE = "faq.manage"

    // 11. Reports
    const val REPORT_VIEW = "report.view"
    const val REPORT_EXPORT = "report.export"

    // 12. Users & Roles
    const val USER_VIEW = "user.view"
    const val USER_CREATE = "user.create"
    const val USER_EDIT = "user.edit"
    const val USER_DISABLE = "user.disable"
    const val USER_DELETE = "user.delete"
    const val ROLE_VIEW = "role.view"
    const val ROLE_CREATE = "role.create"
    const val ROLE_EDIT = "role.edit"
    const val ROLE_ASSIGN = "role.assign"

    // 13. Settings & Audit
    const val SETTINGS_VIEW = "settings.view"
    const val SETTINGS_EDIT = "settings.edit"
    const val AUDIT_VIEW = "audit.view"
    const val AUDIT_DELETE = "audit.delete"
    const val SYSTEM_RESET = "system.reset"

    val ALL_DEFINITIONS: List<PermissionDef> = listOf(
        // Registrations
        PermissionDef(REGISTRATION_VIEW, "पंजीयन (Registration)", "पंजीयन देखें", "सभी भक्तों के पंजीयन विवरण देखें"),
        PermissionDef(REGISTRATION_CREATE, "पंजीयन (Registration)", "नया पंजीयन करें", "भक्तों का नया ऑनलाइन/ऑफलाइन पंजीयन करें"),
        PermissionDef(REGISTRATION_EDIT, "पंजीयन (Registration)", "पंजीयन सुधारें", "भक्तों की व्यक्तिगत जानकारी व विवरण अपडेट करें"),
        PermissionDef(REGISTRATION_APPROVE, "पंजीयन (Registration)", "पंजीयन स्वीकृत करें", "लंबित पंजीयन को आधिकारिक रूप से स्वीकृत करें"),
        PermissionDef(REGISTRATION_REJECT, "पंजीयन (Registration)", "पंजीयन अस्वीकृत करें", "अनुचित पंजीयन को निरस्त/अस्वीकृत करें"),
        PermissionDef(REGISTRATION_CANCEL, "पंजीयन (Registration)", "पंजीयन रद्द करें", "स्वीकृत पंजीयन को विशेष कारणों से रद्द करें"),
        PermissionDef(REGISTRATION_DELETE, "पंजीयन (Registration)", "स्थायी रूप से हटाएं", "पंजीयन रिकॉर्ड को पूर्णतः डिलीट करें", isCritical = true),

        // Groups
        PermissionDef(GROUP_VIEW, "समूह (Groups)", "समूह देखें", "पदयात्रा समूह व परिवार के विवरण देखें"),
        PermissionDef(GROUP_CREATE, "समूह (Groups)", "नया समूह बनाएं", "परिवार या पदयात्रा समूह पंजीकृत करें"),
        PermissionDef(GROUP_EDIT, "समूह (Groups)", "समूह सुधारें", "समूह के सदस्यों को जोड़ें या घटाएं"),
        PermissionDef(GROUP_DELETE, "समूह (Groups)", "समूह हटाएं", "समूह रिकॉर्ड को हटाएं"),

        // Passes
        PermissionDef(PASS_VIEW, "प्रवेश पास (Passes)", "पास देखें", "जारी किए गए डिजिटल/क्यूआर पास देखें"),
        PermissionDef(PASS_CREATE, "प्रवेश पास (Passes)", "पास बनाएं / जारी करें", "भक्तों को नया प्रवेश पास जारी करें"),
        PermissionDef(PASS_EDIT, "प्रवेश पास (Passes)", "पास नवीनीकृत / बदलें", "पास श्रेणी अथवा वैधता अपडेट करें"),
        PermissionDef(PASS_CANCEL, "प्रवेश पास (Passes)", "पास निरस्त करें", "अनुचित या डुप्लिकेट पास को ब्लॉक/कैंसिल करें"),
        PermissionDef(PASS_VERIFY, "प्रवेश पास (Passes)", "पास सत्यापन", "क्यूआर कोड स्कैन कर पास की प्रामाणिकता जांचें"),

        // Seating
        PermissionDef(SEAT_VIEW, "सीट व्यवस्था (Seating)", "सीट स्थिति देखें", "पांडाल व ब्लॉक में रिक्त/आवंटित सीटें देखें"),
        PermissionDef(SEAT_CREATE, "सीट व्यवस्था (Seating)", "सीट कॉन्फ़िगर करें", "नए ब्लॉक, पंक्तियां व सीटें जोड़ें"),
        PermissionDef(SEAT_ASSIGN, "सीट व्यवस्था (Seating)", "सीट आवंटित करें", "भक्त को चयनित सीट आवंटित करें"),
        PermissionDef(SEAT_CHANGE, "सीट व्यवस्था (Seating)", "सीट बदलें", "आवंटित सीट को बदलकर दूसरी सीट दें"),
        PermissionDef(SEAT_RELEASE, "सीट व्यवस्था (Seating)", "सीट मुक्त करें", "आवंटन रद्द कर सीट को रिक्त करें"),
        PermissionDef(SEAT_BLOCK, "सीट व्यवस्था (Seating)", "सीट ब्लॉक / रिजर्व करें", "विशिष्ट अतिथियों हेतु सीट सुरक्षित करें"),

        // Events
        PermissionDef(EVENT_VIEW, "कार्यक्रम (Events)", "कार्यक्रम देखें", "महोत्सव व यात्रा की समय सारिणी देखें"),
        PermissionDef(EVENT_CREATE, "कार्यक्रम (Events)", "कार्यक्रम जोड़ें", "नया सत्र, आरती, महाप्रसाद समय जोड़ें"),
        PermissionDef(EVENT_EDIT, "कार्यक्रम (Events)", "कार्यक्रम संशोधित करें", "दिनांक, समय व स्थान अपडेट करें"),
        PermissionDef(EVENT_DELETE, "कार्यक्रम (Events)", "कार्यक्रम हटाएं", "रद्द हुआ कार्यक्रम हटाएं"),
        PermissionDef(EVENT_PUBLISH, "कार्यक्रम (Events)", "कार्यक्रम प्रकाशित करें", "जनसाधारण हेतु कार्यक्रम लाइव करें"),

        // Checkin
        PermissionDef(CHECKIN_VIEW, "प्रवेश जांच (Check-in)", "उपस्थिति देखें", "गेट पर सत्यापित भक्तों की सूची देखें"),
        PermissionDef(CHECKIN_CREATE, "प्रवेश जांच (Check-in)", "प्रवेश स्कैन करें", "गेट पर क्यूआर स्कैन कर चेक-इन दर्ज करें"),
        PermissionDef(CHECKIN_REVERSE, "प्रवेश जांच (Check-in)", "चेक-इन उलटना", "भूलवश हुए चेक-इन को रिवर्स करें", isCritical = true),

        // Donations
        PermissionDef(DONATION_VIEW, "सहयोग / दान (Donations)", "दान सूची देखें", "प्राप्त सहयोग राशि का विवरण देखें"),
        PermissionDef(DONATION_CREATE, "सहयोग / दान (Donations)", "नया दान दर्ज करें", "सहयोग राशि व रसीद विवरण प्रविष्ट करें"),
        PermissionDef(DONATION_EDIT, "सहयोग / दान (Donations)", "दान रिकॉर्ड सुधारें", "सहयोग विवरण में सुधार करें"),
        PermissionDef(DONATION_CANCEL, "सहयोग / दान (Donations)", "दान रद्द करें", "त्रुटिपूर्ण दान प्रविष्टि रद्द करें", isCritical = true),

        // Receipts
        PermissionDef(RECEIPT_VIEW, "रसीदें (Receipts)", "रसीदें देखें", "डिजिटल रसीदें देखें व पुनः प्रिंट करें"),
        PermissionDef(RECEIPT_CREATE, "रसीदें (Receipts)", "रसीद जारी करें", "आधिकारिक सहयोग रसीद तैयार करें"),
        PermissionDef(RECEIPT_VERIFY, "रसीदें (Receipts)", "रसीद सत्यापित करें", "नंबर या क्यूआर से रसीद की जांच करें"),
        PermissionDef(RECEIPT_CANCEL, "रसीदें (Receipts)", "रसीद निरस्त करें", "रसीद रद्द करें (ऑडिट में दर्ज रहेगी)", isCritical = true),

        // Expenses
        PermissionDef(EXPENSE_VIEW, "व्यय (Expenses)", "व्यय विवरण देखें", "महोत्सव आयोजन के खर्च देखें"),
        PermissionDef(EXPENSE_CREATE, "व्यय (Expenses)", "नया खर्च दर्ज करें", "टेंट, प्रसाद, व्यवस्था आदि का बिल दर्ज करें"),
        PermissionDef(EXPENSE_EDIT, "व्यय (Expenses)", "खर्च विवरण सुधारें", "दर्ज व्यय में संशोधन करें"),
        PermissionDef(EXPENSE_APPROVE, "व्यय (Expenses)", "खर्च स्वीकृत करें", "दर्ज व्यय वाउचर को स्वीकृत करें"),
        PermissionDef(EXPENSE_CANCEL, "व्यय (Expenses)", "खर्च अस्वीकृत करें", "अनुचित खर्च वाउचर निरस्त करें"),

        // CMS & Content
        PermissionDef(CMS_VIEW, "वेबसाइट सामग्री (CMS)", "सामग्री देखें", "वेबसाइट एवं ऐप पृष्ठ विवरण देखें"),
        PermissionDef(CMS_CREATE, "वेबसाइट सामग्री (CMS)", "सामग्री जोड़ें", "नया पृष्ठ अथवा बैनर जोड़ें"),
        PermissionDef(CMS_EDIT, "वेबसाइट सामग्री (CMS)", "सामग्री सुधारें", "वेबसाइट टेक्स्ट, संपर्क, इतिहास सुधारें"),
        PermissionDef(CMS_PUBLISH, "वेबसाइट सामग्री (CMS)", "सामग्री प्रकाशित करें", "संपादित जानकारी लाइव करें"),
        PermissionDef(GALLERY_MANAGE, "वेबसाइट सामग्री (CMS)", "फोटो गैलरी प्रबंधित करें", "यात्रा व जन्मोत्सव के छायाचित्र जोड़ें/हटाएं"),
        PermissionDef(NOTICE_MANAGE, "वेबसाइट सामग्री (CMS)", "सूचनाएं प्रबंधित करें", "जरूरी दिशा-निर्देश व सूचनाएं प्रकाशित करें"),
        PermissionDef(FAQ_MANAGE, "वेबसाइट सामग्री (CMS)", "प्रश्नोत्तरी (FAQ) प्रबंधित करें", "अक्सर पूछे जाने वाले सवाल प्रबंधित करें"),

        // Reports
        PermissionDef(REPORT_VIEW, "रिपोर्ट्स (Reports)", "रिपोर्ट्स देखें", "पंजीयन, सीट, उपस्थिति व वित्त के आंकड़े देखें"),
        PermissionDef(REPORT_EXPORT, "रिपोर्ट्स (Reports)", "डेटा एक्सपोर्ट करें", "एक्सेल/सीएसवी/पीडीएफ में रिकॉर्ड एक्सपोर्ट करें"),

        // Users & Roles
        PermissionDef(USER_VIEW, "उपयोगकर्ता (Users)", "स्टाफ सूची देखें", "सभी अधिकृत उपयोगकर्ताओं की सूची देखें"),
        PermissionDef(USER_CREATE, "उपयोगकर्ता (Users)", "नया स्टाफ जोड़ें", "एडमिन, ऑपरेटर, स्वयंसेवक खाते बनाएं"),
        PermissionDef(USER_EDIT, "उपयोगकर्ता (Users)", "स्टाफ विवरण सुधारें", "नाम, मोबाइल, ईमेल में संशोधन करें"),
        PermissionDef(USER_DISABLE, "उपयोगकर्ता (Users)", "खाता ब्लॉक/सक्रिय करें", "उपयोगकर्ता का लॉगिन अधिकार रोकें"),
        PermissionDef(USER_DELETE, "उपयोगकर्ता (Users)", "स्टाफ खाता हटाएं", "स्टाफ खाता स्थायी रूप से हटाएं", isCritical = true),
        PermissionDef(ROLE_VIEW, "भूमिकाएं (Roles)", "भूमिकाएं देखें", "सिस्टम एवं कस्टम भूमिकाओं की सूची देखें"),
        PermissionDef(ROLE_CREATE, "भूमिकाएं (Roles)", "कस्टम भूमिका बनाएं", "नए अधिकार समूह वाली भूमिका बनाएं"),
        PermissionDef(ROLE_EDIT, "भूमिकाएं (Roles)", "भूमिका अनुमतियां बदलें", "कस्टम भूमिका के अधिकार संशोधित करें"),
        PermissionDef(ROLE_ASSIGN, "भूमिकाएं (Roles)", "भूमिका आवंटित करें", "उपयोगकर्ताओं को भूमिकाएं प्रदान करें/बदलें", isCritical = true),

        // Settings & Audit
        PermissionDef(SETTINGS_VIEW, "सिस्टम सेटिंग्स (Settings)", "सेटिंग्स देखें", "संस्था व ऐप कॉन्फ़िगरेशन देखें"),
        PermissionDef(SETTINGS_EDIT, "सिस्टम सेटिंग्स (Settings)", "सेटिंग्स बदलें", "डीपी/लोगो, संस्था संपर्क व प्रणाली सेटिंग्स बदलें"),
        PermissionDef(AUDIT_VIEW, "ऑडिट व सुरक्षा (Audit)", "ऑडिट लॉग देखें", "सभी सुरक्षा व प्रशासनिक गतिविधियों का इतिहास देखें"),
        PermissionDef(AUDIT_DELETE, "ऑडिट व सुरक्षा (Audit)", "ऑडिट लॉग साफ करें", "ऑडिट लॉग मिटाएं (केवल सुपर एडमिन)", isCritical = true),
        PermissionDef(SYSTEM_RESET, "ऑडिट व सुरक्षा (Audit)", "सिस्टम / डेटाबेस रीसेट", "प्रणाली को फैक्टरी रीसेट करें", isCritical = true)
    )

    val DEFINITION_MAP: Map<String, PermissionDef> = ALL_DEFINITIONS.associateBy { it.id }

    val CATEGORIES: List<String> = ALL_DEFINITIONS.map { it.categoryHindi }.distinct()
}

/**
 * RBAC Matrix: Maps standard roles to their default set of allowed permissions.
 */
object RolePermissions {

    val SUPER_ADMIN_PERMISSIONS: Set<String> = AppPermissions.ALL_DEFINITIONS.map { it.id }.toSet()

    val ADMIN_PERMISSIONS: Set<String> = setOf(
        AppPermissions.REGISTRATION_VIEW,
        AppPermissions.REGISTRATION_CREATE,
        AppPermissions.REGISTRATION_EDIT,
        AppPermissions.REGISTRATION_APPROVE,
        AppPermissions.REGISTRATION_REJECT,
        AppPermissions.REGISTRATION_CANCEL,
        AppPermissions.GROUP_VIEW,
        AppPermissions.GROUP_CREATE,
        AppPermissions.GROUP_EDIT,
        AppPermissions.PASS_VIEW,
        AppPermissions.PASS_CREATE,
        AppPermissions.PASS_EDIT,
        AppPermissions.PASS_CANCEL,
        AppPermissions.PASS_VERIFY,
        AppPermissions.SEAT_VIEW,
        AppPermissions.SEAT_ASSIGN,
        AppPermissions.SEAT_CHANGE,
        AppPermissions.SEAT_RELEASE,
        AppPermissions.SEAT_BLOCK,
        AppPermissions.EVENT_VIEW,
        AppPermissions.EVENT_CREATE,
        AppPermissions.EVENT_EDIT,
        AppPermissions.EVENT_PUBLISH,
        AppPermissions.CHECKIN_VIEW,
        AppPermissions.CHECKIN_CREATE,
        AppPermissions.CHECKIN_REVERSE,
        AppPermissions.DONATION_VIEW,
        AppPermissions.DONATION_CREATE,
        AppPermissions.DONATION_EDIT,
        AppPermissions.DONATION_CANCEL,
        AppPermissions.RECEIPT_VIEW,
        AppPermissions.RECEIPT_CREATE,
        AppPermissions.RECEIPT_VERIFY,
        AppPermissions.RECEIPT_CANCEL,
        AppPermissions.EXPENSE_VIEW,
        AppPermissions.EXPENSE_CREATE,
        AppPermissions.EXPENSE_EDIT,
        AppPermissions.EXPENSE_APPROVE,
        AppPermissions.EXPENSE_CANCEL,
        AppPermissions.CMS_VIEW,
        AppPermissions.CMS_CREATE,
        AppPermissions.CMS_EDIT,
        AppPermissions.CMS_PUBLISH,
        AppPermissions.GALLERY_MANAGE,
        AppPermissions.NOTICE_MANAGE,
        AppPermissions.FAQ_MANAGE,
        AppPermissions.REPORT_VIEW,
        AppPermissions.REPORT_EXPORT,
        AppPermissions.USER_VIEW,
        AppPermissions.USER_CREATE,
        AppPermissions.USER_EDIT,
        AppPermissions.USER_DISABLE,
        AppPermissions.ROLE_VIEW,
        AppPermissions.SETTINGS_VIEW,
        AppPermissions.SETTINGS_EDIT,
        AppPermissions.AUDIT_VIEW
    )

    val EVENT_MANAGER_PERMISSIONS: Set<String> = setOf(
        AppPermissions.EVENT_VIEW,
        AppPermissions.EVENT_CREATE,
        AppPermissions.EVENT_EDIT,
        AppPermissions.EVENT_PUBLISH,
        AppPermissions.REGISTRATION_VIEW,
        AppPermissions.REGISTRATION_APPROVE,
        AppPermissions.REGISTRATION_REJECT,
        AppPermissions.GROUP_VIEW,
        AppPermissions.GROUP_CREATE,
        AppPermissions.GROUP_EDIT,
        AppPermissions.PASS_VIEW,
        AppPermissions.SEAT_VIEW,
        AppPermissions.CHECKIN_VIEW,
        AppPermissions.REPORT_VIEW,
        AppPermissions.NOTICE_MANAGE
    )

    val REGISTRATION_OPERATOR_PERMISSIONS: Set<String> = setOf(
        AppPermissions.REGISTRATION_VIEW,
        AppPermissions.REGISTRATION_CREATE,
        AppPermissions.REGISTRATION_EDIT,
        AppPermissions.GROUP_VIEW,
        AppPermissions.GROUP_CREATE,
        AppPermissions.GROUP_EDIT,
        AppPermissions.PASS_VIEW,
        AppPermissions.PASS_CREATE,
        AppPermissions.PASS_VERIFY
    )

    val SEAT_MANAGER_PERMISSIONS: Set<String> = setOf(
        AppPermissions.SEAT_VIEW,
        AppPermissions.SEAT_CREATE,
        AppPermissions.SEAT_ASSIGN,
        AppPermissions.SEAT_CHANGE,
        AppPermissions.SEAT_RELEASE,
        AppPermissions.SEAT_BLOCK,
        AppPermissions.PASS_VIEW,
        AppPermissions.PASS_VERIFY,
        AppPermissions.REGISTRATION_VIEW
    )

    val COLLECTION_OPERATOR_PERMISSIONS: Set<String> = setOf(
        AppPermissions.DONATION_VIEW,
        AppPermissions.DONATION_CREATE,
        AppPermissions.DONATION_EDIT,
        AppPermissions.RECEIPT_VIEW,
        AppPermissions.RECEIPT_CREATE,
        AppPermissions.RECEIPT_VERIFY,
        AppPermissions.REPORT_VIEW
    )

    val CHECK_IN_VOLUNTEER_PERMISSIONS: Set<String> = setOf(
        AppPermissions.CHECKIN_VIEW,
        AppPermissions.CHECKIN_CREATE,
        AppPermissions.PASS_VERIFY
    )

    val CONTENT_MANAGER_PERMISSIONS: Set<String> = setOf(
        AppPermissions.CMS_VIEW,
        AppPermissions.CMS_CREATE,
        AppPermissions.CMS_EDIT,
        AppPermissions.CMS_PUBLISH,
        AppPermissions.GALLERY_MANAGE,
        AppPermissions.NOTICE_MANAGE,
        AppPermissions.FAQ_MANAGE,
        AppPermissions.EVENT_VIEW
    )

    val REPORT_VIEWER_PERMISSIONS: Set<String> = setOf(
        AppPermissions.REPORT_VIEW,
        AppPermissions.REGISTRATION_VIEW,
        AppPermissions.SEAT_VIEW,
        AppPermissions.CHECKIN_VIEW,
        AppPermissions.EVENT_VIEW
    )

    val PUBLIC_USER_PERMISSIONS: Set<String> = emptySet()

    fun getDefaultPermissionsForRole(role: UserRole): Set<String> {
        return when (role) {
            UserRole.SUPER_ADMIN -> SUPER_ADMIN_PERMISSIONS
            UserRole.ADMIN -> ADMIN_PERMISSIONS
            UserRole.EVENT_MANAGER -> EVENT_MANAGER_PERMISSIONS
            UserRole.REGISTRATION_OPERATOR -> REGISTRATION_OPERATOR_PERMISSIONS
            UserRole.SEAT_MANAGER -> SEAT_MANAGER_PERMISSIONS
            UserRole.COLLECTION_OPERATOR -> COLLECTION_OPERATOR_PERMISSIONS
            UserRole.CHECK_IN_VOLUNTEER -> CHECK_IN_VOLUNTEER_PERMISSIONS
            UserRole.CONTENT_MANAGER -> CONTENT_MANAGER_PERMISSIONS
            UserRole.REPORT_VIEWER -> REPORT_VIEWER_PERMISSIONS
            UserRole.PUBLIC_USER -> PUBLIC_USER_PERMISSIONS
        }
    }
}

/**
 * RBAC Authorizer: Computes effective permissions and enforces role-based security.
 */
object RbacAuthorizer {

    /**
     * Resolve effective permissions for a user given their primary role,
     * secondary roles, custom roles from the DB, and explicit allows/denials.
     * Principle: Explicit DENY overrides any ALLOW.
     */
    fun getEffectivePermissions(
        user: AppUser?,
        customRolesMap: Map<String, CustomRole> = emptyMap()
    ): Set<String> {
        if (user == null || !user.isActive) return emptySet()

        // Super Admin always has full access
        if (user.role == UserRole.SUPER_ADMIN) {
            return RolePermissions.SUPER_ADMIN_PERMISSIONS
        }

        // 1. Gather all permissions from primary role
        val effective = mutableSetOf<String>()
        effective.addAll(RolePermissions.getDefaultPermissionsForRole(user.role))

        // 2. Add permissions from secondary roles (comma-separated: e.g. "EVENT_MANAGER,ROLE_KARYAKRAM")
        val secondary = user.secondaryRolesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        for (sec in secondary) {
            // Check built-in role
            try {
                val builtIn = UserRole.valueOf(sec)
                effective.addAll(RolePermissions.getDefaultPermissionsForRole(builtIn))
            } catch (e: Exception) {
                // Check custom role
                val custom = customRolesMap[sec]
                if (custom != null && custom.isActive) {
                    val customPerms = custom.permissionsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    effective.addAll(customPerms)
                }
            }
        }

        // 3. Add explicit custom permissions
        val explicitAllows = user.customPermissionsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        effective.addAll(explicitAllows)

        // 4. Subtract explicit denied permissions (DENIAL WINS)
        val explicitDenies = user.deniedPermissionsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        effective.removeAll(explicitDenies.toSet())

        return effective
    }

    /**
     * Check if a user possesses a specific permission.
     */
    fun hasPermission(
        user: AppUser?,
        permissionId: String,
        customRolesMap: Map<String, CustomRole> = emptyMap()
    ): Boolean {
        if (user == null || !user.isActive) return false
        if (user.role == UserRole.SUPER_ADMIN) return true
        val permissions = getEffectivePermissions(user, customRolesMap)
        return permissions.contains(permissionId)
    }

    /**
     * Check if a user has ANY of the specified permissions.
     */
    fun hasAnyPermission(
        user: AppUser?,
        vararg permissionIds: String,
        customRolesMap: Map<String, CustomRole> = emptyMap()
    ): Boolean {
        if (user == null || !user.isActive) return false
        if (user.role == UserRole.SUPER_ADMIN) return true
        val permissions = getEffectivePermissions(user, customRolesMap)
        return permissionIds.any { permissions.contains(it) }
    }

    /**
     * Enforce a permission requirement. Returns Result.success or Result.failure with SecurityException.
     */
    fun enforce(
        user: AppUser?,
        permissionId: String,
        actionNameHindi: String,
        customRolesMap: Map<String, CustomRole> = emptyMap()
    ): Result<Unit> {
        if (user == null) {
            return Result.failure(SecurityException("लॉगिन आवश्यक: कृपया पहले प्रमाणीकरण करें।"))
        }
        if (!user.isActive) {
            return Result.failure(SecurityException("खाता निष्क्रिय: आपका उपयोगकर्ता खाता निष्क्रिय कर दिया गया है।"))
        }
        if (!hasPermission(user, permissionId, customRolesMap)) {
            val def = AppPermissions.DEFINITION_MAP[permissionId]
            val permName = def?.titleHindi ?: permissionId
            return Result.failure(
                SecurityException("अनाधिकृत कार्रवाई ($actionNameHindi): आपके पास '$permName' की अनुमति नहीं है।")
            )
        }
        return Result.success(Unit)
    }

    /**
     * Verify if the acting user has hierarchical authority to manage the target user.
     * Rule:
     * - SUPER_ADMIN can manage all users.
     * - ADMIN can manage non-super-admins, but CANNOT modify or create SUPER_ADMIN.
     * - A user cannot promote someone to a role higher than themselves.
     */
    fun canManageUser(actor: AppUser?, target: AppUser?): Boolean {
        if (actor == null || !actor.isActive) return false
        if (actor.role == UserRole.SUPER_ADMIN) return true

        // If target is Super Admin, only Super Admin can manage
        if (target != null && target.role == UserRole.SUPER_ADMIN) return false

        // Admin can manage other staff (except Super Admin)
        if (actor.role == UserRole.ADMIN) {
            return target?.role != UserRole.SUPER_ADMIN
        }

        return false
    }

    /**
     * Verify if the actor is permitted to assign the designated role.
     * Prevents privilege escalation (e.g. Admin assigning Super Admin).
     */
    fun canAssignRole(actor: AppUser?, targetRole: UserRole): Boolean {
        if (actor == null || !actor.isActive) return false
        if (actor.role == UserRole.SUPER_ADMIN) return true
        if (targetRole == UserRole.SUPER_ADMIN) return false // Only Super Admin can assign Super Admin
        return actor.role == UserRole.ADMIN
    }
}

/**
 * Scoped data model for entry verification.
 * Check-in volunteers ONLY receive minimum required attendee information:
 * Name, Registration ID, Pass ID, Seat, Pass Status, Check-in Status.
 * If already checked in, displays prominent "ALREADY CHECKED IN" with timestamp.
 */
data class CheckinAttendeeInfo(
    val devoteeName: String,
    val registrationId: String,
    val passId: String,
    val seatNumber: String,
    val passStatus: String,
    val checkinStatus: String, // "सत्यापित (VALID)" or "ALREADY CHECKED IN"
    val isAlreadyCheckedIn: Boolean = false,
    val checkinTimestamp: Long = 0L,
    val checkinFormattedTime: String = "",
    val gate: String = "",
    val errorMessage: String? = null
)
