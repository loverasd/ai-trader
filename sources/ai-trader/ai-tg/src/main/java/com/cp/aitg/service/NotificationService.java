package com.cp.aitg.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class NotificationService {
    private final TelegramLongPollingBot telegramBot;

    @Autowired
    public NotificationService(TelegramLongPollingBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void sendPriceAlertNotification(String symbol, double targetPrice,double currentPrice) {
        SendMessage message = new SendMessage();
        String telegramChatId = System.getProperty("TELEGRAM_CHAT_ID");

        message.setChatId("7085075035");
        message.setText("Price Alert: " + symbol + " reached " + currentPrice);
        
        try {
            telegramBot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
