package com.jason

import com.jason.database.DatabaseFactory
import com.jason.plugins.configureHTTP
import com.jason.plugins.configureRouting
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import java.awt.Color

fun main(args: Array<String>) {
//    println(parseColor("#F2DA03"))
//    println(Color.YELLOW)
    args.find {
        it.startsWith("-port=")
    }.let {
        it?.removePrefix("-port=")?.toInt() ?: 8083
    }.also {
        embeddedServer(CIO, port = it, host = "0.0.0.0", module = Application::module).start(wait = true)
    }
}

fun Application.module() {
    DatabaseFactory.init()
    configureHTTP()
    configureRouting()
}
