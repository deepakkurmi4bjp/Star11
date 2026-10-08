package com.example.data.local

import androidx.room.*
import com.example.data.model.DonationReceipt
import com.example.data.model.ExpenseItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    // Donations
    @Query("SELECT * FROM donation_receipts ORDER BY receiptNumber DESC")
    fun getAllDonations(): Flow<List<DonationReceipt>>

    @Query("SELECT * FROM donation_receipts WHERE receiptNumber = :receiptNo LIMIT 1")
    suspend fun getDonationByReceiptNumber(receiptNo: String): DonationReceipt?

    @Query("SELECT * FROM donation_receipts WHERE verificationToken = :token LIMIT 1")
    suspend fun getDonationByToken(token: String): DonationReceipt?

    @Query("SELECT * FROM donation_receipts WHERE collectorName = :collector ORDER BY receiptNumber DESC")
    fun getDonationsByCollector(collector: String): Flow<List<DonationReceipt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonation(donation: DonationReceipt)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonations(donations: List<DonationReceipt>)

    @Update
    suspend fun updateDonation(donation: DonationReceipt)

    @Query("UPDATE donation_receipts SET status = :status WHERE receiptNumber = :receiptNo")
    suspend fun updateDonationStatus(receiptNo: String, status: String)

    @Query("SELECT SUM(amount) FROM donation_receipts WHERE status = 'VERIFIED'")
    fun getTotalDonationsSum(): Flow<Double?>

    // Expenses
    @Query("SELECT * FROM expenses ORDER BY date DESC, expenseId DESC")
    fun getAllExpenses(): Flow<List<ExpenseItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseItem>)

    @Update
    suspend fun updateExpense(expense: ExpenseItem)

    @Delete
    suspend fun deleteExpense(expense: ExpenseItem)

    @Query("SELECT SUM(amount) FROM expenses WHERE approvalStatus = 'APPROVED'")
    fun getTotalExpensesSum(): Flow<Double?>
}
