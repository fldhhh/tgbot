package me.kuku.telegram.config

import io.ktor.client.call.*
import io.ktor.client.request.*
import jakarta.annotation.PostConstruct
import me.kuku.telegram.utils.*
import me.kuku.utils.JobManager
import org.mapdb.DBMaker
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.bot.BaseAbilityBot
import org.telegram.abilitybots.api.bot.DefaultAbilities
import org.telegram.abilitybots.api.db.MapDBContext
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Reply
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.bots.DefaultBotOptions.ProxyType
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredMemberExtensionFunctions
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.jvm.jvmName

@Component
class TelegramBean(
    private val telegramConfig: TelegramConfig,
    private val applicationContext: ApplicationContext
): ApplicationListener<ContextRefreshedEvent> {

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val telegramBot = event.applicationContext.getBean(TelegramBot::class.java)
        val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
        botsApi.registerBot(telegramBot)
    }

    @Bean
    fun telegramBot(): TelegramBot {
        val botOptions = DefaultBotOptions()
        if (telegramConfig.proxyType != ProxyType.NO_PROXY) {
            botOptions.proxyHost = telegramConfig.proxyHost
            botOptions.proxyPort = telegramConfig.proxyPort
            botOptions.proxyType = telegramConfig.proxyType
        }
        return TelegramBot(telegramConfig.token, telegramConfig.username, telegramConfig.creatorId, botOptions, applicationContext)
    }
}

private fun createDbContext(botUsername: String): MapDBContext {
    if (botUsername.isEmpty()) error("kuku.telegram.username can't empty")
    val dir = File("config")
    if (!dir.exists()) dir.mkdir()
    return MapDBContext(DBMaker
        .fileDB(File("config${File.separator}$botUsername"))
        .fileMmapEnableIfSupported()
        .closeOnJvmShutdown()
        .transactionEnable()
        .make())
}

val telegramExceptionHandler = TelegramExceptionHandler()

class TelegramBot(val token: String, botUsername: String, private val creatorId: Long, botOptions: DefaultBotOptions,
                  private val applicationContext: ApplicationContext):
    AbilityBot(token, botUsername, createDbContext(botUsername), botOptions) {

    private val telegramSubscribeList = mutableListOf<TelegramSubscribe>()
    private val updateFunction = mutableListOf<UpdateFunction>()
    private data class UpdateFunction(val function: KFunction<*>, val any: Any)

    override fun creatorId() = creatorId

    override fun onRegister() {
        val baseAbilityBotClazz = BaseAbilityBot::class.java
        val abilitiesField = baseAbilityBotClazz.getDeclaredField("abilities")
        abilitiesField.isAccessible = true
        val names = applicationContext.beanDefinitionNames
        val clazzList = mutableListOf<Class<*>>(DefaultAbilities::class.java)
        val map = mutableMapOf<String, Ability>()
        for (name in names) {
            applicationContext.getType(name)?.let {
                clazzList.add(it)
            }
        }
        val list = mutableListOf<Reply>()
        val abilitySubscriber = AbilitySubscriber()
        val callBackQList= mutableListOf<CallbackQ>()
        for (clazz in clazzList) {
            val methods = clazz.declaredMethods
            var any: Any? = null
            for (method in methods) {
                val returnType = method.returnType
                if (returnType == Ability::class.java) {
                    if (any == null)
                        any = if (clazz == DefaultAbilities::class.java) DefaultAbilities(this) else applicationContext.getBean(clazz)
                    val newAny = any
                    val ability = method.invoke(newAny) as Ability
                    map[ability.name()] = ability
                } else if (returnType == Reply::class.java) {
                    if (any == null)
                        any = if (clazz == DefaultAbilities::class.java) DefaultAbilities(this) else applicationContext.getBean(clazz)
                    val newAny = any
                    val reply = method.invoke(newAny) as Reply
                    list.add(reply)
                }
            }
            val functions = kotlin.runCatching {
                clazz.kotlin.declaredMemberExtensionFunctions
            }.getOrNull() ?: continue
            for (function in functions) {
                val type = function.extensionReceiverParameter?.type
                val kClass = type?.classifier as? KClass<*>
                when (kClass?.jvmName) {
                    "me.kuku.telegram.utils.AbilitySubscriber" -> {
                        val obj = applicationContext.getBean(clazz)
                        function.call(obj, abilitySubscriber)
                    }
                    "me.kuku.telegram.utils.CallbackQ" -> {
                        val callBackQ = CallbackQ()
                        val obj = applicationContext.getBean(clazz)
                        function.call(obj, callBackQ)
                        callBackQList.add(callBackQ)
                    }
                    "me.kuku.telegram.utils.TelegramSubscribe" -> {
                        val telegramSubscribe = TelegramSubscribe()
                        val obj = applicationContext.getBean(clazz)
                        function.call(obj, telegramSubscribe)
                        telegramSubscribeList.add(telegramSubscribe)
                    }
                    "org.telegram.telegrambots.meta.api.objects.Update" -> {
                        updateFunction.add(UpdateFunction(function, applicationContext.getBean(clazz)))
                    }
                    "me.kuku.telegram.utils.TelegramExceptionHandler" -> {
                        val obj = applicationContext.getBean(clazz)
                        function.call(obj, telegramExceptionHandler)
                    }
                }
            }
        }
        @Suppress("UNCHECKED_CAST")
        map.putAll(abilitySubscriber::class.java.getDeclaredField("abilityMap").also { it.isAccessible = true }.get(abilitySubscriber) as Map<out String, Ability>)
        @Suppress("UNCHECKED_CAST")
        callBackQList.forEach { callBackQ ->
            list.add(callBackQ::class.java.getDeclaredMethod("toReply").also { it.isAccessible = true }.invoke(callBackQ) as Reply) }
        abilitiesField.set(this, map)
        val repliesField = baseAbilityBotClazz.getDeclaredField("replies")
        repliesField.isAccessible = true
        repliesField.set(this, list)
        val initStatsMethod = baseAbilityBotClazz.getDeclaredMethod("initStats")
        initStatsMethod.isAccessible = true
        initStatsMethod.invoke(this)
    }

    override fun onUpdateReceived(update: Update) {
        super.onUpdateReceived(update)
        applicationContext.publishEvent(TelegramUpdateEvent(update))
        JobManager.now {
            for (function in updateFunction) {
                telegramExceptionHandler.invokeHandler(TelegramContext(this@TelegramBot, update)) {
                    function.function.callSuspend(function.any, update)
                }
            }
            for (telegramSubscribe in telegramSubscribeList) {
                telegramExceptionHandler.invokeHandler(TelegramContext(this@TelegramBot, update)) {
                    telegramSubscribe.invoke(this@TelegramBot, update)
                }
            }
        }
    }

    override fun blacklist(): MutableSet<Long> {
        return db.getSet(BLACKLIST)
    }

    override fun getBaseUrl(): String {
        val telegramConfig = applicationContext.getBean(TelegramConfig::class.java)
        return if (telegramConfig.url.isEmpty()) super.getBaseUrl()
        else "${telegramConfig.url}/bot$token/"
    }

    override fun admins(): MutableSet<Long> {
        return db.getSet(ADMINS)
    }
}

class TelegramUpdateEvent(val update: Update): ApplicationEvent(update)

@Component
@ConfigurationProperties(prefix = "kuku.telegram")
class TelegramConfig {
    var token: String = ""
    var username: String = ""
    var creatorId: Long = 0
    var proxyHost: String = ""
    var proxyPort: Int = 0
    var proxyType: ProxyType = ProxyType.NO_PROXY
    var url: String = ""

    @PostConstruct
    fun dockerInit() {
        val runtime = Runtime.getRuntime()
        val process = try {
            runtime.exec("/usr/bin/env")
        } catch (e: Exception) {
            return
        }
        val text = process.inputStream.use {
            it.readAllBytes().toString(charset("utf-8"))
        }
        val line = text.split("\n")
        for (env in line) {
            val arr = env.split("=")
            if (arr.size == 2) {
                val key = arr[0].trim()
                val value = arr[1].trim()
                when (key.uppercase()) {
                    "KUKU_TELEGRAM_TOKEN" -> token = value
                    "KUKU_TELEGRAM_USERNAME" -> username = value
                    "KUKU_TELEGRAM_CREATOR_ID" -> creatorId = value.toLong()
                    "KUKU_TELEGRAM_PROXY_HOST" -> proxyHost = value
                    "KUKU_TELEGRAM_PROXY_PORT" -> proxyPort = value.toInt()
                    "KUKU_TELEGRAM_PROXY_TYPE" -> proxyType = ProxyType.valueOf(value.uppercase())
                    "KUKU_TELEGRAM_URL" -> url = value
                }
            }
        }
    }
}

