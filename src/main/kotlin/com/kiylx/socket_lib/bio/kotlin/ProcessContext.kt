package com.kiylx.socket_lib.bio.kotlin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.logging.Logger

object ProcessContext {
    private val job: Job = Job()
    val coroutineScope: CoroutineScope = CoroutineScope(job + Dispatchers.IO)
    val logger: Logger = Logger.getLogger("main")
    val threadPool = Executors.newCachedThreadPool()
}

fun runWithIo(block: suspend CoroutineScope.() -> Unit): Result<Job> {
    return runCatching {
        val job = ProcessContext.coroutineScope.launch(Dispatchers.IO) {
            block()
        }
        return@runCatching job
    }
}