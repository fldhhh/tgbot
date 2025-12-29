@file:Suppress("unused", "UNCHECKED_CAST")

package me.kuku.pojo

import com.alibaba.fastjson.annotation.JSONType
import com.alibaba.fastjson.serializer.JSONSerializer
import com.alibaba.fastjson.serializer.ObjectSerializer
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.lang.reflect.Type
import java.time.temporal.Temporal
import java.util.Date

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonSerialize(using = CommonResultSerializer::class)
@JSONType(serializer = CommonResultFastjsonSerializer::class)
class CommonResult<T> private constructor(
    val code: Int, val message: String, val data: T?
) {
    fun data() = data!!

    fun success() = code == 200

    fun failure() = code != 200

    companion object {

        @JvmOverloads
        @JvmStatic
        fun <T> success(data: T? = null, message: String = "成功"): CommonResult<T> {
            return CommonResult(200, message, data as T)
        }

        @JvmOverloads
        @JvmStatic
        fun <T> failure(message: String, data: T? = null, code: Int = 500): CommonResult<T> {
            return CommonResult(code, message, data)
        }

        @JvmOverloads
        @JvmStatic
        fun <T> fail(message: String, data: T? = null, code: Int = 500) = failure(message, data, code)

    }
}
private fun Any.valid(): Boolean {
    return !(this is Int || this is String || this is Long || this is Float || this is Double || this is Boolean ||
            this is Date || this is Temporal || this is Byte || this is Char || this is Short)
}

class CommonResultSerializer: JsonSerializer<CommonResult<*>>() {
    override fun serialize(value: CommonResult<*>?, gen: JsonGenerator, serializers: SerializerProvider) {
        value?.let {
            if (value.success()) {
                val aa = value.data ?: mapOf("message" to value.message)
                gen.writeObject(if (aa.valid()) aa else mapOf("data" to aa))
            } else {
                gen.writeStartObject()
                gen.writeNumberField("code", it.code)
                gen.writeStringField("message", it.message)
                it.data?.let { data ->
                    gen.writeObjectField("data", data)
                }
                gen.writeEndObject()
            }
        } ?: gen.writeObject("{}")
    }
}

class CommonResultFastjsonSerializer: ObjectSerializer {
    override fun write(serializer: JSONSerializer, any: Any?, fieldName: Any?, fieldType: Type?, features: Int) {
        if (any == null) return serializer.writeNull()
        if (any is CommonResult<*>) {
            if (any.success()) {
                val aa = any.data ?: mapOf("message" to any.message)
                serializer.write(if (aa.valid()) aa else mapOf("data" to aa))
            } else {
                val map = mapOf("code" to any.code, "message" to any.message, "data" to any.data)
                serializer.write(map)
            }
        }
    }
}

class SimpleResult private constructor(
    val success: Boolean, val message: String
) {
    companion object {

        @JvmStatic
        fun success() = SimpleResult(true, "success")

        @JvmStatic
        @JvmOverloads
        fun failure(message: String = "fail") = SimpleResult(false, message)

        @JvmStatic
        @JvmOverloads
        fun fail(message: String = "fail") = failure(message)

    }
}
