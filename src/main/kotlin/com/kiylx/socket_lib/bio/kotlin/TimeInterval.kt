package com.kiylx.socket_lib.bio.kotlin

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class TimeInterval(
    private val interval: Long,//时间间隔
) {
    private var timeLast = System.currentTimeMillis()
    private var lock: ReentrantLock = ReentrantLock()

    fun flushTime() {
        timeLast = System.currentTimeMillis()
    }

    /**
     * 当大于阈值时，执行block块
     */
    fun then(block: () -> Unit) {
        lock.withLock {
            if (System.currentTimeMillis() - timeLast > interval) {
                flushTime()
                block()
            }
        }
    }
}