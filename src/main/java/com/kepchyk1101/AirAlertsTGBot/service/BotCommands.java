package com.kepchyk1101.AirAlertsTGBot.service;

/*
    Тут команды и их описание перечислено. Все просто
*/
public enum BotCommands {

    START("/start", "запуск/перезапуск бота"),
    HELP("/help", "faq / допомога"),
    ADD("/add", "додати регіон/місто у список ваших повідомлень"),
    REMOVE("/remove", "видалити регіон/місто зі списку ваших повідомлень"),
    SUBS("/subs", "переглянути список повідомлень");

    private final String command;
    private final String description;

    BotCommands(String command, String description) {
        this.command = command;
        this.description = description;
    }

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

}