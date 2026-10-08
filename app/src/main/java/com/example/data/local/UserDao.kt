package com.example.data.local

import androidx.room.*
import com.example.data.model.AppUser
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM app_users ORDER BY id DESC")
    fun getAllUsers(): Flow<List<AppUser>>

    @Query("SELECT * FROM app_users WHERE isActive = 1")
    fun getActiveUsers(): Flow<List<AppUser>>

    @Query("SELECT * FROM app_users WHERE LOWER(username) = LOWER(:identifier) OR LOWER(email) = LOWER(:identifier) OR mobileNumber = :identifier OR registrationId = :identifier LIMIT 1")
    suspend fun findUserByIdentifier(identifier: String): AppUser?

    @Query("SELECT * FROM app_users WHERE mobileNumber = :mobile OR LOWER(email) = LOWER(:mobile)")
    suspend fun findUsersByMobile(mobile: String): List<AppUser>

    @Query("SELECT * FROM app_users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): AppUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: AppUser): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<AppUser>)

    @Update
    suspend fun updateUser(user: AppUser)

    @Delete
    suspend fun deleteUser(user: AppUser)

    @Query("UPDATE app_users SET password = :newPassword WHERE id = :userId")
    suspend fun updatePassword(userId: Long, newPassword: String)

    @Query("UPDATE app_users SET isActive = :isActive WHERE id = :userId")
    suspend fun toggleActive(userId: Long, isActive: Boolean)
}
