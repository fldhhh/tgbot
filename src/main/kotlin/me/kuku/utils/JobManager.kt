@file:Suppress("unused")

package me.kuku.utils

import kotlinx.coroutines.*
import java.lang.Runnable

object JobManager {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    @JvmStatic
    fun now(runnable: Runnable): Job {
        return coroutineScope.launch {
            try {
                runnable.run()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun now(block: suspend CoroutineScope.() -> Unit): Job {
        return coroutineScope.launch {
            kotlin.runCatching {
                block()
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    suspend fun <T> await(delay: Long = 0, block: suspend CoroutineScope.() -> T): T {
        val ss = coroutineScope.async {
            delay(delay)
            block()
        }
        return ss.await()
    }

    @JvmStatic
    fun delay(wait: Long, runnable: Runnable): Job {
        return coroutineScope.launch {
            delay(wait)
            try {
                runnable.run()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun delay(wait: Long, block: suspend CoroutineScope.() -> Unit): Job {
        return coroutineScope.launch {
            delay(wait)
            kotlin.runCatching {
                block()
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    @JvmStatic
    @JvmOverloads
    fun every(wait: Long = 0, runnable: Runnable): Job {
        return coroutineScope.launch {
            while (true) {
                delay(wait)
                try {
                    runnable.run()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun every(wait: Long = 0, block: suspend CoroutineScope.() -> Unit): Job {
        return coroutineScope.launch {
            while (true) {
                delay(wait)
                kotlin.runCatching {
                    block()
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
    }

    fun every(waitStr: String, block: suspend CoroutineScope.() -> Unit): Job {
        val chat = waitStr.last()
        val wait = waitStr.substring(0, waitStr.length - 1).toLong()
        val finally = wait * when (chat) {
            's' -> 1000
            'm' -> 1000 * 60
            'h' -> 1000 * 60 * 60
            'd' -> 1000 * 60 * 60 * 24
            else -> 1000
        }
        return coroutineScope.launch {
            while (true) {
                delay(finally)
                kotlin.runCatching {
                    block()
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
    }

    private fun firstDelay(atTime: String): Long {
        val sss = atTime.split(" ")
        val time = if (sss.size == 1) sss[0] else sss[1]
        val arr = time.split(":")
        var str = when (arr.size) {
            1 -> "$time:00:00"
            2 -> "$time:00"
            else -> time
        }
        str = if (sss.size == 1) {
            val nowStrTime = DateTimeFormatterUtils.formatNow("yyyy-MM-dd ")
            nowStrTime + str
        } else "${sss[0]} $str"
        val todayCronTime = DateTimeFormatterUtils.parse(str, "yyyy-MM-dd HH:mm:ss")
        val nowTime = System.currentTimeMillis()
        return if (todayCronTime > nowTime) todayCronTime - nowTime
        else todayCronTime + 1000 * 60 * 60 * 24 - nowTime
    }

    // 2022-03-02 11:33:22   || 11:33:22
    fun atTime(atTime: String, always: Boolean = false, block: suspend CoroutineScope.() -> Unit): Job {
        val firstDelay = firstDelay(atTime)
        return coroutineScope.launch {
            if (always) {
                delay(firstDelay)
                while (true) {
                    kotlin.runCatching {
                        block()
                    }.onFailure {
                        it.printStackTrace()
                    }
                    delay(1000 * 60 * 60 * 24)
                }
            } else {
                delay(firstDelay)
                kotlin.runCatching {
                    block()
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
    }

    @JvmStatic
    @JvmOverloads
    fun atTime(atTime: String, always: Boolean = false, runnable: Runnable): Job {
        val firstDelay = firstDelay(atTime)
        return coroutineScope.launch {
            if (always) {
                delay(firstDelay)
                while (true) {
                    kotlin.runCatching {
                        runnable.run()
                    }.onFailure {
                        it.printStackTrace()
                    }
                    delay(1000 * 60 * 60 * 24)
                }
            } else {
                delay(firstDelay)
                kotlin.runCatching {
                    runnable.run()
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
    }

}
