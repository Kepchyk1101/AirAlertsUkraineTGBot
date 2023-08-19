package com.kepchyk1101.AirAlertsTGBot.service.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class Executors {

    @Autowired @Lazy
    private TelegramBot telegramBot;

    public void executeSendMessage(SendMessage sendMessage) {

        try {
            telegramBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки sendMessage: " + e);
        }

    }

    public void executeEditMessageReplyMarkup(EditMessageReplyMarkup editMessageReplyMarkup) {

        try {
            telegramBot.execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки editMessageReplyMarkup: " + e);
        }

    }


}