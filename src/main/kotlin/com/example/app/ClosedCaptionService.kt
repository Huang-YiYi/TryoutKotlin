package com.example.app

import com.corundumstudio.socketio.Configuration
import com.corundumstudio.socketio.SocketConfig
import com.corundumstudio.socketio.SocketIOServer
import com.example.app.model.ProgrammeList
import com.example.app.model.RequestLineNumberResponse
import com.example.app.model.RequestStateResponse

class ClosedCaptionService constructor(private val port: Int, private val clientRequestListener: ClientRequestListener) {
    private val server: SocketIOServer
    init {
        val config = Configuration()
        config.hostname = "0.0.0.0"
        config.port = port

        val socketConfig = SocketConfig()
        socketConfig.isReuseAddress = true

        config.socketConfig = socketConfig

        server = SocketIOServer(config)
    }

    fun start() {
        server.addEventListener("request-state", String::class.java) { client, data, ackSender ->
            val lines = clientRequestListener.onRequestScript()
            ackSender.sendAckData(RequestStateResponse(lines, clientRequestListener.onRequestCurrentLineNumber()))
        }

        server.addEventListener("request-current-line-number", Object::class.java) { client, data, ackSender ->
            val lineNumber = clientRequestListener.onRequestCurrentLineNumber()
            ackSender.sendAckData(RequestLineNumberResponse(lineNumber))
        }
        server.start()
    }

    fun notifyScriptUpdated() {
        val lines = clientRequestListener.onRequestScript()
        val lineNumber = clientRequestListener.onRequestCurrentLineNumber()
        server.broadcastOperations.sendEvent("script-updated", RequestStateResponse(lines, lineNumber))
    }

    fun setProgrammeList() {
        // TODO confirm with client how this looks like before implementing more
        server.broadcastOperations.sendEvent("set-programme-list", ProgrammeList("05-06-2020"))
    }

    fun setCurrentLineNumber(lineNumber: Int) {
        server.broadcastOperations.sendEvent("set-current-line-number", RequestLineNumberResponse(lineNumber))
    }

    fun close() {
        server.stop()
    }

    interface ClientRequestListener {
        fun onRequestScript(): List<String>
        fun onRequestCurrentLineNumber(): Int
    }
}