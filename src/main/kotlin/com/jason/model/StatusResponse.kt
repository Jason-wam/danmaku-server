package com.jason.model

import kotlinx.serialization.Serializable

@Serializable
data class StatusResponse(val status: Int, val message: String)