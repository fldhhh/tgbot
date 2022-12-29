package me.kuku.telegram.scheduled

import kotlinx.coroutines.delay
import me.kuku.telegram.config.TelegramBot
import me.kuku.telegram.entity.Status
import me.kuku.telegram.entity.TwitterService
import me.kuku.telegram.logic.TwitterLogic
import me.kuku.telegram.logic.TwitterPojo
import me.kuku.utils.OkHttpKtUtils
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendVideo
import org.telegram.telegrambots.meta.api.objects.InputFile
import java.lang.Long.max
import java.util.concurrent.TimeUnit

@Component
class TwitterScheduled(
    private val twitterService: TwitterService,
    private val telegramBot: TelegramBot
) {

    private val userMap = mutableMapOf<Long, Long>()

    @Scheduled(fixedDelay = 2, timeUnit = TimeUnit.MINUTES)
    suspend fun push() {
        val entityList = twitterService.findByPush(Status.ON)
        for (entity in entityList) {
            val tgId = entity.tgId
            val list = TwitterLogic.friendTweet(entity).sortedBy { -it.id }
            delay(3000)
            if (list.isEmpty()) continue
            val newList = mutableListOf<TwitterPojo>()
            if (userMap.containsKey(tgId)) {
                val oldId = userMap[tgId]!!
                for (twitterPojo in list) {
                    if (twitterPojo.id <= oldId) break
                    newList.add(twitterPojo)
                }
                for (twitterPojo in newList) {
                    val text = "#Twitter新推推送\n${TwitterLogic.convertStr(twitterPojo)}"
                    val videoUrl = if (twitterPojo.videoList.isNotEmpty()) twitterPojo.videoList[0]
                    else if (twitterPojo.forwardVideoList.isNotEmpty()) twitterPojo.forwardVideoList[0]
                    else ""
                    try {
                        if (videoUrl.isNotEmpty()) {
                            OkHttpKtUtils.getByteStream(videoUrl).use {
                                val sendVideo = SendVideo(tgId.toString(), InputFile(it, "${twitterPojo.id}.mp4"))
                                sendVideo.caption = text
                                telegramBot.execute(sendVideo)
                            }
                        } else if (twitterPojo.photoList.isNotEmpty() || twitterPojo.forwardPhotoList.isNotEmpty()) {
                            val imageList = twitterPojo.photoList
                            imageList.addAll(twitterPojo.forwardPhotoList)
                            telegramBot.sendPic(tgId, text, imageList)
                        } else telegramBot.silent().send(text, tgId)
                    } catch (e: Exception) {
                        telegramBot.silent().send(text, tgId)
                    }
                }
            }
            userMap[tgId] = max(list[0].id, userMap[tgId] ?: 0)
        }
    }

}
