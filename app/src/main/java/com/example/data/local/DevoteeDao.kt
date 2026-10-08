package com.example.data.local

import androidx.room.*
import com.example.data.model.Devotee
import com.example.data.model.GroupMember
import kotlinx.coroutines.flow.Flow

@Dao
interface DevoteeDao {
    @Query("SELECT * FROM devotees ORDER BY createdAt DESC")
    fun getAllDevotees(): Flow<List<Devotee>>

    @Query("SELECT * FROM devotees WHERE registrationId = :regId LIMIT 1")
    suspend fun getDevoteeById(regId: String): Devotee?

    @Query("SELECT * FROM devotees WHERE mobileNumber = :mobile OR whatsappNumber = :mobile LIMIT 1")
    suspend fun getDevoteeByMobile(mobile: String): Devotee?

    @Query("SELECT * FROM devotees WHERE registrationId = :query OR mobileNumber LIKE '%' || :query || '%' OR fullName LIKE '%' || :query || '%'")
    fun searchDevotees(query: String): Flow<List<Devotee>>

    @Query("SELECT * FROM devotees WHERE groupId = :groupId")
    suspend fun getDevoteesByGroupId(groupId: String): List<Devotee>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevotee(devotee: Devotee)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevotees(devotees: List<Devotee>)

    @Update
    suspend fun updateDevotee(devotee: Devotee)

    @Query("UPDATE devotees SET status = :newStatus WHERE registrationId = :regId")
    suspend fun updateDevoteeStatus(regId: String, newStatus: String)

    @Query("DELETE FROM devotees WHERE registrationId = :regId")
    suspend fun deleteDevotee(regId: String)

    // Group Members
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMembers(members: List<GroupMember>)

    @Query("SELECT * FROM group_members WHERE groupId = :groupId")
    fun getMembersByGroupId(groupId: String): Flow<List<GroupMember>>

    @Query("SELECT * FROM group_members WHERE groupId = :groupId")
    suspend fun getMembersByGroupIdSync(groupId: String): List<GroupMember>

    @Query("SELECT COUNT(*) FROM devotees")
    fun getTotalDevoteeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM devotees WHERE status = 'CONFIRMED'")
    fun getConfirmedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM devotees WHERE status = 'WAITLISTED'")
    fun getWaitlistCount(): Flow<Int>
}
