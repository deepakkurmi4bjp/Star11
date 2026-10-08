package com.example.data.local

import androidx.room.*
import com.example.data.model.EventProgram
import kotlinx.coroutines.flow.Flow

@Dao
interface EventProgramDao {
    @Query("SELECT * FROM event_programs ORDER BY date, time ASC")
    fun getAllPrograms(): Flow<List<EventProgram>>

    @Query("SELECT * FROM event_programs WHERE date = :date ORDER BY time ASC")
    fun getProgramsByDate(date: String): Flow<List<EventProgram>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgram(program: EventProgram)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrograms(programs: List<EventProgram>)

    @Update
    suspend fun updateProgram(program: EventProgram)

    @Delete
    suspend fun deleteProgram(program: EventProgram)
}
