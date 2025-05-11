package me.kuku.telegram.extension

import com.fasterxml.jackson.databind.JsonNode
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.model.request.InputMediaPhoto
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.request.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.kuku.telegram.context.*
import me.kuku.telegram.entity.BiliBiliService
import me.kuku.telegram.logic.BiliBiliLogic
import me.kuku.telegram.logic.YgoLogic
import me.kuku.telegram.utils.RegexUtils
import me.kuku.telegram.utils.client
import me.kuku.telegram.utils.githubCommit
import org.springframework.stereotype.Service

@Service
class ToolExtension(
    private val ygoLogic: YgoLogic,
    private val biliBiliService: BiliBiliService
) {

    private val mutex = Mutex()

    fun MixSubscribe.queryYgoCard() {
        ability {
            sub("ygo", 1) {
                val cardList = ygoLogic.search(firstArg())
                val list = mutableListOf<Array<InlineKeyboardButton>>()
                for (i in cardList.indices) {
                    val card = cardList[i]
                    list.add(arrayOf(InlineKeyboardButton(card.chineseName).callbackData("ygoCard-${card.cardPassword}")))
                }
                sendMessage("请选择查询的卡片", replyKeyboard = InlineKeyboardMarkup(*list.toTypedArray()))
            }
        }
        telegram {
            callbackStartsWith("ygoCard") {
                answerCallbackQuery("获取成功")
                val id = query.data().split("-")[1]
                val card = ygoLogic.searchDetail(id.toLong())
                val sendPhoto = SendPhoto(chatId, client.get(card.imageUrl).bodyAsBytes())
                sendPhoto.caption("中文名：${card.chineseName}\n日文名：${card.japaneseName}\n英文名：${card.englishName}\n效果：\n${card.effect}\n链接：${card.url}")
                bot.asyncExecute(sendPhoto)
            }
        }
    }

    fun AbilitySubscriber.tool() {
        sub("info", locality = Locality.ALL, privacy=Privacy.PUBLIC) {
            val id = message.chat().id()
            val messageThreadId = message.messageThreadId()
            sendMessage("""
                chatId: `$id`
                messageThreadId: `$messageThreadId`
            """.trimIndent(), parseMode = ParseMode.Markdown)
        }
        sub("updatelog") {
            val commitList = githubCommit()
            val list = mutableListOf<Array<InlineKeyboardButton>>()
            for (githubCommit in commitList) {
                list.add(arrayOf(InlineKeyboardButton("${githubCommit.date} - ${githubCommit.message}").callbackData("none")))
            }
            sendMessage("更新日志", InlineKeyboardMarkup(*list.stream().limit(6).toList().toTypedArray()))
        }
    }

    fun AbilitySubscriber.loLiCon() {
        sub("lolicon", locality = Locality.ALL) {
            val r18 = kotlin.runCatching {
                if (firstArg().lowercase() == "r18") 1 else 0
            }.getOrDefault(0)
            val jsonNode = client.get("https://api.lolicon.app/setu/v2?r18=$r18").body<JsonNode>()
            val url = jsonNode["data"][0]["urls"]["original"].asText()
            val bytes = client.get(url).bodyAsBytes()
            if (bytes.size > 1024 * 10 * 1024) {
                val sendDocument = SendDocument(chatId, bytes).fileName("lolicon.jpg")
                message.messageThreadId()?.let {
                    sendDocument.messageThreadId(it)
                }
                bot.asyncExecute(sendDocument)
            } else {
                val sendPhoto = SendPhoto(chatId, bytes)
                message.messageThreadId()?.let {
                    sendPhoto.messageThreadId(it)
                }
                bot.asyncExecute(sendPhoto)
            }
        }
        sub("loliconmulti", locality = Locality.ALL) {
            val r18 = kotlin.runCatching {
                if (firstArg().lowercase() == "r18") 1 else 0
            }.getOrDefault(0)
            val jsonNode = client.get("https://api.lolicon.app/setu/v2?num=5&r18=$r18").body<JsonNode>()
            val list = jsonNode["data"].map { node -> node["urls"]["original"].asText() }
            val inputMediaList = mutableListOf<InputMediaPhoto>()
            for (i in list.indices) {
                val s = list[i]
                val bytes = client.get(s).bodyAsBytes()
                if (bytes.size > 1024 * 10 * 1024) continue
                val mediaPhoto = InputMediaPhoto(bytes)
                inputMediaList.add(mediaPhoto)
            }
            val sendMediaGroup = SendMediaGroup(chatId, *inputMediaList.toTypedArray())
            message.messageThreadId()?.let {
                sendMediaGroup.messageThreadId(it)
            }
            bot.asyncExecute(sendMediaGroup)
        }
    }

    fun AbilitySubscriber.dyna() {
        sub("bv", input = 1, locality = Locality.ALL) {
            mutex.withLock {
                val biliBiliEntity = biliBiliService.findByTgId(tgId)
                    ?: biliBiliService.findAll().randomOrNull() ?: errorAnswerCallbackQuery("未绑定哔哩哔哩，无法获取视频")
                val bvId = firstArg()
                val file = BiliBiliLogic.videoByBvId(biliBiliEntity, bvId)
                if (file.length() > 1024 * 1024 * 1024 * 2L) {
                    sendMessage("该视频大于2G，无法发送")
                } else {
                    val sendVideo =
                        SendVideo(chatId, file).caption(bvId)
                    messageThreadId?.let { sendVideo.messageThreadId(it) }
                    bot.asyncExecute(sendVideo)
                }
                file.delete()
            }
        }

        sub("dy", 1, locality = Locality.ALL) {
            mutex.withLock {
                val urlArg = firstArg()
                val locationResponse = client.get(urlArg) {
                    userAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1")
                }
                val htmlUrl = locationResponse.headers["Location"] ?: error("获取抖音视频失败")
                val html = client.get(htmlUrl) {
                    userAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1")
                }.bodyAsText()
                val id = RegexUtils.extract(html, "(?<=video_id=)", "&") ?: error("获取抖音视频失败")
                val response = client.get("https://m.douyin.com/aweme/v1/playwm/?video_id=$id&ratio=720p&line=0")
                val url = response.headers["Location"] ?: error("获取抖音视频失败")
                val bytes = client.get(url).body<ByteArray>()
                val sendVideo = SendVideo(chatId, bytes).fileName("$id.mp4")
                messageThreadId?.let { sendVideo.messageThreadId(it) }
                bot.asyncExecute(sendVideo)
            }
        }
    }

}
