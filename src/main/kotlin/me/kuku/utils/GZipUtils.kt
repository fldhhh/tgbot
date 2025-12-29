@file:Suppress("unused")

package me.kuku.utils

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object GZipUtils {

    fun gzip(content: ByteArray): ByteArray {
        val bao = ByteArrayOutputStream()
        val gos = GZIPOutputStream(bao)
        val bai = ByteArrayInputStream(content)
        val buffer = ByteArray(1024)
        var n: Int
        while (bai.read(buffer).also { n = it } != -1) {
            gos.write(buffer, 0, n)
        }
        gos.flush()
        gos.close()
        return bao.toByteArray()
    }

    fun unGzip(content: ByteArray): ByteArray {
        val bao = ByteArrayOutputStream()
        val gis = GZIPInputStream(ByteArrayInputStream(content))
        val buffer = ByteArray(1024)
        var n: Int
        while (gis.read(buffer).also { n = it } != -1) {
            bao.write(buffer, 0, n)
        }
        return bao.toByteArray()
    }
}