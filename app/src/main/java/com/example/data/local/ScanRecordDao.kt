package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanRecordDao {
    @Query("SELECT * FROM scan_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<ScanRecordEntity>>

    @Query("SELECT * FROM scan_records ORDER BY timestamp DESC LIMIT 1")
    fun getLatestRecord(): Flow<ScanRecordEntity?>

    @Query("SELECT * FROM scan_records ORDER BY timestamp DESC LIMIT 2")
    fun getLatestTwoRecords(): Flow<List<ScanRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: ScanRecordEntity): Long

    @Query("DELETE FROM scan_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM scan_records")
    suspend fun clearAll()
}
