package com.bera.whitehole.api

import com.bera.whitehole.BuildConfig
import com.bera.whitehole.data.localdb.Preferences
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.extensions.filters.Filter
import com.github.kotlintelegrambot.network.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * The remote API module of the project.
 * Exposes functions to upload and download files.
 */
object BotApi {
    private lateinit var bot: Bot
    fun create() {
        bot = bot {
            token = Preferences.getString(
                Preferences.botToken,
                "123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11"
            )
            dispatch {
                command("start") {
                    val chatId = message.chat.id
                    val result = bot.sendMessage(
                        chatId = ChatId.fromId(chatId),
                        text = chatId.toString()
                    )
                }
            }
        }
    }

    fun startPolling() {
        bot.startPolling()
    }

    suspend fun getChat(chatId: ChatId): Boolean {
        return withContext(Dispatchers.IO) {
            bot.getChat(chatId).isSuccess
        }
    }

    suspend fun sendFile(
        file: File,
        channelId: Long
    ): Pair<retrofit2.Response<Response<Message>?>?, Exception?> {
        return withContext(Dispatchers.IO) {
            bot.sendDocument(
                chatId = ChatId.fromId(channelId),
                document = TelegramFile.ByFile(file),
                disableContentTypeDetection = false,
            )
        }

    }

    suspend fun getFile(fileId: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            bot.downloadFileBytes(fileId)
        }
    }
}