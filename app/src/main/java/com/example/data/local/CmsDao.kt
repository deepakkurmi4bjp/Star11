package com.example.data.local

import androidx.room.*
import com.example.data.model.CmsItem
import com.example.data.model.FaqItem
import com.example.data.model.NoticeItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CmsDao {
    // CMS content
    @Query("SELECT * FROM cms_content")
    fun getAllCmsContent(): Flow<List<CmsItem>>

    @Query("SELECT value FROM cms_content WHERE `key` = :key LIMIT 1")
    suspend fun getCmsValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCmsItems(items: List<CmsItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setCmsValue(item: CmsItem)

    // Notices
    @Query("SELECT * FROM notices WHERE isActive = 1 ORDER BY priority = 'HIGH' DESC, id DESC")
    fun getActiveNotices(): Flow<List<NoticeItem>>

    @Query("SELECT * FROM notices ORDER BY id DESC")
    fun getAllNotices(): Flow<List<NoticeItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotices(notices: List<NoticeItem>)

    @Update
    suspend fun updateNotice(notice: NoticeItem)

    @Delete
    suspend fun deleteNotice(notice: NoticeItem)

    // FAQs
    @Query("SELECT * FROM faqs ORDER BY displayOrder ASC, id ASC")
    fun getAllFaqs(): Flow<List<FaqItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaqs(faqs: List<FaqItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaq(faq: FaqItem)

    @Delete
    suspend fun deleteFaq(faq: FaqItem)
}
