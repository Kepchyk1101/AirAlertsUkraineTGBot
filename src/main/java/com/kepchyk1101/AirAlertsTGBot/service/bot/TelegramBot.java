package com.kepchyk1101.AirAlertsTGBot.service.bot;

import com.kepchyk1101.AirAlertsTGBot.config.BotConfig;
import com.kepchyk1101.AirAlertsTGBot.handlers.CallbackQueryHandler;
import com.kepchyk1101.AirAlertsTGBot.handlers.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired private MessageHandler messageHandler;
    @Autowired private CallbackQueryHandler callbackQueryHandler;

    private final BotConfig config;

    public TelegramBot(BotConfig config) {

        this.config = config;

        executeSetMyCommand(new SetMyCommands(List.of(
                new BotCommand("start", "запуск бота"),
                new BotCommand("help", "довідка"),
                new BotCommand("add", "додати область/місто до списку повідомлень"),
                new BotCommand("remove", "видалити область/місто зі списку повідомлень"),
                new BotCommand("subs", "переглянути список повідомлень"),
                new BotCommand("status", "дізнатися стан тривог у областях/містах")),
                new BotCommandScopeDefault(), null));

    }

    @Override
    public String getBotUsername() {
        return config.getBotToken();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        // Если пришёл текст - направляем в обработчик текста
        if (update.hasMessage() && update.getMessage().hasText())
            messageHandler.handle(update);

        // Если пришёл callbackquery - направляем в обработчик callbackquery
        else if (update.hasCallbackQuery())
            callbackQueryHandler.handle(update);

    }

    // Почему этот метод не в Executors ? - на этом моменте у меня немного сгорело и я решил его оставить тут, не хотел он работать.
    private void executeSetMyCommand(SetMyCommands setMyCommands) {

        try {
            this.execute(setMyCommands);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

}