import com.kiylx.socket_lib.bio.client.BaseBioClient
import com.kiylx.socket_lib.bio.kotlin.ProcessContext
import com.kiylx.socket_lib.bio.msg.StringMessageParser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    test()
//    test2()
//    testByteConvert()
//    testByteConvert2()

}

/**
 * 测试socke连接到服务端
 */
fun test() {
    runBlocking {
        val msgParser = StringMessageParser(initValue = "初始值")
        ProcessContext.coroutineScope.launch {
            //在连接之前监听，会监听到初始值
            //也可以在连接之后监听，可能会错过一次或多次消息
            msgParser.strMsgFlow.collect {
                println("client,flow: $it")
            }
        }
        val client = BaseBioClient.open("192.168.0.63", 2233, msgParser)
//            ProcessContext.coroutineScope.launch {
//                //也可以在连接之后监听，可能会错过一次或多次消息
//                client.msgParser.strMsgFlow.collect {
//                    println("client,flow: $it")
//                }
//            }
        //发送消息
        client.send("测试\n".toByteArray())
        delay(20000)
        print("主线程结束")
    }
}

fun test2() {
    val socket = Socket("192.168.0.63", 2233)
    val input = socket.getInputStream()
    val buffer = BufferedReader(InputStreamReader(input))
    thread {
        socket.getOutputStream().write("测试\n".toByteArray())
    }
    while (true) {
        println(buffer.readLine())
    }
}


/*
其中有两个位运算，一个是>>，一个是&。

0xff的作用一:
十六进制0xff的长度是一个字节，即八位，二进制为：1111 1111，那么一个 8bit 数与1111 1111与运算还是这个数本身，但是一个16bit 数与 0xff就被截断了，
比如 [11001101 11001100] & 0xff结果为 [11001100]。那如果想不被截断怎么办？把0xff扩展为二个字节即：0xffff，那么以此类推，0xffffff,0xffffffff都出来了。

0xff的作用二:
java专属，由于java没有unsigned类型，所以为了适应与其他语言二进制通讯时各种数据的一致性，需要做一些处理。
最直观的例子：int a = -127 & 0xFF ; // 等同于 unsigned int c = 129; (这里的-127与129是字节，只为了直观而写的具体数字)
这里要严格说明一点：再32位机器上，0xff实际上是 0x00000000 00000000 00000000 11111111，
而-127是 11111111 11111111 11111111 10000001 (补码形式), 那么-127 & 0xff的结果自然是
00000000 00000000 00000000 10000001 即 129.
简而言之，该作用主要是为了将 有符号数转换为无符号数。

>>8的作用：
这个是根据需求而定的，可以是>>8也可以是>>16,>>24,等等
而跟 & 0xff运算的意义其实就是截断，将123456的高位右移8位，通过0xff截取出来。
实际意义就是取字节，比如一个4字节的数，需要将每个字节内容取出来转换出目标数据，那么通过>> 并且 &0xff 运算 就可以去除想要的部分。
再详细点：4字节 ，32 位，按照大端方式排列，

最高位                      最低位
11111111 10101010 11000011 10101010
最高位8字节要移到最低位那么，这个8个字节>>（3*8），然后与0xff运算，取出. 然后后续得>>(2*8) & 0xff ;>>(1*8) & 0xff,均可取出。


java是大端存储
例如：
int num=1505;
byte[] result=new byte[4];

这样的一个整数，在java内存以二进制形式表示为：
+-----------------+---------------+---------------+---------------+
|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|1|0|1|1|1|1|0|0|0|0|1|
+-----------------+---------------+---------------+---------------+
byte[0]=00000000=0; 内存地址低位。存储了数据的高位
byte[1]=00000000=0;
byte[2]=00000101=5;
byte[3]=11100001=-31; 内存地址高位。存储了数据的低位
ps：二进制的数据写在纸上，从左往右是高位到低位
所以上面byte数组下标从0到3，代表的是内存地址由低到高，存储的二进制数据，则是从高位到低位

 */
fun testByteConvert() {
    val a = 1234567890
    val b1 = ByteArray(4)
    //字符串“a”应该是大端存储，将字节数据取出来，还是大端存储的放进字节数组
    b1[0] = (a shr 24 and 0xff).toByte()//右移24位并转换符号，把最高的8位取出来
    b1[1] = (a shr 16 and 0xff).toByte()
    b1[2] = (a shr 8 and 0xff).toByte()
    b1[3] = (a and 0xff).toByte()

    //想把数据从字节数组中读出来，由于是大端存储，b1[0]存储的是二进制数据的高位，所以要左移24位，以此类推
    val b2 = (b1[0].toInt() and 0xff shl 24) //先& oxff转换符号，再左移24位，后面补零
        .or(b1[1].toInt() and 0xff shl 16) //& oxff转换符号，左移16位，前后面补零
        .or(b1[2].toInt() and 0xff shl 8)//& oxff转换符号，左移8位，后前面补零
        .or(b1[3].toInt() and 0xff)//& oxff转换符号，左移0位，前面补零

    println(b2) //正确结果
    print("\n")
    val b3 = b1[0].toInt() shl 24 or (b1[1].toInt() shl 16) or (b1[2].toInt() shl 8) or b1[3].toInt()
    println(b3) //错误结果，没有做有符号转无符号操作，导致结果不对。
    print("\n")
}

//测试把大端存储的数据转换到小端存储，并把数据从小端存储的字节数数组中读出来
fun testByteConvert2() {
    val a = 1234567890
    val b1 = ByteArray(4)
    //字符串“a”应该是大端存储，将字节数据取出来，以小端存储的放进字节数组
    b1[0] = (a and 0xff).toByte()
    b1[1] = (a shr 8 and 0xff).toByte()
    b1[2] = (a shr 16 and 0xff).toByte()
    b1[3] = (a shr 24 and 0xff).toByte()

    //想把数据从字节数组中读出来，由于是小端存储，b1[0]存储的是二进制数据的低，所以要右移24位，以此类推
    val b2 = (b1[0].toInt() and 0xff) //先& oxff转换符号，再左移24位，后面补零
        .or(b1[1].toInt() and 0xff shl 8) //& oxff转换符号，左移16位，前后面补零
        .or(b1[2].toInt() and 0xff shl 16)//& oxff转换符号，左移8位，后前面补零
        .or(b1[3].toInt() and 0xff shl 24)//& oxff转换符号，左移0位，前面补零

    println(b2) //正确结果
    print("\n")
}
