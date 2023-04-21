package com.jason.database

import com.jason.model.DanmakuEntity

interface DanmakuDao {
    suspend fun delete(id: Int): Boolean
    suspend fun deleteAll(parent: String): Boolean
    suspend fun allDanmakus(parent: String): List<DanmakuEntity>
    suspend fun allDanmakus(parent: String, limit: Int): List<DanmakuEntity>
    suspend fun addDanmaku(parent: String, time: Long, type: Int, text: String, color: String): Boolean
}