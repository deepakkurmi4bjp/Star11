package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        Devotee::class,
        GroupMember::class,
        PassItem::class,
        SeatItem::class,
        CheckinRecord::class,
        EventProgram::class,
        DonationReceipt::class,
        ExpenseItem::class,
        NoticeItem::class,
        FaqItem::class,
        CmsItem::class,
        AuditLog::class,
        AppUser::class,
        CustomRole::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun devoteeDao(): DevoteeDao
    abstract fun passSeatDao(): PassSeatDao
    abstract fun eventProgramDao(): EventProgramDao
    abstract fun financeDao(): FinanceDao
    abstract fun cmsDao(): CmsDao
    abstract fun auditDao(): AuditDao
    abstract fun userDao(): UserDao
    abstract fun roleDao(): RoleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "narmada_utsav.db"
                ).fallbackToDestructiveMigration(true)
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
