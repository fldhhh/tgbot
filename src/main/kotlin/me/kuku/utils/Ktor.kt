package me.kuku.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import java.net.Proxy
import java.net.Socket
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLEngine
import javax.net.ssl.X509ExtendedTrustManager

internal val trustAllCert = object : X509ExtendedTrustManager() {
    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?, socket: Socket?) {
    }

    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?, engine: SSLEngine?) {
    }

    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?, socket: Socket?) {
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?, engine: SSLEngine?) {
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return arrayOf()
    }
}
internal val trustAllCerts = arrayOf(trustAllCert)
internal val sslContext =
    SSLContext.getInstance("SSL").also { it.init(null, trustAllCerts, java.security.SecureRandom()) }
internal val sslSocketFactory = sslContext.socketFactory

private var ktorProxy: Proxy? = null

fun setKtorProxy(proxyParam: Proxy) {
    ktorProxy = proxyParam
}

private fun ObjectMapper.config() {
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
    disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
    configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
    registerModule(JavaTimeModule())
    registerModule(kotlinModule())
}

val client by lazy {
    HttpClient(OkHttp) {
        engine {
            config {
                followRedirects(false)
                followSslRedirects(false)
                connectTimeout(60, TimeUnit.SECONDS)
                readTimeout(60, TimeUnit.SECONDS)
                sslSocketFactory(sslSocketFactory, trustAllCerts[0])
                hostnameVerifier { _, _ -> true }
                pingInterval(20, TimeUnit.SECONDS)
                if (ktorProxy != null) {
                    proxy(ktorProxy)
                }
            }
        }

        followRedirects = false

        install(ContentNegotiation) {
            jackson {
                config()
            }
        }

        install(WebSockets) {
            contentConverter = JacksonWebsocketContentConverter(Jackson.objectMapper)
        }

        defaultRequest {
            header("Accept", "*/*")
        }
    }
}

fun FormBuilder.append(
    key: String,
    byteArray: ByteArray,
    filename: String,
    contentType: String = "application/octet-stream"
) {
    this.append(key, byteArray, Headers.build {
        append(HttpHeaders.ContentType, contentType)
        append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
    })
}

fun HttpResponse.cookie(): String {
    val cookies = this.setCookie()
    val sb = StringBuilder()
    for (cookie in cookies) {
        if (cookie.value == "deleted") continue
        sb.append("${cookie.name}=${cookie.value}; ")
    }
    return sb.toString()
}

fun String.findCookie(name: String): String? {
    return OkUtils.cookie(this, name)
}

fun String.findCookieStr(name: String): String {
    return OkUtils.cookieStr(this, name)
}

fun String.cookieReplace(name: String, value: String): String {
    return OkUtils.cookieReplace(this, name, value)
}

fun HttpMessageBuilder.origin(content: String): Unit = headers.set(HttpHeaders.Origin, content)

fun HttpMessageBuilder.referer(content: String): Unit = headers.set(HttpHeaders.Referrer, content)

fun HttpMessageBuilder.cookieString(content: String): Unit = headers.set(HttpHeaders.Cookie, content)

fun HttpRequestBuilder.setJsonBody(content: Any) {
    contentType(ContentType.Application.Json)
    setBody(content)
}

fun HttpRequestBuilder.setFormDataContent(builder: ParametersBuilder.() -> Unit) {
    setBody(FormDataContent(Parameters.build { builder() }))
}
