package com.bera.whitehole.api

import com.bera.whitehole.data.localdb.Preferences
import com.bera.whitehole.utils.Constants.SAMPLE_API_KEY
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.network.Response
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The remote API module of the project.
 * Exposes functions to upload and download files.
 */
object BotApi {
    private lateinit var bot: Bot
    var chatId: Long? = null

    fun create() {
        bot = bot {
            token = Preferences.getEncryptedString(
                Preferences.botToken,
                SAMPLE_API_KEY
            )
            dispatch {
                command("start") {
                    chatId = message.chat.id
                    chatId?.let {
                        val result = bot.sendMessage(
                            chatId = ChatId.fromId(it),
                            text = chatId.toString()
                        )
                    }
                }
            }
        }
    }

    fun startPolling() {
        bot.startPolling()
    }

    fun stopPolling() {
        bot.stopPolling()
    }

    suspend fun getChat(chatId: ChatId): Boolean {
        return withContext(Dispatchers.IO) {
            bot.getChat(chatId).isSuccess
        }
    }

    suspend fun sendFile(
        file: File,
        channelId: Long,
    ): Pair<retrofit2.Response<Response<Message>?>?, Exception?> {
        return withContext(Dispatchers.IO) {
            bot.sendDocument(
                chatId = ChatId.fromId(channelId),
                document = TelegramFile.ByFile(file),
                disableContentTypeDetection = false
            )
        }
    }

    suspend fun getFile(fileId: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            bot.downloadFileBytes(fileId)
        }
    }
}