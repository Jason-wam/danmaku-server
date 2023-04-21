package com.jason.plugins

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
import java.io.File

val dao: DanmakuDao by lazy { DanmakuDaoImpl() }

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
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
                val id = parameters["id"]
                val text = parameters["text"]
                val type = parameters["type"]?.toInt() ?: 1
                val time = parameters["time"]?.toLong() ?: -1
                val color = parameters["color"] ?: "#FFFFFF"

                if (id.isNullOrBlank()) {
                    call.respond(StatusResponse(HttpStatusCode.BadRequest.value, "Id不能为空!"))
                    return@post
                }
                if (text.isNullOrBlank()) {
                    call.respond(StatusResponse(HttpStatusCode.BadRequest.value, "弹幕内容不能为空!"))
                    return@post
                }
                if (time < 0) {
                    call.respond(StatusResponse(HttpStatusCode.BadRequest.value, "弹幕时间不得小于0!"))
                    return@post
                }
                if (dao.addDanmaku(id, time, type, text, color)) {
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
                    val danmakuArr = JSONObject(it.readText()).optJSONArray("list")
                    for (i in 0 until danmakuArr.length()) {
                        val obj = danmakuArr.getJSONObject(i)
                        val time = obj.optLong("time")
                        val type = obj.getInt("type")
                        val text = obj.getString("text")
                        val color = obj.getString("color")
                        add(DanmakuEntity(-1, type = type, text = text, time = time, color = color))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
