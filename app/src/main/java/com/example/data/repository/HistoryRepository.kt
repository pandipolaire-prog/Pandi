package com.example.data.repository

import com.example.data.database.HistoryDao
import com.example.data.database.HistoryEntity
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {
    val allHistory: Flow<List<HistoryEntity>> = historyDao.getAllHistory()

    suspend fun insert(item: HistoryEntity) {
        historyDao.insertHistory(item)
    }

    suspend fun delete(item: HistoryEntity) {
        historyDao.deleteHistory(item)
    }

    suspend fun deleteById(id: Int) {
        historyDao.deleteById(id)
    }

    suspend fun clearAll() {
        historyDao.clearAllHistory()
    }
}
