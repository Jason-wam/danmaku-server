package com.jason.database

import com.jason.database.DatabaseFactory.dbQuery
import com.jason.model.DanmakuEntity
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class DanmakuDaoImpl : DanmakuDao {
    override suspend fun count(): Long = dbQuery {
        Danmakus.selectAll().count()
    }

    override suspend fun count(parent: String): Long = dbQuery {
        Danmakus.select {
            Danmakus.parent eq parent
        }.count()
    }

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

    override suspend fun addDanmaku(
        parent: String,
        time: Long,
        type: Int,
        text: String,
        size: Int,
        color: String
    ): Boolean = dbQuery {
        Danmakus.insert {
            it[Danmakus.text] = text
            it[Danmakus.time] = time
            it[Danmakus.type] = type
            it[Danmakus.size] = size
            it[Danmakus.color] = color
            it[Danmakus.parent] = parent
        }.resultedValues.orEmpty().isNotEmpty()
    }

    private fun ResultRow.resultRowToDanmaku() = DanmakuEntity(
        id = this[Danmakus.id],
        text = this[Danmakus.text],
        size = this[Danmakus.size],
        time = this[Danmakus.time],
        color = this[Danmakus.color],
        type = this[Danmakus.type],
    )
}