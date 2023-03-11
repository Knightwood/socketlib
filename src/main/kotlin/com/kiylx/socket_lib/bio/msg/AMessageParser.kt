package com.kiylx.socket_lib.bio.msg

import com.kiylx.socket_lib.bio.kotlin.KCloseable
import kotlinx.coroutines.flow.StateFlow
import java.io.OutputStream
import java.net.Socket

abstract class AMessageParser : KCloseable {
    protected var socket: Socket? = null

    //子类可以决定使用哪种flow令使用者监听消息
    //字节数组类型的flow
    open val byteArrMsgFlow: StateFlow<ByteArray> get() = throw Exception("别用")

    //string字符串类型的flow
    open val strMsgFlow: StateFlow<String> get() = throw Exception("别用")

    /**
     * socket是否正在运行
     */
    fun isRunning(): Boolean {
        return socket?.let {
            it.isConnected && !it.isClosed
        } ?: false
    }

    /**
     * 是否可以从socket的inputstream读取
     */
    fun canRead(): Boolean {
        return socket?.let {
            !it.isInputShutdown
        } ?: false
    }

    /**
     * socket提供了outputStream
     */
    internal abstract fun provideOutputStream(outputStream: OutputStream)

    /**
     * socket提供了inputStream，通过此方法不停读取socket发来的消息
     */
    internal abstract fun parseInputStream()

    /**
     * 向socket发送消息
     */
    internal abstract suspend fun sendDataToStream(msg: ByteArray, off: Int, len: Int)

    internal fun initMessageParser(client: Socket) {
        this.socket = client
        provideOutputStream(client.getOutputStream())
        parseInputStream()
    }

}

enum class MessageFlowKind {
    STRING,
    BYTE_ARRAY,
}