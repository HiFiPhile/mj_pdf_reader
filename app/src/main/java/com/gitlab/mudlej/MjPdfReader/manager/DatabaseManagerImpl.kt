package com.gitlab.mudlej.MjPdfReader.manager

import com.gitlab.mudlej.MjPdfReader.repository.AppDatabase
import com.gitlab.mudlej.MjPdfReader.repository.SavedLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DatabaseManagerImpl(private val database: AppDatabase): DatabaseManager {

    override suspend fun saveLocationInBackground(fileHash: String, pageNumber: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            database.savedLocationDao().insert(SavedLocation(fileHash, pageNumber))
        }
    }

    override suspend fun findLocation(fileHash: String): Int {
        return withContext(Dispatchers.IO) {
            database.savedLocationDao().findSavedPage(fileHash) ?: 0
        }
    }
}