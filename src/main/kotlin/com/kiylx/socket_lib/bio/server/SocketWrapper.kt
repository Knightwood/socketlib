package com.kiylx.socket_lib.bio.server

import com.kiylx.socket_lib.bio.kotlin.KCloseable
import com.kiylx.socket_lib.bio.kotlin.ProcessContext
import com.kiylx.socket_lib.bio.msg.AMessageParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.Socket

class SocketWrapper(
    private val client: Socket,
) : KCloseable {
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

    /**
     * 生成输入和输出流，监听消息传入
     */
    internal fun start() {
        if (!isRunning) {
            isRunning = true
            msgParser.initMessageParser(client)
        }
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
}