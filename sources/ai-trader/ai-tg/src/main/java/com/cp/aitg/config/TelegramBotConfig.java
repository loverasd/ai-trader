package com.cp.aitg.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Configuration

public class TelegramBotConfig {

    @Bean
    public TelegramLongPollingBot telegramBot() {
        return new TelegramLongPollingBot() {
            @Override
            public String getBotUsername() {
                return "Ai_Trader_SSS_Bot"; // Bot 的用户名
            }

            @Override
            public String getBotToken() {
                return System.getProperty("TELEGRAM_BOT_TOKEN"); // 从环境变量获取 Token
            }

            @Override
            public void onUpdateReceived(Update update) {
                // 处理来自 Telegram 的消息
            }

            public void sendMessageToChat(String chatId, String message) {
                try {
                    execute(new SendMessage(chatId, message));
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
