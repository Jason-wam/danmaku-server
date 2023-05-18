package com.jason

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.datamatrix.encoder.SymbolShapeHint
import com.jason.database.DatabaseFactory
import com.jason.plugins.configureHTTP
import com.jason.plugins.configureRouting
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.response.*
import java.awt.Color

fun main(args: Array<String>) {
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
