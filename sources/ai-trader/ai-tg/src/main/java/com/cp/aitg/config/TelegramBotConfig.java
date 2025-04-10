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
                return "Ai_Trader_SSS_Bot";
            }

            @Override
            public String getBotToken() {
                return System.getProperty("telegram.bot.token");
            }

            @Override
            public void onUpdateReceived(Update update) {
                // 消息接收逻辑（可忽略）
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
