@file:Suppress("unused")

package me.kuku.utils

import java.io.*
import java.nio.file.Files

object IOUtils {

    private val tmpLocation = File("tmp")
    private const val tmp = "tmp"

    @JvmStatic
    @JvmOverloads
    fun writeTmpFile(fileName: String, `is`: InputStream, isClose: Boolean = true): File {
        if (!tmpLocation.exists()) tmpLocation.mkdir()
        val path = tmp + File.separator + fileName
        val fos: FileOutputStream?
        try {
            fos = FileOutputStream(path)
            write(`is`, fos, isClose)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return File(path)
    }

    @JvmStatic
    fun writeTmpFile(fileName: String, bytes: ByteArray): File {
        val path = Files.createTempFile("me/kuku", fileName)
        Files.write(path, bytes)
        return path.toFile()
    }

    @JvmStatic
    fun close(closeable: Closeable?) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @JvmStatic
    fun read(file: File): ByteArray {
        return file.readBytes()
    }

    @JvmStatic
    @JvmOverloads
    fun read(fis: InputStream, isClose: Boolean = true): ByteArray {
        return fis.readBytes().also { if (isClose) fis.close() }
    }

    @JvmStatic
    @JvmOverloads
    fun write(`is`: InputStream, os: OutputStream, isClose: Boolean = true) {
        try {
            val buffer = ByteArray(1024)
            var len: Int
            while (`is`.read(buffer).also { len = it } != -1) {
                os.write(buffer, 0, len)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (isClose) {
                close(`is`)
            }
            close(os)
        }
    }
}
