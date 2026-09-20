package com.example.data.repository

import com.example.data.local.ScanRecordDao
import com.example.data.local.ScanRecordEntity
import kotlinx.coroutines.flow.Flow

class ScanHistoryRepository(private val scanRecordDao: ScanRecordDao) {

    val allRecords: Flow<List<ScanRecordEntity>> = scanRecordDao.getAllRecords()
    val latestRecord: Flow<ScanRecordEntity?> = scanRecordDao.getLatestRecord()
    val latestTwoRecords: Flow<List<ScanRecordEntity>> = scanRecordDao.getLatestTwoRecords()

    suspend fun saveScanRecord(record: ScanRecordEntity): Long {
        return scanRecordDao.insertRecord(record)
    }

    suspend fun deleteRecord(id: Long) {
        scanRecordDao.deleteRecordById(id)
    }

    suspend fun clearHistory() {
        scanRecordDao.clearAll()
    }
}
