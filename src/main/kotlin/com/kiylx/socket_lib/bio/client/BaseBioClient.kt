package com.kiylx.socket_lib.bio.client

import com.kiylx.socket_lib.bio.kotlin.KCloseable
import com.kiylx.socket_lib.bio.kotlin.ProcessContext
import com.kiylx.socket_lib.bio.msg.AMessageParser
import com.kiylx.socket_lib.bio.msg.ByteArrayMessageParser
import kotlinx.coroutines.launch
import java.net.Socket

class BaseBioClient(ip: String, port: Int) : KCloseable {
    private var client: Socket = Socket(ip, port)
    private var isRunning = false

    //解析socket发来的消息
    lateinit var msgParser: AMessageParser
        internal set

    //socket的信息
    val address get() = client.inetAddress
    val port get() = client.port
    val localPort get() = client.localPort
    val ip get() = client.inetAddress.hostAddress
    val hostName get() = client.inetAddress.hostName

    override fun close() {
        msgParser.close()
    }

    private fun connect(msgParser: AMessageParser = ByteArrayMessageParser()): BaseBioClient {
        this.msgParser = msgParser
        if (!isRunning) {
            isRunning = true
            msgParser.initMessageParser(client)
        }
        return this
    }

    fun send(msg: ByteArray) {
        ProcessContext.coroutineScope.launch {
            send(msg, 0, msg.size)
        }
    }

    suspend fun send(msg: ByteArray, off: Int, len: Int) {
        if (client.isConnected) {
            msgParser.sendDataToStream(msg, off, len)
        }
    }

    companion object {
        fun open(ip: String, port: Int, msgParser: AMessageParser = ByteArrayMessageParser()): BaseBioClient {
            return BaseBioClient(ip, port).connect(msgParser)
        }
    }
}