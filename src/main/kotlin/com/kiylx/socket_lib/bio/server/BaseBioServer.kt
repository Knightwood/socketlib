package com.kiylx.socket_lib.bio.server

import com.kiylx.socket_lib.bio.kotlin.KCloseable
import com.kiylx.socket_lib.bio.kotlin.ProcessContext
import com.kiylx.socket_lib.bio.msg.AMessageParser
import com.kiylx.socket_lib.bio.msg.ByteArrayMessageParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

/**
 * 示例：
 *     val server= BaseBioServer().open(2233).listen()
 *     //发送消息-直接发送
 *     server.sendData("192.168.0.2",1000,msg) // msg是ByteArray
 *     //通过socket发送
 *     val client= server.find("192.168.0.2",1000)
 *     //直接发送
 *     client.send(msg) // msg是ByteArray
 *     //或者
 *     client.send(msg,off,len) // msg是ByteArray
 *
 */
class BaseBioServer : KCloseable {
    var isRunning = false
    var stop: Boolean = false
    private val clientList: MutableList<SocketWrapper> = mutableListOf()//记录客户端信息
    private lateinit var serverSocket: ServerSocket

    fun open(port: Int, backlog: Int = 50, bindAddr: InetAddress): BaseBioServer {
        if (!this::serverSocket.isInitialized) {
            serverSocket = ServerSocket(port, backlog, bindAddr)
        }
        return this
    }

    fun open(port: Int): BaseBioServer {
        if (!this::serverSocket.isInitialized) {
            serverSocket = ServerSocket(port)
        }
        return this
    }

    /**
     * @param messageParser 消息解析器，向socket发送或接受消息
     * @param isSuspend true:使用协程; false:使用线程池
     * @param block 对接入socket进一步处理
     */
    fun listen(
        messageParser: AMessageParser? = null,
        isSuspend: Boolean = true,
        block: ((client: SocketWrapper) -> Unit)? = null
    ): BaseBioServer {
        if (!this::serverSocket.isInitialized) {
            throw Exception("serverSocket未初始化")
        }
        if (stop) {
            throw Exception("已经释放资源")
        }
        if (isRunning) {
            throw Exception("不可重复调用")
        }
        if (isSuspend) {
            ProcessContext.coroutineScope.launch(Dispatchers.IO) {
                listenInner(messageParser, block)
            }
        } else {
            ProcessContext.threadPool.execute {
                listenInner(messageParser, block)
            }
        }

        return this
    }

    /**
     * 监听新连接
     */
    private fun listenInner(
        messageParser: AMessageParser?,
        block: ((socketWrapper: SocketWrapper) -> Unit)?
    ) {
        isRunning = true
        while (!stop) {
            val socket: Socket = serverSocket.accept()
            val socketWrapper = SocketWrapper(socket).apply {
                msgParser = messageParser ?: ByteArrayMessageParser()//消息解析发送
            }
            clientList.add(socketWrapper)
//            ProcessContext.logger.info("新连接：${socketWrapper.ip}")
            block?.let {
                //给外界一个处理client的机会
                it(socketWrapper)
            }
            socketWrapper.start()//开始监听连接
        }
    }

    fun find(ipAddress: String, socketPort: Int): SocketWrapper? {
        return clientList.find {
            it.ip == ipAddress && it.port == socketPort
        }
    }

    fun sendData(inetAddress: InetAddress, msg: ByteArray) {
        clientList.find {
            it.address == inetAddress
        }?.send(msg)
    }

    fun sendData(ipAddress: String, socketPort: Int, msg: ByteArray) {
        find(ipAddress, socketPort)?.send(msg)
    }

    override fun close() {
        stop = true
        clientList.forEach {
            it.close()
        }
        clientList.clear()
        serverSocket.close()
    }
}