package com.kiylx.socket_lib.bio.msg

import com.kiylx.socket_lib.bio.kotlin.ProcessContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.*

/**
 * 解析socket发来的消息
 * 向socket发送消息
 */
class ByteArrayMessageParser : AMessageParser() {
    //字节数组类型的flow
    protected var _byteArrMsgFlow: MutableStateFlow<ByteArray> = MutableStateFlow(byteArrayOf())//通过这个flow，可以监听socket发来的消息
    override val byteArrMsgFlow: StateFlow<ByteArray> get() = _byteArrMsgFlow

    private var stop: Boolean = false

    //接收
    private lateinit var bufferedInputStream: BufferedInputStream
    var inputStreamBufferLen = 8192
    private val inputStreamBuffer: ByteArray = ByteArray(inputStreamBufferLen)

    //发送
    private var bufferedOutputStream: BufferedOutputStream? = null//向socket发送消息
    var outputStreamBufferSize: Int = 8192

    override fun close() {
        stop = true
        bufferedInputStream.close()
        bufferedOutputStream?.close()
    }

    override fun parseInputStream() {
        if (::bufferedInputStream.isInitialized) {
            throw Exception("bufferedInputStream已经初始化")
        }
        val inputStream: InputStream = socket!!.getInputStream()
        bufferedInputStream = BufferedInputStream(inputStream)
        while (!stop) {
            if (isRunning() && canRead()) {
                val i = bufferedInputStream.read(inputStreamBuffer, 0, inputStreamBufferLen)
                if (i != -1) {
                    _byteArrMsgFlow.tryEmit(inputStreamBuffer)
                    ProcessContext.logger.info(inputStreamBuffer.decodeToString())
                } else {
                    ProcessContext.logger.info("消息结束")
                }
            } else {
                stop = true
                ProcessContext.logger.info("socket结束")
            }
        }
    }

    override suspend fun sendDataToStream(msg: ByteArray, off: Int, len: Int): Unit =
        withContext(Dispatchers.IO) {
            try {
                bufferedOutputStream?.let {
                    it.write(msg, off, len)
                    it.flush()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    override fun provideOutputStream(outputStream: OutputStream) {
        bufferedOutputStream = BufferedOutputStream(outputStream, outputStreamBufferSize)
    }
}
