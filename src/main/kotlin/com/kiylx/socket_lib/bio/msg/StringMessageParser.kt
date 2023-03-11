package com.kiylx.socket_lib.bio.msg

import com.kiylx.socket_lib.bio.kotlin.ProcessContext
import com.kiylx.socket_lib.bio.kotlin.TimeInterval
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*

/**
 * 解析socket发来的消息
 * 向socket发送消息
 */
class StringMessageParser(
    val initValue: String = "",//flow的初始值
    val endValue: String = "e-n?d",//flow结束值，socket结束时会发送此值
) : AMessageParser() {
    //字符串类型的flow
    private var _msgFlow: MutableStateFlow<String> = MutableStateFlow(initValue)//通过这个flow，可以监听socket发来的消息
    override val strMsgFlow: StateFlow<String> get() = _msgFlow

    private var stop: Boolean = false
    private val timeInterval = TimeInterval(3000L)

    //接收
    private lateinit var bufferedReader: BufferedReader

    //发送
    private var bufferedOutputStream: BufferedOutputStream? = null//向socket发送消息
    var outputStreamBufferSize: Int = 8192

    override fun close() {
        stop = true
        bufferedReader.close()
        bufferedOutputStream?.close()
    }


    override fun parseInputStream() {
        try {
            ProcessContext.coroutineScope.launch(Dispatchers.IO) {
                if (::bufferedReader.isInitialized) {
                    throw Exception("bufferedReader已经初始化")
                }
                val inputStream: InputStream = socket!!.getInputStream()
                bufferedReader = BufferedReader(InputStreamReader(inputStream))
//            val dataInputStream = DataInputStream(BufferedInputStream(inputStream));
                while (!stop) {
                    if (isRunning() and canRead()) {
                        val i = bufferedReader.readLine()
                        if (i != null) {
                            timeInterval.flushTime()
                            _msgFlow.emit(i)
                        } else {
                            timeInterval.then { //超时关闭流
                                stop = true
                                println("fuck end\n")
                                _msgFlow.tryEmit(endValue)
                            }
                            println("没消息了\n")
                        }
                    } else {
                        stop = true
                        println("fuck end\n")
                        _msgFlow.emit(endValue)
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
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