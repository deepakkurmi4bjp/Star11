package com.example.data.local

import androidx.room.*
import com.example.data.model.AuditLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 500")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs WHERE user LIKE '%' || :query || '%' OR action LIKE '%' || :query || '%' OR role LIKE '%' || :query || '%' ORDER BY timestamp DESC LIMIT 200")
    fun searchAuditLogs(query: String): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLog)

    @Query("DELETE FROM audit_logs")
    suspend fun clearAllLogs()
}
