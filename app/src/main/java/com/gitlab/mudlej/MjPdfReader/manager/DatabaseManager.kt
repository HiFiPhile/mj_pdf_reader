package com.gitlab.mudlej.MjPdfReader.manager

interface DatabaseManager {

    suspend fun saveLocationInBackground(fileHash: String, pageNumber: Int)

    suspend fun findLocation(fileHash: String): Int

}