package com.jason.model

import kotlinx.serialization.Serializable

@Serializable
data class DanmakuResponse(val status: Int, val list: List<DanmakuEntity>)
