@file:Suppress("unused")

package me.kuku.utils

import java.lang.StringBuilder

object HexUtils {
    @JvmStatic
    fun byteArrayToHex(src: ByteArray): String {
        val sb = StringBuilder()
        for (element in src) {
            val v = element.toInt() and 0xFF
            val hv = Integer.toHexString(v)
            if (hv.length < 2) {
                sb.append(0)
            }
            sb.append(hv)
        }
        return sb.toString()
    }

    private fun hexToByte(inHex: String): Byte {
        return inHex.toInt(16).toByte()
    }

    @JvmStatic
    fun hexToByteArray(tInHex: String): ByteArray {
        var inHex = tInHex
        var hexLen = inHex.length
        val result = if (hexLen % 2 == 1) {
            hexLen++
            val ss = ByteArray(hexLen / 2)
            inHex = "0$inHex"
            ss
        } else ByteArray(hexLen / 2)
        for ((j, i) in (0 until  hexLen step 2).withIndex()) {
            result[j] = hexToByte(inHex.substring(i, i + 2))
        }
        return result
    }
}

fun ByteArray.hex(): String {
    return HexUtils.byteArrayToHex(this)
}