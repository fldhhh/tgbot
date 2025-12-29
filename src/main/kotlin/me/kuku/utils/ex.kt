@file:Suppress("unused")

package me.kuku.utils

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.fasterxml.jackson.databind.JsonNode
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Base64

fun String.toJSONObject(): JSONObject {
    return JSON.parseObject(this)
}

fun String.toJSONArray(): JSONArray {
    return JSON.parseArray(this)
}

fun Any.toJSONString(): String {
    return JSON.toJSONString(this)
}

fun String.toJsonNode(): JsonNode {
    return Jackson.parse(this)
}

fun String.toUrlEncode(enc: String = "utf-8"): String {
    return URLEncoder.encode(this, enc)
}

fun String.toUrlDecode(enc: String = "utf-8"): String {
    return URLDecoder.decode(this, enc)
}

fun String.base64Encode(): String {
    return Base64.getEncoder().encodeToString(this.toByteArray())
}

fun ByteArray.base64Encode(): String {
    return Base64.getEncoder().encodeToString(this)
}

fun String.base64Decode(): ByteArray {
    return Base64.getDecoder().decode(this)
}

fun ByteArray.base64Decode(): ByteArray {
    return Base64.getDecoder().decode(this)
}