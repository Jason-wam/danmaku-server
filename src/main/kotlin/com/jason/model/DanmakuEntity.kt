package com.jason.model

import kotlinx.serialization.*

@Serializable
data class DanmakuEntity(val id: Int, val time: Long, val type: Int, val text: String, val color: String)
