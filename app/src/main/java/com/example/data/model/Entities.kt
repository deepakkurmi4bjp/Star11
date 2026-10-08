package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devotees")
data class Devotee(
    @PrimaryKey val registrationId: String,
    val groupId: String = "",
    val fullName: String,
    val fatherHusbandName: String = "",
    val gender: String = "पुरुष",
    val age: Int = 30,
    val mobileNumber: String,
    val whatsappNumber: String = "",
    val altMobileNumber: String = "",
    val address: String = "",
    val village: String = "",
    val post: String = "",
    val gramPanchayat: String = "",
    val tehsil: String = "",
    val district: String = "",
    val state: String = "मध्य प्रदेश",
    val pinCode: String = "",
    val emergencyContactName: String = "",
    val emergencyContactNumber: String = "",
    val emergencyRelationship: String = "",
    val isChunriYatraParticipant: Boolean = true,
    val accompanyingPersonsCount: Int = 0,
    val groupFamilyName: String = "",
    val villageOrganization: String = "",
    val preferredDate: String = "11-13 फ़रवरी 2027 (तीनों दिन)",
    val isTransportRequired: Boolean = false,
    val isFoodRequired: Boolean = true,
    val isAccommodationRequired: Boolean = false,
    val specialRequirements: String = "",
    val idProofLast4: String = "",
    val status: String = "CONFIRMED", // CONFIRMED, WAITLISTED, CANCELLED
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "group_members")
data class GroupMember(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: String,
    val individualRegistrationId: String,
    val memberName: String,
    val relationship: String,
    val age: Int,
    val gender: String,
    val mobile: String = ""
)

@Entity(tableName = "passes")
data class PassItem(
    @PrimaryKey val passId: String,
    val registrationId: String,
    val devoteeName: String,
    val category: String, // सामान्य भक्त पास, परिवार पास, विशेष अतिथि पास, VIP / VVIP पास, स्वयंसेवक पास, वाहन पास
    val seatNumber: String = "",
    val venue: String = "मुख्य पंडाल (नर्मदा तट)",
    val section: String = "",
    val row: String = "",
    val validDate: String = "11-13 फ़रवरी 2027",
    val validEvent: String = "द्वितीय विशाल श्री मां नर्मदा जन्मोत्सव एवं चुनरी पदयात्रा 2027",
    val status: String = "ACTIVE", // ACTIVE, USED, CANCELLED, BLOCKED
    val qrToken: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "seats")
data class SeatItem(
    @PrimaryKey val seatId: String,
    val venue: String = "मुख्य पंडाल",
    val section: String, // "Section A", "Section B"
    val row: String,     // "A1", "A2", "B1"...
    val seatNumber: String, // "A1-01"
    val status: String = "AVAILABLE", // AVAILABLE, RESERVED, ASSIGNED, OCCUPIED, BLOCKED
    val assignedDevoteeId: String? = null,
    val assignedDevoteeName: String? = null,
    val assignedGroupId: String? = null
)

@Entity(tableName = "checkins")
data class CheckinRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val passId: String,
    val registrationId: String,
    val devoteeName: String,
    val seatNumber: String,
    val checkinTimestamp: Long = System.currentTimeMillis(),
    val checkedInBy: String = "Volunteer",
    val status: String = "CHECKED_IN"
)

@Entity(tableName = "event_programs")
data class EventProgram(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // "11 फ़रवरी 2027", "12 फ़रवरी 2027", "13 फ़रवरी 2027"
    val time: String,
    val title: String,
    val venue: String,
    val description: String,
    val chiefGuest: String = "",
    val category: String, // "चुनरी यात्रा", "आरती एवं पूजन", "सत्संग एवं प्रवचन", "महाप्रसाद / भंडारा", "सांस्कृतिक संध्या"
    val status: String = "UPCOMING"
)

@Entity(tableName = "donation_receipts")
data class DonationReceipt(
    @PrimaryKey val receiptNumber: String,
    val donorName: String,
    val mobileNumber: String,
    val amount: Double,
    val paymentMode: String, // "नकद (Cash)", "UPI / QR", "बैंक ट्रांसफर", "चेक"
    val purpose: String, // "चुनरी अर्पण", "महाप्रसाद सेवा", "भंडारा", "सामान्य सहयोग"
    val collectorName: String,
    val date: String,
    val transactionId: String = "",
    val verificationToken: String,
    val status: String = "VERIFIED" // VERIFIED, CANCELLED
)

@Entity(tableName = "expenses")
data class ExpenseItem(
    @PrimaryKey val expenseId: String,
    val date: String,
    val category: String, // "मंडप एवं पंडाल", "महाप्रसाद व भोजन", "प्रचार-प्रसार", "वाहन व परिवहन", "लाइट व साउंड", "सुरक्षा व स्वयंसेवक", "विविध"
    val description: String,
    val amount: Double,
    val paidTo: String,
    val paymentMode: String = "नकद",
    val billReference: String = "",
    val createdBy: String = "व्यवस्थापक",
    val approvalStatus: String = "APPROVED" // APPROVED, PENDING, REJECTED
)

@Entity(tableName = "notices")
data class NoticeItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val date: String,
    val priority: String = "NORMAL", // HIGH, MEDIUM, NORMAL
    val isActive: Boolean = true,
    val isBanner: Boolean = false
)

@Entity(tableName = "faqs")
data class FaqItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val question: String,
    val answer: String,
    val category: String = "सामान्य",
    val displayOrder: Int = 0
)

@Entity(tableName = "cms_content")
data class CmsItem(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val user: String,
    val role: String,
    val action: String,
    val recordType: String,
    val recordId: String,
    val oldValue: String = "",
    val newValue: String = "",
    val permissionUsed: String = "",
    val status: String = "SUCCESS"
)

enum class UserRole(
    val labelHindi: String,
    val badgeBgColor: Long = 0xFF424242,
    val badgeTextColor: Long = 0xFFFFFFFF
) {
    SUPER_ADMIN("सुपर एडमिन (Super Admin)", 0xFF4A148C, 0xFFFFFFFF),
    ADMIN("प्रशासक (Admin)", 0xFF880E4F, 0xFFFFFFFF),
    EVENT_MANAGER("कार्यक्रम प्रबंधक (Event Manager)", 0xFF0D47A1, 0xFFFFFFFF),
    REGISTRATION_OPERATOR("पंजीयन ऑपरेटर (Registration Operator)", 0xFF00695C, 0xFFFFFFFF),
    SEAT_MANAGER("सीट प्रबंधक (Seat Manager)", 0xFF3949AB, 0xFFFFFFFF),
    COLLECTION_OPERATOR("सहयोग / रसीद संग्रहकर्ता (Collection Operator)", 0xFF1B5E20, 0xFFFFFFFF),
    CHECK_IN_VOLUNTEER("प्रवेश सत्यापन स्वयंसेवक (Check-in Volunteer)", 0xFFE65100, 0xFFFFFFFF),
    CONTENT_MANAGER("सामग्री प्रबंधक (Content Manager)", 0xFF00838F, 0xFFFFFFFF),
    REPORT_VIEWER("रिपोर्ट समीक्षक (Report Viewer)", 0xFF37474F, 0xFFFFFFFF),
    PUBLIC_USER("भक्त / सामान्य नागरिक (Public User)", 0xFF546E7A, 0xFFFFFFFF);

    companion object {
        val OPERATOR get() = REGISTRATION_OPERATOR
        val VOLUNTEER get() = CHECK_IN_VOLUNTEER
        val COLLECTOR get() = COLLECTION_OPERATOR
        val PUBLIC get() = PUBLIC_USER
    }
}

@Entity(tableName = "custom_roles")
data class CustomRole(
    @PrimaryKey val id: String,
    val nameHindi: String,
    val description: String = "",
    val permissionsCsv: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "SUPER_ADMIN",
    val isActive: Boolean = true
)

@Entity(tableName = "app_users")
data class AppUser(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String, // email or registrationId or mobile
    val email: String = "",
    val password: String,
    val fullName: String,
    val mobileNumber: String = "",
    val role: UserRole = UserRole.PUBLIC_USER,
    val secondaryRolesCsv: String = "", // comma-separated roles e.g. "EVENT_MANAGER,SEAT_MANAGER"
    val customPermissionsCsv: String = "", // explicit allowed permissions
    val deniedPermissionsCsv: String = "", // explicit denied permissions
    val isActive: Boolean = true,
    val registrationId: String = "", // for linked devotee pass/details
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "SYSTEM", // "SUPER_ADMIN", "SYSTEM", "AUTO_REGISTRATION"
    val lastLoginAt: Long = 0L,
    val failedLoginAttempts: Int = 0
)

data class AppLogoConfig(
    val type: String = "PRESET", // PRESET, CUSTOM_URL
    val presetId: String = "CARTOON_MAA_NARMADA", // CARTOON_MAA_NARMADA, MAA_NARMADA, SHIVLING, SACRED_OM, KALASH, TRISHUL, DEEPAM, CHUNRI_YATRA
    val customUrl: String = "",
    val festivalTitle: String = "द्वितीय विशाल श्री मां नर्मदा जन्मोत्सव 2027",
    val festivalSubtitle: String = "पावन चुनरी पदयात्रा एवं महाआरती महोत्सव",
    val badgeText: String = "🌸 बाल रूप मां नर्मदा",
    val updatedAt: Long = System.currentTimeMillis(),
    val updatedBy: String = "Deepak53802@gmail.com"
)

