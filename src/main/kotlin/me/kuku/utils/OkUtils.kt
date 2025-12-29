@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package me.kuku.utils

import com.fasterxml.jackson.databind.JsonNode
import me.kuku.pojo.UA
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.ByteString
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.file.Path
import java.util.HashMap

object OkUtils {

    private val MEDIA_JSON = "application/json;charset=utf-8".toMediaType()
    private val MEDIA_STREAM = "application/octet-stream".toMediaType()
    private val MEDIA_TEXT = "text/plain;charset=UTF-8".toMediaType()

    @JvmStatic
    fun str(response: Response): String {
        return response.body!!.string()
    }

    @JvmStatic
    fun json(response: Response): JsonNode {
        val str = str(response)
        return str.toJsonNode()
    }

    @JvmStatic
    fun jsonp(response: Response): JsonNode {
        var str = str(response)
        str = str.substring(str.indexOf('{'), str.lastIndexOf('}') + 1)
        return str.toJsonNode()
    }

    @JvmStatic
    fun bytes(response: Response): ByteArray {
        return response.body!!.bytes()
    }

    @JvmStatic
    fun byteStream(response: Response): InputStream {
        return response.body!!.byteStream()
    }

    @JvmStatic
    fun byteString(response: Response): ByteString {
        return response.body!!.byteString()
    }


    @JvmStatic
    fun file(response: Response, path: Path): File {
        val file = path.toFile() ?: File(".")
        return file(response, file)
    }

    @JvmStatic
    @JvmOverloads
    fun file(response: Response, file: File? = null): File {
        var iis: InputStream? = null
        var fos: FileOutputStream? = null
        try {
            iis = byteStream(response)
            val tempFile = file ?: File(".")
            val newFile = if (tempFile.isDirectory) {
                File(tempFile, fileName(response))
            } else tempFile
            fos = FileOutputStream(newFile)
            iis.copyTo(fos)
            return newFile
        } finally {
            fos?.close()
            iis?.close()
        }
    }

    fun fileName(response: Response): String {
        val dis = response.header("Content-Disposition")
        return if (dis != null) {
            MyUtils.regex("""filename="""", "\"", dis) ?: MyUtils.regex(""""(?<=filename=).*"""", dis) ?: "未知文件名"
        } else {
            var url = response.request.url.toString()
            if (url.last() == '/') url = url.removeSuffix("/")
            url.substring(url.lastIndexOf('/') + 1)
        }
    }


    @JvmStatic
    fun json(jsonStr: String): RequestBody {
        return jsonStr.toRequestBody(MEDIA_JSON)
    }

    @JvmStatic
    fun json(any: Any): RequestBody {
        return Jackson.toJsonString(any).toRequestBody(MEDIA_JSON)
    }

    @JvmStatic
    fun text(str: String): RequestBody {
        return str.toRequestBody(MEDIA_TEXT)
    }

    @JvmStatic
    fun body(text: String, contentType: String): RequestBody {
        return text.toRequestBody(contentType.toMediaType())
    }

    // ------------------------------------------------------------------------------------------------------------

    @JvmStatic
    @JvmOverloads
    fun headers(cookie: String = "", referer: String = "", userAgent: String = UA.PC.value): Headers {
        return Headers.headersOf("cookie", cookie, "referer", referer, "user-agent", userAgent)
    }

    @JvmStatic
    fun headers(cookie: String, referer: String, ua: UA): Headers {
        return headers(cookie, referer, ua.value)
    }

    @JvmStatic
    fun ua(ua: UA) = Headers.headersOf("User-Agent", ua.value)

    @JvmStatic
    fun ua(ua: String) = Headers.headersOf("User-Agent", ua)

    @JvmStatic
    fun cookie(cookie: String) = Headers.headersOf("Cookie", cookie)

    @JvmStatic
    fun referer(url: String) = Headers.headersOf("Referer", url)

    @JvmStatic
    fun stream(byteString: ByteString) = byteString.toRequestBody(MEDIA_STREAM)

    // -------------------------------------------------------------------------------------------------------------

    private fun cookie(cookies: List<String>): String {
        val sb = StringBuilder()
        for (tempCookie in cookies) {
            if (tempCookie == "deleted") continue
            val cookie = MyUtils.regex(".*?;", tempCookie) ?: continue
            val arr = cookie.split("=")
            if (arr.size < 2) continue
            if (arr[1] == ";") continue
            sb.append(cookie).append(" ")
        }
        return sb.toString()
    }

    @JvmStatic
    fun cookie(response: Response): String {
        val cookies = response.headers("Set-Cookie")
        return cookie(cookies)
    }

    @JvmStatic
    fun cookie(response: Response, name: String): String? {
        val cookie = cookie(response)
        return cookie(cookie, name)
    }


    @JvmStatic
    fun cookieStr(response: Response, vararg name: String): String {
        val cookie = cookie(response)
        return cookieStr(cookie, *name)
    }

    @JvmStatic
    fun cookieToMap(response: Response, vararg name: String): Map<String, String> {
        val cookie = cookie(response)
        return cookie(cookie, *name)
    }

    @JvmStatic
    fun cookieStr(cookie: String, vararg name: String): String {
        val sb = StringBuilder()
        for (str in name) {
            val singleCookie = cookieStr(cookie, str)
            sb.append(singleCookie)
        }
        return sb.toString()
    }

    @JvmStatic
    private fun cookieStr(cookie: String, name: String): String? {
        return MyUtils.regex("$name=[^;]+; ", cookie)
    }

    @JvmStatic
    fun cookie(cookie: String, name: String): String? {
        var arr = cookie.split("; ")
        if (arr.isEmpty()) arr = cookie.split(";")
        for (str in arr) {
            val newArr = str.split("=")
            if (newArr.size > 1 && newArr[0].trim() == name) {
                return str.substring(str.indexOf('=') + 1)
            }
        }
        return null
    }

    @JvmStatic
    fun cookie(cookie: String, vararg name: String): Map<String, String> {
        val map: MutableMap<String, String> = HashMap()
        for (str in name) {
            map[str] = cookie(cookie, str) ?: ""
        }
        return map
    }

    @JvmStatic
    fun cookieToMap(cookie: String): Map<String, String> {
        val map: MutableMap<String, String> = HashMap()
        val arr = cookie.split(";")
        for (str in arr) {
            val newArr = str.split("=")
            if (newArr.size > 1) map[newArr[0].trim()] = newArr[1].trim()
        }
        return map
    }

    @JvmStatic
    fun cookieReplace(cookie: String, name: String, value: String): String {
        val find = cookieStr(cookie, name)
        return if (find != null) {
            cookie.replace(find, "$name=$value; ")
        } else {
            cookie
        }
    }

    // -----------------------------------------------------------------------------------------------------------


    @JvmStatic
    @JvmOverloads
    fun streamBody(file: File, contentType: String = "application/octet-stream"): RequestBody {
        return file.asRequestBody(contentType.toMediaType())
    }

    @JvmStatic
    @JvmOverloads
    fun streamBody(byteArray: ByteArray, contentType: String = "application/octet-stream"): RequestBody {
        return byteArray.toRequestBody(contentType.toMediaType())
    }

    @JvmStatic
    @JvmOverloads
    fun streamBody(iis: InputStream, contentType: String = "application/octet-stream"): RequestBody {
        return streamBody(iis.readBytes(), contentType)
    }

    @JvmStatic
    fun urlParams(map: Map<String, String>): String {
        val sb = java.lang.StringBuilder()
        for ((key, value) in map) {
            try {
                sb.append(key).append("=").append(URLEncoder.encode(value, "utf-8")).append("&")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        }
        return sb.toString()
    }
}
