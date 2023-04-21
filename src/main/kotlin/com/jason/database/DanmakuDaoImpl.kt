package com.jason.database

import com.jason.database.DatabaseFactory.dbQuery
import com.jason.model.DanmakuEntity
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class DanmakuDaoImpl : DanmakuDao {
    override suspend fun delete(id: Int): Boolean = dbQuery {
        Danmakus.deleteWhere {
            Danmakus.id eq id
        } > 0
    }

    override suspend fun deleteAll(parent: String): Boolean = dbQuery {
        Danmakus.deleteWhere {
            Danmakus.parent eq parent
        } > 0
    }

    override suspend fun allDanmakus(parent: String): List<DanmakuEntity> = dbQuery {
        Danmakus.select {
            Danmakus.parent eq parent
        }.map {
            it.resultRowToDanmaku()
        }
    }

    override suspend fun allDanmakus(parent: String, limit: Int): List<DanmakuEntity> = dbQuery {
        Danmakus.select {
            Danmakus.parent eq parent
        }.limit(limit).map {
            it.resultRowToDanmaku()
        }
    }

    override suspend fun addDanmaku(parent: String, time: Long, type: Int, text: String, color: String): Boolean = dbQuery {
        Danmakus.insert {
            it[Danmakus.text] = text
            it[Danmakus.time] = time
            it[Danmakus.type] = type
            it[Danmakus.color] = color
            it[Danmakus.parent] = parent
        }.resultedValues.orEmpty().isNotEmpty()
    }

    private fun ResultRow.resultRowToDanmaku() = DanmakuEntity(
            id = this[Danmakus.id],
            text = this[Danmakus.text],
            time = this[Danmakus.time],
            color = this[Danmakus.color],
            type = this[Danmakus.type],
    )
}