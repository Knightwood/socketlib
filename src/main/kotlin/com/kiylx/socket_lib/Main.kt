package com.kiylx.socket_lib

import com.kiylx.socket_lib.bio.kotlin.ProcessContext
import com.kiylx.socket_lib.bio.msg.StringMessageParser
import com.kiylx.socket_lib.bio.server.BaseBioServer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


fun main() {
    runBlocking {
        val server = BaseBioServer()
        //在2233端口开始服务，并监听链接
        server.open(2233).listen(
            StringMessageParser("inity")        //替换消息解析器
        ) { client ->
            //这里可以直接拿到client（接入的socket），这样可以给socket发消息，或是监听消息
            ProcessContext.coroutineScope.launch {
                client.msgParser.strMsgFlow.collect {
                    //监听某个具体的socket发来的消息
                    print("flow：$it \n")
                    //向client发送消息
                    val msg = "hello${System.currentTimeMillis()}\n".toByteArray()
                    client.send(msg)
                }
            }
        }
        // 查找某个client,送发消息
//        val client: SocketWrapper? = server.find("192.168.0.63", 1000)
//        val msg = "hello\n".toByteArray()
//        client?.send(msg)
        delay(20000)
        print("主线程结束")
    }
}