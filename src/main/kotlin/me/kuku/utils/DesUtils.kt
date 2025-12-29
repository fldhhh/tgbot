@file:Suppress("unused")

package me.kuku.utils

import java.security.Key
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.crypto.spec.IvParameterSpec

object DesUtils {

//    private const val IV_PARAMETER = "12345678"
    private const val ALGORITHM = "DES"
    private const val CIPHER_ALGORITHM = "DES/ECB/PKCS5Padding"

    private fun generateKey(key: ByteArray): Key {
        val dks = DESKeySpec(key)
        val keyFactory = SecretKeyFactory.getInstance(ALGORITHM)
        return keyFactory.generateSecret(dks)
    }

    private fun generateKey(key: String): Key =
        generateKey(key.toByteArray())

    fun encrypt(data: ByteArray, key: ByteArray): String {
        val secretKey = generateKey(key)
        val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
//        val iv = IvParameterSpec(IV_PARAMETER.toByteArray())
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val bytes = cipher.doFinal(data)
        return bytes.base64Encode()
    }

    @JvmStatic
    fun encrypt(data: String, key: String): String {
        return encrypt(data.toByteArray(), key.toByteArray())
    }

    fun decrypt(data: ByteArray, key: ByteArray): String {
        val secretKey = generateKey(key)
        val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
//        val iv = IvParameterSpec(IV_PARAMETER.toByteArray())
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val bytes = cipher.doFinal(data.base64Decode())
        return String(bytes)
    }

    @JvmStatic
    fun decrypt(data: String, password: String) = decrypt(data.toByteArray(), password.toByteArray())

}

fun String.desEncrypt(key: String): String {
    return DesUtils.encrypt(this, key)
}

fun ByteArray.desEncrypt(key: String): String {
    return DesUtils.encrypt(this, key.toByteArray())
}

fun String.desDecrypt(key: String): String {
    return DesUtils.decrypt(this, key)
}