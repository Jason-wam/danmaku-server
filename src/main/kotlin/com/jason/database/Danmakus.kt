package com.jason.database

import org.jetbrains.exposed.sql.*

object Danmakus: Table() {
    val id = integer("id").autoIncrement()
    val time = long("time")
    val type = integer("type")
    val size = integer("size")
    val text = varchar("text",128)
    val color = varchar("color",10)//#F3E700
    val parent = varchar("parent",40)

    override val primaryKey = PrimaryKey(id)
}