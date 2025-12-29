@file:Suppress("unused")

package me.kuku.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule

object Jackson {

    @JvmStatic
    var objectMapper: ObjectMapper = ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
        .registerModules(JavaTimeModule(), kotlinModule())

    @JvmStatic
    fun toJsonString(any: Any): String {
        return objectMapper.writeValueAsString(any)
    }

    @JvmStatic
    fun parse(json: String): JsonNode {
        return objectMapper.readTree(json)
    }

    @JvmStatic
    fun <T> parseObject(json: String, clazz: Class<T>): T {
        return objectMapper.readValue(json, clazz)
    }

    @JvmStatic
    fun <T> parseObject(json: String, valueTypeRef: TypeReference<T>): T {
        return objectMapper.readValue(json, valueTypeRef)
    }

    inline fun <reified T: Any> parseObject(json: String): T {
        return objectMapper.readValue(json, object: TypeReference<T>(){})
    }

    @JvmStatic
    fun <T> parseArray(json: String, clazz: Class<T>): MutableList<T> {
        return objectMapper.readerFor(clazz).readValues<T>(json).readAll()
    }

    @JvmStatic
    fun <T> parseArray(json: String, valueTypeRef: TypeReference<T>): MutableList<T> {
        return objectMapper.readerFor(valueTypeRef).readValues<T>(json).readAll()
    }

    inline fun <reified T: Any> parseArray(json: String): List<T> {
        return objectMapper.readValue(json, object: TypeReference<List<T>>(){})
    }

    @JvmStatic
    fun <T> convertValue(jsonNode: JsonNode, clazz: Class<T>): T {
        return objectMapper.convertValue(jsonNode, clazz)
    }

    inline fun <reified T: Any> convertValue(jsonNode: JsonNode): T {
        return objectMapper.convertValue(jsonNode, object: TypeReference<T>() {})
    }

    inline fun <reified T:Any> valueToTree(any: Any): T {
        return objectMapper.valueToTree(any)
    }

    @JvmStatic
    fun createObjectNode(): ObjectNode {
        return objectMapper.createObjectNode()
    }

    @JvmStatic
    fun createArrayNode(): ArrayNode {
        return objectMapper.createArrayNode()
    }

    @JvmStatic
    fun valueToTree(any: Any): JsonNode {
        return objectMapper.valueToTree(any)
    }

    @JvmStatic
    fun <T> treeToValue(n: TreeNode, clazz: Class<T>): T {
        return objectMapper.treeToValue(n, clazz)
    }

    inline fun <reified T: Any> treeToValue(n: TreeNode): T {
        return objectMapper.convertValue(n, object: TypeReference<T>() {})
    }
}

fun JsonNode.getString(key: String): String = this[key].asText()

fun JsonNode.getString(i: Int): String = this[i].asText()

fun JsonNode.getInteger(key: String): Int = this[key].asInt()

fun JsonNode.getBoolean(key: String): Boolean = this[key].asBoolean()

fun JsonNode.getLong(key: String): Long = this[key].asLong()

fun JsonNode.getDouble(key: String): Double = this[key].asDouble()

inline fun <reified T: Any> JsonNode.convertValue() = Jackson.convertValue<T>(this)

inline fun <reified T: Any> JsonNode.treeToValue() = Jackson.treeToValue<T>(this)