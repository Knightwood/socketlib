package com.kiylx.socket_lib.convert

import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*


/**
 * 将2个byte数组进行拼接
 */
fun addBytes(data1: ByteArray, data2: ByteArray): ByteArray {
    val data3 = ByteArray(data1.size + data2.size)
    System.arraycopy(data1, 0, data3, 0, data1.size)
    System.arraycopy(data2, 0, data3, data1.size, data2.size)
    return data3
}

/**
 * 重写了Inpustream 中的skip(long n) 方法，
 * 将数据流中起始的n 个字节跳过
 */
fun skipBytesFromStream(inputStream: InputStream, n: Long): Long {
    var remaining = n
    // SKIP_BUFFER_SIZE is used to determine the size of skipBuffer
    val SKIP_BUFFER_SIZE = 2048
    // skipBuffer is initialized in skip(long), if needed.
    val skipBuffer = ByteArray(SKIP_BUFFER_SIZE)
    var nr = 0
    val localSkipBuffer: ByteArray = skipBuffer
    if (n <= 0) {
        return 0
    }
    while (remaining > 0) {
        try {
            nr = inputStream.read(localSkipBuffer, 0, Math.min(SKIP_BUFFER_SIZE.toLong(), remaining).toInt())
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (nr < 0) {
            break
        }
        remaining -= nr.toLong()
    }
    return n - remaining
}

class DataByteArrayUtils {
    companion object {

        /**
         * 十六进制字符串转换十进制
         *
         * @param hex 十六进制字符串
         * @return 十进制数值
         */
        fun hexStringToAlgorism(hex: String): Int {
            var hex = hex
            hex = hex.uppercase(Locale.getDefault())
            val max = hex.length
            var result = 0
            for (i in max downTo 1) {
                val c = hex[i - 1]
                var algorism = 0
                algorism = if (c in '0'..'9') {
                    c.code - '0'.code
                } else {
                    c.code - 55
                }
                result += (Math.pow(16.0, (max - i).toDouble()) * algorism).toInt()
            }
            return result
        }

        /**
         * 字符串转换成十六进制字符串
         *
         * @param str 待转换的ASCII字符串
         * @return String 每个Byte之间空格分隔，如: [61 6C 6B]
         */
        fun str2HexStr(str: String): String {
            val chars = "0123456789ABCDEF".toCharArray()
            val sb = StringBuilder("")
            val bs = str.toByteArray()
            var bit: Int
            for (i in bs.indices) {
                bit = bs[i].toInt() and 0x0f0 shr 4
                sb.append(chars[bit])
                bit = bs[i].toInt() and 0x0f
                sb.append(chars[bit])
                sb.append(' ')
            }
            return sb.toString().trim { it <= ' ' }
        }

        /**
         * 16进制转换成字符串
         *
         * @param hexStr
         * @return
         */
        fun hexStr2Str(hexStr: String): String {
            val str = "0123456789ABCDEF"
            val hexs = hexStr.toCharArray()
            val bytes = ByteArray(hexStr.length / 2)
            var n: Int
            for (i in bytes.indices) {
                n = str.indexOf(hexs[2 * i]) * 16
                n += str.indexOf(hexs[2 * i + 1])
                bytes[i] = (n and 0xff).toByte()
            }
            return String(bytes)
        }

        /**
         * 二进制字符串转十进制
         *
         * @param binary 二进制字符串
         * @return 十进制数值
         */
        fun String.binaryToAlgorism(): Int {
            val max = this.length
            var result = 0
            for (i in max downTo 1) {
                val c = this[i - 1]
                val algorism = c.code - '0'.code
                result += (Math.pow(2.0, (max - i).toDouble()) * algorism).toInt()
            }
            return result
        }


    }
}

//数据转换成大端存储字节数组
//数据转换成小端存储字节数组
class DataToByteArray {
    companion object {
        fun Int.toByteArray(mode: ByteOrder): ByteArray {
            return if (mode == ByteOrder.LITTLE_ENDIAN) {
                val src = ByteArray(4)
                src[0] = (this and 0xff).toByte()
                src[1] = (this shr 8 and 0xff).toByte()
                src[2] = (this shr 16 and 0xff).toByte()
                src[3] = (this shr 24 and 0xff).toByte()
                src
            } else {
                val src = ByteArray(4)
                src[3] = (this and 0xff).toByte()
                src[2] = (this shr 8 and 0xff).toByte()
                src[1] = (this shr 16 and 0xff).toByte()
                src[0] = (this shr 24 and 0xff).toByte()
                src
            }
        }

        /**
         * @param mode 指定此字节数组是什么方式存储的
         */
        fun Short.toByteArray(mode: ByteOrder): ByteArray {
            return ByteBuffer.allocate(2).order(mode).putShort(this).array()
        }

        /**
         * @param mode 指定此字节数组是什么方式存储的
         */
        fun Long.toByteArray(mode: ByteOrder): ByteArray {
            return ByteBuffer.allocate(8).order(mode).putLong(this).array()
        }

        /**
         * 16进制表示的字符串转换为字节数组
         *
         * @param s 16进制表示的字符串
         * @return byte[] 字节数组
         */
        fun String.hexStringToByteArray(): ByteArray {
            val s = this
            val len = this.length
            val b = ByteArray(len / 2)
            var i = 0
            while (i < len) {
                // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
                b[i / 2] = ((s[i].digitToIntOrNull(16)!! shl 4)
                        + s[i + 1].digitToIntOrNull(16)!!).toByte()
                i += 2
            }
            return b
        }

    }
}

//大端存储的字节数组转换成数据
//小端存储的字节数组转换成数据
class ByteArrToData {
    companion object {
        /**
         * @param mode 存储的字节数组类型
         */
        fun ByteArray.toInt(mode: ByteOrder): Int {
            var result = 0
            result = if (mode == ByteOrder.BIG_ENDIAN) {
                (this[0].toInt() and 0xff shl 24) //先& oxff转换符号，再左移24位，后面补零
                    .or(this[1].toInt() and 0xff shl 16) //& oxff转换符号，左移16位，前后面补零
                    .or(this[2].toInt() and 0xff shl 8)//& oxff转换符号，左移8位，后前面补零
                    .or(this[3].toInt() and 0xff)//& oxff转换符号，左移0位，前面补零
            } else {
                (this[0].toInt() and 0xff) //先& oxff转换符号，再左移24位，后面补零
                    .or(this[1].toInt() and 0xff shl 8) //& oxff转换符号，左移16位，前后面补零
                    .or(this[2].toInt() and 0xff shl 16)//& oxff转换符号，左移8位，后前面补零
                    .or(this[3].toInt() and 0xff shl 24)//& oxff转换符号，左移0位，前面补零
            }
            return result
        }

        /**
         * @param mode 指定此字节数组是什么方式存储的
         */
        fun ByteArray.toShort(mode: ByteOrder): Short {
            if (this.size != 2) {
                throw UnsupportedOperationException("the byte length is not 2");
            }
            return ByteBuffer.allocate(this.size).order(mode).put(this).getShort(0)
        }

        /**
         * @param mode 指定此字节数组是什么方式存储的
         */
        fun ByteArray.toLong(mode: ByteOrder): Long {
            if (this.size != 8) {
                throw UnsupportedOperationException("the byte length is not 8");
            }
            return ByteBuffer.allocate(this.size).order(mode).put(this).getLong(0)
        }

        fun ByteArray.toHexString(): String {
            val src = this
            val stringBuilder = java.lang.StringBuilder("")

            for (i in src.indices) {
                val v = src[i].toInt() and 0xFF
                stringBuilder.append("0x")
                val hv = Integer.toHexString(v)
                if (hv.length < 2) {
                    stringBuilder.append(0)
                }
                stringBuilder.append(hv)
                if (i != src.size - 1) {
                    stringBuilder.append(",")
                }
            }
            return stringBuilder.toString()
        }

        /**
         * 字节数组转换为十六进制字符串
         *
         * @return String 十六进制字符串
         */
        fun ByteArray.byte2hex(): String {
            val b = this
            var hs = ""
            var stmp: String
            for (n in b.indices) {
                stmp = Integer.toHexString(b[n].toInt() and 0xff)
                hs = if (stmp.length == 1) {
                    hs + "0" + stmp
                } else {
                    hs + stmp
                }
            }
            return hs.uppercase(Locale.getDefault())
        }

        /**
         * 将字节转换为二进制字符串
         *
         * @return 二进制字符串
         */
        fun ByteArray.byteToBit(): String {
            val sb = StringBuffer()
            var z: Int
            var len: Int
            var str: String
            for (w in indices) {
                z = this[w].toInt()
                z = z or 256
                str = Integer.toBinaryString(z)
                len = str.length
                sb.append(str.substring(len - 8, len))
            }
            return sb.toString()
        }

        /**
         * 字节数组转为普通字符串（ASCII对应的字符）
         *
         * @return String
         */
        fun ByteArray.byte2String(): String? {
            var result: String? = ""
            var temp: Char
            val length = size
            for (i in 0 until length) {
                temp = Char(this[i].toUShort())
                result += temp
            }
            return result
        }
    }
}

fun main() {
    val s="kotlin str!"
}