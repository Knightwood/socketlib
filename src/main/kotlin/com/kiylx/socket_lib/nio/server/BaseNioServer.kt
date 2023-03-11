package com.kiylx.socket_lib.nio.server

import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

class BaseNioServer() {
    lateinit var serverSocketChannel: ServerSocketChannel
    lateinit var selector: Selector
    var stop: Boolean = false
    val clientMap: Map<String, SocketChannel> = hashMapOf()//记录客户端信息

    fun open(): BaseNioServer {
        selector = Selector.open()
        serverSocketChannel = ServerSocketChannel.open().apply {
            configureBlocking(false)
        }
        return this
    }

    fun listen(local: InetSocketAddress, backlog: Int = 50) {
        if (!this::serverSocketChannel.isInitialized) {
            //未初始化，进行初始化
            open()
        }
        serverSocketChannel.run {
            bind(local, backlog)
            /*
                SelectionKey.OP_ACCEPT —— 接收连接进行事件，表示服务器监听到了客户连接，那么服务器可以接收这个连接了SelectionKey.OP_CONNECT —— 连接就绪事件，表示客户与服务器的连接已经建立成功
                SelectionKey.OP_READ  —— 读就绪事件，表示通道中已经有了可读的数据，可以执行读操作了（通道目前有数据，可以进行读操作了）
                SelectionKey.OP_WRITE —— 写就绪事件，表示已经可以向通道写数据了（通道目前可以用于写操作）
                 */
            register(selector, SelectionKey.OP_ACCEPT)
        }

    }

}