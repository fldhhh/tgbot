package me.kuku.telegram.extension

import com.pengrad.telegrambot.model.request.ParseMode
import me.kuku.telegram.context.AbilitySubscriber
import org.springframework.stereotype.Service

@Service
class IndexExtension {

    fun AbilitySubscriber.start() {
        sub("start") {
            sendMessage("""
                自动签到机器人
            """.trimIndent(), parseMode = ParseMode.Markdown)
        }
    }

}
