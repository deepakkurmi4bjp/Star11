package com.example.data.local

import androidx.room.*
import com.example.data.model.CheckinRecord
import com.example.data.model.PassItem
import com.example.data.model.SeatItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PassSeatDao {
    // Passes
    @Query("SELECT * FROM passes ORDER BY createdAt DESC")
    fun getAllPasses(): Flow<List<PassItem>>

    @Query("SELECT * FROM passes WHERE passId = :passId LIMIT 1")
    suspend fun getPassById(passId: String): PassItem?

    @Query("SELECT * FROM passes WHERE qrToken = :token LIMIT 1")
    suspend fun getPassByToken(token: String): PassItem?

    @Query("SELECT * FROM passes WHERE registrationId = :regId LIMIT 1")
    suspend fun getPassByRegistrationId(regId: String): PassItem?

    @Query("SELECT * FROM passes WHERE registrationId = :regId")
    fun observePassByRegistrationId(regId: String): Flow<PassItem?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPass(pass: PassItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPasses(passes: List<PassItem>)

    @Update
    suspend fun updatePass(pass: PassItem)

    @Query("UPDATE passes SET status = :status WHERE passId = :passId")
    suspend fun updatePassStatus(passId: String, status: String)

    @Query("UPDATE passes SET seatNumber = :seatNum, section = :sec, row = :row WHERE passId = :passId")
    suspend fun updatePassSeat(passId: String, seatNum: String, sec: String, row: String)

    // Seats
    @Query("SELECT * FROM seats ORDER BY section, row, seatNumber")
    fun getAllSeats(): Flow<List<SeatItem>>

    @Query("SELECT * FROM seats WHERE section = :section ORDER BY row, seatNumber")
    fun getSeatsBySection(section: String): Flow<List<SeatItem>>

    @Query("SELECT * FROM seats WHERE seatNumber = :seatNum LIMIT 1")
    suspend fun getSeatByNumber(seatNum: String): SeatItem?

    @Query("SELECT * FROM seats WHERE status = 'AVAILABLE' ORDER BY section, row, seatNumber")
    suspend fun getAvailableSeats(): List<SeatItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeats(seats: List<SeatItem>)

    @Update
    suspend fun updateSeat(seat: SeatItem)

    @Query("UPDATE seats SET status = :status, assignedDevoteeId = :devoteeId, assignedDevoteeName = :name, assignedGroupId = :grpId WHERE seatNumber = :seatNum")
    suspend fun assignSeat(seatNum: String, status: String, devoteeId: String?, name: String?, grpId: String?)

    @Query("UPDATE seats SET status = 'AVAILABLE', assignedDevoteeId = null, assignedDevoteeName = null, assignedGroupId = null WHERE seatNumber = :seatNum")
    suspend fun releaseSeat(seatNum: String)

    @Query("UPDATE seats SET status = 'AVAILABLE', assignedDevoteeId = null, assignedDevoteeName = null, assignedGroupId = null WHERE assignedDevoteeId = :devoteeId")
    suspend fun releaseSeatByDevoteeId(devoteeId: String)

    // Check-in
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckin(checkin: CheckinRecord)

    @Query("SELECT * FROM checkins WHERE passId = :passId LIMIT 1")
    suspend fun getCheckinByPassId(passId: String): CheckinRecord?

    @Query("SELECT * FROM checkins ORDER BY checkinTimestamp DESC")
    fun getAllCheckins(): Flow<List<CheckinRecord>>

    @Query("SELECT COUNT(*) FROM checkins WHERE status = 'CHECKED_IN'")
    fun getCheckedInCount(): Flow<Int>
}
