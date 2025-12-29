@file:Suppress("unused")

package me.kuku.utils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.lang.RuntimeException
import java.lang.StringBuilder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object MD5Utils {

    @JvmStatic
    fun toMD5(inStr: String): String {
        val charArray = inStr.toCharArray()
        val byteArray = ByteArray(charArray.size)
        for (i in charArray.indices) {
            byteArray[i] = charArray[i].code.toByte()
        }
        return toMD5(byteArray)
    }

    @JvmStatic
    fun toMD5(byteArray: ByteArray): String {
        val md5 = try {
            MessageDigest.getInstance("MD5")
        } catch (var6: NoSuchAlgorithmException) {
            throw RuntimeException(var6)
        }
        val md5Bytes = md5.digest(byteArray)
        val hexValue = StringBuilder()
        for (i in md5Bytes.indices) {
            val `val` = md5Bytes[i].toInt() and 0xff
            if (`val` < 16) {
                hexValue.append("0")
            }
            hexValue.append(Integer.toHexString(`val`))
        }
        return hexValue.toString()
    }
}

object HmacMd5Utils {

    @JvmStatic
    fun toHmacMd5(inStr: ByteArray, key: ByteArray): ByteArray {
        val secretKeySpec = SecretKeySpec(key, "HmacMD5")
        val mac = Mac.getInstance(secretKeySpec.algorithm)
        mac.init(secretKeySpec)
        return mac.doFinal(inStr)
    }

    @JvmStatic
    fun toHmacMd5(inStr: String, key: String): String {
        val bytes = toHmacMd5(inStr.toByteArray(), key.toByteArray())
        return HexUtils.byteArrayToHex(bytes)
    }

}

fun String.md5(): String {
    return MD5Utils.toMD5(this)
}

fun ByteArray.md5(): String {
    return MD5Utils.toMD5(this)
}

fun String.hmacMd5(key: String): String {
    return HmacMd5Utils.toHmacMd5(this, key)
}

fun ByteArray.hmacMd5(key: String): String {
    return HmacMd5Utils.toHmacMd5(this, key.toByteArray()).hex()
}