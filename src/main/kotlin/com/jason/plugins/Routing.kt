package com.jason.plugins

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.datamatrix.encoder.SymbolShapeHint
import com.jason.database.DanmakuDao
import com.jason.database.DanmakuDaoImpl
import com.jason.model.DanmakuEntity
import com.jason.model.DanmakuResponse
import com.jason.model.StatusResponse
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

val dao: DanmakuDao by lazy { DanmakuDaoImpl() }

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        get("/count") {
            call.respondText(dao.count().toString())
        }

        get("/danmaku") {
            val id = call.parameters["id"]
            val version = call.parameters["ver"] //预留参数
            if (id.isNullOrBlank()) {
                call.respond(StatusResponse(HttpStatusCode.BadRequest.value, "Id不能为空!"))
            } else {
                call.respond(DanmakuResponse(HttpStatusCode.OK.value, loadDefaultDanmaku().apply {
                    addAll(dao.allDanmakus(id, 5000))
                }))
            }
        }

        post("/addDanmaku") {
            runCatching {
                val parameters = call.receiveParameters()
                val id = parameters["id"].orEmpty()
                val text = parameters["text"].orEmpty()
                val type = parameters["type"]?.toInt() ?: 1
                val time = parameters["time"]?.toLong() ?: -1
                val size = parameters["size"]?.toInt() ?: 13
                val color = parameters["color"] ?: "#FFFFFF"
                if (isDanmakuBlocked(text)) {
                    call.respond(StatusResponse(HttpStatusCode.BadRequest.value, "弹幕内容违规!"))
                    return@post
                }
                if (id.isBlank()) {
                    call.respond(StatusResponse(HttpStatusCode.BadRequest.value, "Id不能为空!"))
                    return@post
                }
                if (text.isBlank()) {
                    call.respond(StatusResponse(HttpStatusCode.BadRequest.value, "弹幕内容不能为空!"))
                    return@post
                }
                if (time < 0) {
                    call.respond(StatusResponse(HttpStatusCode.BadRequest.value, "弹幕时间不得小于0!"))
                    return@post
                }
                if (dao.addDanmaku(id, time, type, text, size, color)) {
                    call.respond(StatusResponse(HttpStatusCode.OK.value, "弹幕发送成功！"))
                } else {
                    call.respond(StatusResponse(HttpStatusCode.InternalServerError.value, "弹幕发送失败！"))
                }
            }.onFailure {
                it.printStackTrace()
                call.respond(StatusResponse(HttpStatusCode.InternalServerError.value, it.stackTraceToString()))
            }
        }
    }
}

private suspend fun loadDefaultDanmaku(): ArrayList<DanmakuEntity> = withContext(Dispatchers.IO) {
    ArrayList<DanmakuEntity>().apply {
        try { //加载部分预设的弹幕
            val dir = System.getProperty("user.dir")
            val file = File(dir, "danmaku.json")
            if (file.exists()) {
                file.bufferedReader().use {
                    val danmakuArr = JSONObject(it.readText()).optJSONArray("preset")
                    for (i in 0 until danmakuArr.length()) {
                        val obj = danmakuArr.getJSONObject(i)
                        val time = obj.optLong("time")
                        val type = obj.getInt("type")
                        val text = obj.getString("text")
                        val size = obj.optInt("size", 13)
                        val color = obj.getString("color")
                        add(DanmakuEntity(-1, type = type, text = text, time = time, size = size, color = color))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private suspend fun isDanmakuBlocked(input: String): Boolean = withContext(Dispatchers.IO) {
    var blocked = false
    val dir = System.getProperty("user.dir")
    val file = File(dir, "danmaku.json")
    if (file.exists()) {
        file.bufferedReader().use {
            val danmakuArr = JSONObject(it.readText()).optJSONArray("blocked")
            for (i in 0 until danmakuArr.length()) {
                val text = danmakuArr.getString(i)
                if (text.contains(input, ignoreCase = true)) {
                    blocked = true
                }
            }
        }
    }
    return@withContext blocked
}
