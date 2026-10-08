package com.example.data.local

import androidx.room.*
import com.example.data.model.CustomRole
import kotlinx.coroutines.flow.Flow

@Dao
interface RoleDao {
    @Query("SELECT * FROM custom_roles ORDER BY nameHindi ASC")
    fun getAllCustomRoles(): Flow<List<CustomRole>>

    @Query("SELECT * FROM custom_roles WHERE id = :id LIMIT 1")
    suspend fun getRoleById(id: String): CustomRole?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRole(role: CustomRole)

    @Update
    suspend fun updateRole(role: CustomRole)

    @Delete
    suspend fun deleteRole(role: CustomRole)

    @Query("DELETE FROM custom_roles WHERE id = :id")
    suspend fun deleteRoleById(id: String)
}
