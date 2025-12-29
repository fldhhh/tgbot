@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package me.kuku.utils

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.CompletableDeferred
import me.kuku.pojo.UA
import okhttp3.*
import okio.ByteString
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Path
import java.util.function.Predicate

object OkHttpKtUtils {

    private var okhttpClient: OkHttpClient? = null

    @Synchronized
    private fun okhttpClient(): OkHttpClient {
        if (okhttpClient == null) {
            okhttpClient = OkHttpUtils.okhttpClient()
        }
        return okhttpClient!!
    }

    private fun addHeaders(map: Map<String, String>): Headers {
        val builder = Headers.Builder()
        map.forEach(builder::add)
        return builder.build()
    }

    private fun defaultHeaders(): Headers {
        return Headers.headersOf("user-agent", UA.PC.value)
    }

    private suspend fun xx(request: Request): Response {
        val ss = CompletableDeferred<Response>()
        okhttpClient().newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                ss.completeExceptionally(e)
            }

            override fun onResponse(call: Call, response: Response) {
                ss.complete(response)
            }
        })
        return ss.await()
    }

    suspend fun get(url: String, headers: Headers = defaultHeaders()): Response {
        val request = Request.Builder().url(url).headers(headers).build()
        return xx(request)
    }

    suspend fun get(url: String, map: Map<String, String>): Response {
        return get(url, addHeaders(map))
    }

    private fun mapToFormBody(map: Map<String, String>): FormBody {
        val builder = FormBody.Builder()
        map.forEach(builder::add)
        return builder.build()
    }

    suspend fun post(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): Response {
        val request = Request.Builder().url(url).post(requestBody).headers(headers).build()
        return xx(request)
    }

    suspend fun post(url: String, requestBody: RequestBody = FormBody.Builder().build(), headersMap: Map<String, String>): Response {
        return post(url, requestBody, addHeaders(headersMap))
    }

    suspend fun post(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): Response {
        return post(url, mapToFormBody(map), headers)
    }

    suspend fun post(url: String, map: Map<String, String>, headerMap: Map<String, String>): Response {
        return post(url, map, addHeaders(headerMap))
    }


    suspend fun put(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): Response {
        val request = Request.Builder().url(url).put(requestBody).headers(headers).build()
        return xx(request)
    }

    suspend fun put(url: String, requestBody: RequestBody = FormBody.Builder().build(), headersMap: Map<String, String>): Response {
        return put(url, requestBody, addHeaders(headersMap))
    }

    suspend fun put(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): Response {
        return put(url, mapToFormBody(map), headers)
    }

    suspend fun put(url: String, map: Map<String, String>, headerMap: Map<String, String>): Response {
        return put(url, map, addHeaders(headerMap))
    }

    suspend fun delete(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): Response {
        val request = Request.Builder().url(url).delete(requestBody).headers(headers).build()
        return xx(request)
    }

    suspend fun delete(url: String, requestBody: RequestBody = FormBody.Builder().build(), headersMap: Map<String, String>): Response {
        return delete(url, requestBody, addHeaders(headersMap))
    }

    suspend fun delete(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): Response {
        return delete(url, mapToFormBody(map), headers)
    }

    suspend fun delete(url: String, map: Map<String, String>, headerMap: Map<String, String>): Response {
        return post(url, map, addHeaders(headerMap))
    }

    suspend fun patch(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): Response {
        val request = Request.Builder().url(url).patch(requestBody).headers(headers).build()
        return xx(request)
    }

    suspend fun patch(url: String, requestBody: RequestBody = FormBody.Builder().build(), headersMap: Map<String, String>): Response {
        return patch(url, requestBody, addHeaders(headersMap))
    }

    suspend fun patch(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): Response {
        return patch(url, mapToFormBody(map), headers)
    }

    suspend fun patch(url: String, map: Map<String, String>, headerMap: Map<String, String>): Response {
        return patch(url, map, addHeaders(headerMap))
    }

    // ---------------------------------------------------------------------------------------------------


    // -----------------------------------------------------------------------------------------------------------

    suspend fun getStr(url: String, headers: Headers = defaultHeaders()): String {
        val response = get(url, headers)
        return OkUtils.str(response)
    }

    suspend fun getStr(url: String, map: Map<String, String>): String {
        val response = get(url, map)
        return OkUtils.str(response)
    }

    suspend fun getJson(url: String, headers: Headers = defaultHeaders()): JsonNode {
        val response = get(url, headers)
        return OkUtils.json(response)
    }

    suspend fun getJson(url: String, map: Map<String, String>): JsonNode {
        val response = get(url, map)
        return OkUtils.json(response)
    }

    suspend fun getJsonp(url: String, headers: Headers = defaultHeaders()): JsonNode {
        val response = get(url, headers)
        return OkUtils.jsonp(response)
    }

    suspend fun getJsonp(url: String, map: Map<String, String>): JsonNode {
        val response = get(url, map)
        return OkUtils.jsonp(response)
    }

    suspend fun getBytes(url: String, headers: Headers = defaultHeaders()): ByteArray {
        val response = get(url, headers)
        return OkUtils.bytes(response)
    }

    suspend fun getBytes(url: String, map: Map<String, String>): ByteArray {
        val response = get(url, map)
        return OkUtils.bytes(response)
    }

    suspend fun getByteStream(url: String, headers: Headers = defaultHeaders()): InputStream {
        val response = get(url, headers)
        return OkUtils.byteStream(response)
    }

    suspend fun getByteStream(url: String, map: Map<String, String>): InputStream {
        val response = get(url, map)
        return OkUtils.byteStream(response)
    }

    suspend fun getByteString(url: String, headers: Headers = defaultHeaders()): ByteString {
        val response = get(url, headers)
        return OkUtils.byteString(response)
    }

    suspend fun getByteString(url: String, map: Map<String, String>): ByteString {
        val response = get(url, map)
        return OkUtils.byteString(response)
    }

    // ----------------------------------------------------------------------------------------------------------

    suspend fun postStr(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): String {
        val response = post(url, requestBody, headers)
        return OkUtils.str(response)
    }

    suspend fun postStr(url: String, requestBody: RequestBody = FormBody.Builder().build(), headerMap: Map<String, String>): String {
        val response = post(url, requestBody, addHeaders(headerMap))
        return OkUtils.str(response)
    }

    suspend fun postStr(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): String {
        val response = post(url, map, headers)
        return OkUtils.str(response)
    }

    suspend fun postStr(url: String, map: Map<String, String>, headerMap: Map<String, String>): String {
        val response = post(url, map, headerMap)
        return OkUtils.str(response)
    }

    suspend fun postJson(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): JsonNode {
        val response = post(url, requestBody, headers)
        return OkUtils.json(response)
    }

    suspend fun postJson(url: String, requestBody: RequestBody = FormBody.Builder().build(), headerMap: Map<String, String>): JsonNode {
        val response = post(url, requestBody, addHeaders(headerMap))
        return OkUtils.json(response)
    }

    suspend fun postJson(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): JsonNode {
        val response = post(url, map, headers)
        return OkUtils.json(response)
    }

    suspend fun postJson(url: String, map: Map<String, String>, headerMap: Map<String, String>): JsonNode {
        val response = post(url, map, headerMap)
        return OkUtils.json(response)
    }

    suspend fun postJsonp(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): JsonNode {
        val response = post(url, requestBody, headers)
        return OkUtils.jsonp(response)
    }

    suspend fun postJsonp(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): JsonNode {
        val response = post(url, map, headers)
        return OkUtils.jsonp(response)
    }

    suspend fun postJsonp(url: String, map: Map<String, String>, headerMap: Map<String, String>): JsonNode {
        val response = post(url, map, headerMap)
        return OkUtils.jsonp(response)
    }

    suspend fun postBytes(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): ByteArray {
        val response = post(url, requestBody, headers)
        return OkUtils.bytes(response)
    }

    suspend fun postBytes(url: String, requestBody: RequestBody = FormBody.Builder().build(), headerMap: Map<String, String>): ByteArray {
        val response = post(url, requestBody, addHeaders(headerMap))
        return OkUtils.bytes(response)
    }

    suspend fun postBytes(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): ByteArray {
        val response = post(url, map, headers)
        return OkUtils.bytes(response)
    }

    suspend fun postBytes(url: String, map: Map<String, String>, headerMap: Map<String, String>): ByteArray {
        val response = post(url, map, headerMap)
        return OkUtils.bytes(response)
    }

    // ---------------------------------------------------------------------------------------------------------------

    suspend fun putStr(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): String {
        val response = put(url, requestBody, headers)
        return OkUtils.str(response)
    }

    suspend fun putStr(url: String, requestBody: RequestBody = FormBody.Builder().build(), headerMap: Map<String, String>): String {
        return putStr(url, requestBody, addHeaders(headerMap))
    }

    suspend fun putStr(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): String {
        val response = put(url, map, headers)
        return OkUtils.str(response)
    }

    suspend fun putStr(url: String, map: Map<String, String>, headerMap: Map<String, String>): String {
        val response = put(url, map, headerMap)
        return OkUtils.str(response)
    }

    suspend fun putJson(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): JsonNode {
        val response = put(url, requestBody, headers)
        return OkUtils.json(response)
    }

    suspend fun putJson(url: String, requestBody: RequestBody = FormBody.Builder().build(), headerMap: Map<String, String>): JsonNode {
        return putJson(url, requestBody, addHeaders(headerMap))
    }

    suspend fun putJson(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): JsonNode {
        val response = put(url, map, headers)
        return OkUtils.json(response)
    }

    suspend fun putJson(url: String, map: Map<String, String>, headerMap: Map<String, String>): JsonNode {
        val response = put(url, map, headerMap)
        return OkUtils.json(response)
    }

    // ----------------------------------------------------------------------------------------------------------------

    suspend fun deleteStr(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): String {
        val response = delete(url, requestBody, headers)
        return OkUtils.str(response)
    }

    suspend fun deleteStr(url: String, requestBody: RequestBody = FormBody.Builder().build(), headerMap: Map<String, String>): String {
        return deleteStr(url, requestBody, addHeaders(headerMap))
    }

    suspend fun deleteStr(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): String {
        val response = delete(url, map, headers)
        return OkUtils.str(response)
    }

    suspend fun deleteStr(url: String, map: Map<String, String>, headerMap: Map<String, String>): String {
        val response = delete(url, map, headerMap)
        return OkUtils.str(response)
    }

    suspend fun deleteJson(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): JsonNode {
        val response = delete(url, requestBody, headers)
        return OkUtils.json(response)
    }

    suspend fun deleteJson(url: String, requestBody: RequestBody = FormBody.Builder().build(), headerMap: Map<String, String>): JsonNode {
        return deleteJson(url, requestBody, addHeaders(headerMap))
    }

    suspend fun deleteJson(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): JsonNode {
        val response = delete(url, map, headers)
        return OkUtils.json(response)
    }

    suspend fun deleteJson(url: String, map: Map<String, String>, headerMap: Map<String, String>): JsonNode {
        val response = delete(url, map, headerMap)
        return OkUtils.json(response)
    }

    // ---------------------------------------------------------------------------------------------------------------

    suspend fun patchStr(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): String {
        val response = patch(url, requestBody, headers)
        return OkUtils.str(response)
    }

    suspend fun patchStr(url: String, requestBody: RequestBody = FormBody.Builder().build(), headerMap: Map<String, String>): String {
        return patchStr(url, requestBody, addHeaders(headerMap))
    }

    suspend fun patchStr(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): String {
        val response = patch(url, map, headers)
        return OkUtils.str(response)
    }

    suspend fun patchStr(url: String, map: Map<String, String>, headerMap: Map<String, String>): String {
        val response = patch(url, map, headerMap)
        return OkUtils.str(response)
    }

    suspend fun patchJson(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): JsonNode {
        val response = patch(url, requestBody, headers)
        return OkUtils.json(response)
    }

    suspend fun patchJson(url: String, requestBody: RequestBody = FormBody.Builder().build(), headerMap: Map<String, String>): JsonNode {
        return patchJson(url, requestBody, addHeaders(headerMap))
    }

    suspend fun patchJson(url: String, map: Map<String, String>, headers: Headers = defaultHeaders()): JsonNode {
        val response = patch(url, map, headers)
        return OkUtils.json(response)
    }

    suspend fun patchJson(url: String, map: Map<String, String>, headerMap: Map<String, String>): JsonNode {
        val response = patch(url, map, headerMap)
        return OkUtils.json(response)
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ================== get请求把文件保存在本地 ===============================

    @JvmStatic
    @JvmOverloads
    suspend fun getFile(url: String, path: Path, headers: Headers = defaultHeaders()): File {
        val response = get(url, headers)
        return OkUtils.file(response, path)
    }

    @JvmStatic
    @JvmOverloads
    suspend fun getFile(url: String, file: File, headers: Headers = defaultHeaders()): File {
        val response = get(url, headers)
        return OkUtils.file(response, file)
    }

    @JvmStatic
    suspend fun getFile(url: String, path: Path, headers: Map<String, String>): File {
        val response = get(url, headers)
        return OkUtils.file(response, path)
    }

    @JvmStatic
    suspend fun getFile(url: String, file: File, headers: Map<String, String>): File {
        val response = get(url, headers)
        return OkUtils.file(response, file)
    }

    // ================== post请求把文件保存在本地 ===============================

    @JvmStatic
    @JvmOverloads
    suspend fun postFile(url: String, path: Path, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): File {
        val response = post(url, requestBody, headers)
        return OkUtils.file(response, path)
    }

    @JvmStatic
    @JvmOverloads
    suspend fun postFile(url: String, file: File, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = defaultHeaders()): File {
        val response = post(url, requestBody, headers)
        return OkUtils.file(response, file)
    }

    @JvmStatic
    suspend fun postFile(url: String, path: Path, requestBody: RequestBody = FormBody.Builder().build(), headers: Map<String, String>): File {
        val response = post(url, requestBody, headers)
        return OkUtils.file(response, path)
    }

    @JvmStatic
    suspend fun postFile(url: String, file: File, requestBody: RequestBody = FormBody.Builder().build(), headers: Map<String, String>): File {
        val response = post(url, requestBody, headers)
        return OkUtils.file(response, file)
    }

    @JvmStatic
    @JvmOverloads
    suspend fun postFile(url: String, path: Path, map: Map<String, String>, headers: Headers = defaultHeaders()): File {
        val response = post(url, map, headers)
        return OkUtils.file(response, path)
    }

    @JvmStatic
    @JvmOverloads
    suspend fun postFile(url: String, file: File, map: Map<String, String>, headers: Headers = defaultHeaders()): File {
        val response = post(url, map, headers)
        return OkUtils.file(response, file)
    }

    @JvmStatic
    suspend fun postFile(url: String, path: Path, map: Map<String, String>, headerMap: Map<String, String>): File {
        val response = post(url, map, headerMap)
        return OkUtils.file(response, path)
    }

    @JvmStatic
    suspend fun postFile(url: String, file: File, map: Map<String, String>, headerMap: Map<String, String>): File {
        val response = post(url, map, headerMap)
        return OkUtils.file(response, file)
    }

    fun websocket(url: String, block: OkHttpWebSocket.() -> Unit): WebSocket {
        val okHttpWebSocket = OkHttpWebSocket()
        block.invoke(okHttpWebSocket)
        val request = Request.Builder().get().url(url).build()
        return okhttpClient().newWebSocket(request, object : WebSocketListener() {
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                okHttpWebSocket.closedList.forEach {
                    it.invoke(webSocket, OkHttpWebSocket.Close(code, reason))
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                okHttpWebSocket.closingList.forEach {
                    it.invoke(webSocket, OkHttpWebSocket.Close(code, reason))
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                okHttpWebSocket.failureList.forEach {
                    it.invoke(webSocket, OkHttpWebSocket.Failure(t, response))
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                kotlin.runCatching {
                    val jsonNode = text.toJsonNode()
                    okHttpWebSocket.jsonMessageList.forEach {
                        if (it.predicate.test(jsonNode)) {
                            it.block.invoke(webSocket, jsonNode)
                        }
                    }
                }.onFailure {
                    okHttpWebSocket.textMessageList.forEach {
                        if (it.predicate.test(text)) {
                            it.block.invoke(webSocket, text)
                        }
                    }
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                okHttpWebSocket.byteStringMessageList.forEach {
                    if (it.predicate.test(bytes)) {
                        it.block.invoke(webSocket, bytes)
                    }
                }
            }

            override fun onOpen(webSocket: WebSocket, response: Response) {
                okHttpWebSocket.openList.forEach {
                    it.invoke(webSocket, response)
                }
            }
        })
    }
}

class OkHttpWebSocket {
    data class Close(val code: Int, val reason: String)
    data class Failure(val t: Throwable, val response: Response?)
    data class TextMessageAndPredicate(val block: WebSocket.(String) -> Unit, val predicate: Predicate<String>)
    data class JsonMessageAndPredicate(val block: WebSocket.(JsonNode) -> Unit, val predicate: Predicate<JsonNode>)
    data class ByteStringMessageAndPredicate(val block: WebSocket.(ByteString) -> Unit, val predicate: Predicate<ByteString>)

    val openList = mutableListOf<WebSocket.(Response) -> Unit>()
    val closedList = mutableListOf<WebSocket.(Close) -> Unit>()
    val closingList =  mutableListOf<WebSocket.(Close) -> Unit>()
    val failureList = mutableListOf<WebSocket.(Failure) -> Unit>()
    val textMessageList = mutableListOf<TextMessageAndPredicate>()
    val jsonMessageList = mutableListOf<JsonMessageAndPredicate>()
    val byteStringMessageList = mutableListOf<ByteStringMessageAndPredicate>()

    fun open(block: WebSocket.(Response) -> Unit): OkHttpWebSocket {
        openList.add(block)
        return this
    }

    fun closed(block: WebSocket.(Close) -> Unit): OkHttpWebSocket {
        closedList.add(block)
        return this
    }

    fun closing(block: WebSocket.(Close) -> Unit): OkHttpWebSocket {
        closingList.add(block)
        return this
    }

    fun failure(block: WebSocket.(Failure) -> Unit): OkHttpWebSocket {
        failureList.add(block)
        return this
    }

    fun textMessage(predicate: Predicate<String> = Predicate<String> { true }, block: WebSocket.(String) -> Unit): OkHttpWebSocket {
        textMessageList.add(TextMessageAndPredicate(block, predicate))
        return this
    }

    fun byteStringMessage(predicate: Predicate<ByteString> = Predicate<ByteString> { true }, block: WebSocket.(ByteString) -> Unit): OkHttpWebSocket {
        byteStringMessageList.add(ByteStringMessageAndPredicate(block, predicate))
        return this
    }

    fun jsonMessage(predicate: Predicate<JsonNode> = Predicate<JsonNode> { true }, block: WebSocket.(JsonNode) -> Unit): OkHttpWebSocket {
        jsonMessageList.add(JsonMessageAndPredicate(block, predicate))
        return this
    }


}
