package com.kepchyk1101.AirAlertsTGBot.service.bot;

import com.kepchyk1101.AirAlertsTGBot.model.Region;
import com.kepchyk1101.AirAlertsTGBot.service.utils.Consts;
import com.vdurmont.emoji.EmojiParser;
import org.springframework.lang.Nullable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

/*
    Тут создаются сообщения, сообщения с кнопками, всякие обновления кнопок, удаления кнопок и т.д.
*/
public class BotMessages {

    public static SendMessage createAnswer(long userChatId, String message) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setChatId(userChatId);
        sendMessage.setText(EmojiParser.parseToUnicode(message));

        return sendMessage;

    }

    public static SendMessage createAnswer(long userChatId, String message, List<List<String>> ButtonsNames, @Nullable String additionalIdentify_CBQ) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setChatId(userChatId);
        sendMessage.setText(EmojiParser.parseToUnicode(message));

        if (additionalIdentify_CBQ != null)
            sendMessage.setReplyMarkup(createInlineKeyboardMarkup(ButtonsNames, additionalIdentify_CBQ));
        else
            sendMessage.setReplyMarkup(createInlineKeyboardMarkup(ButtonsNames, null));


        return sendMessage;

    }

    public static InlineKeyboardMarkup createInlineKeyboardMarkup(List<List<String>> buttonNames, @Nullable String additionalIdentify_CBQ) {

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        for (List<String> rowInLine : buttonNames) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (String buttonsName : rowInLine) {
                InlineKeyboardButton button = new InlineKeyboardButton(buttonsName);
                if (additionalIdentify_CBQ != null)
                    button.setCallbackData(additionalIdentify_CBQ + buttonsName);
                else
                    button.setCallbackData(buttonsName);
                row.add(button);
            }
            rowsInLine.add(row);
        }

        markup.setKeyboard(rowsInLine);

        return markup;

    }

    public static EditMessageReplyMarkup removeAllButtons(long userChatId, int messageId) {

        EditMessageReplyMarkup editMessage = new EditMessageReplyMarkup();
        editMessage.setChatId(userChatId);
        editMessage.setMessageId(messageId);
        editMessage.setReplyMarkup(null);

        return editMessage;

    }

    public static EditMessageReplyMarkup updateRemoveButtons(long userChatId, int messageId, String userNotifiesList, Iterable<Region> regions) {

        EditMessageReplyMarkup editMessage = new EditMessageReplyMarkup();
        editMessage.setChatId(userChatId);
        editMessage.setMessageId(messageId);

        List<List<String>> Buttons = new ArrayList<>();
        List<String> all_btn = new ArrayList<>();
        all_btn.add(Consts.ALL_REGIONS_ID);
        Buttons.add(all_btn);

        List<String> userNotifiesList_splitted = List.of(userNotifiesList.split(";"));

        for (Region region : regions) {
            if (!userNotifiesList_splitted.contains(String.valueOf(region.getId()))) continue;
            List<String> btn = new ArrayList<>();
            btn.add(region.getRegionName());
            Buttons.add(btn);
        }

        editMessage.setReplyMarkup(BotMessages.createInlineKeyboardMarkup(Buttons, Consts.UNIQUE_REMOVE_ID));

       return editMessage;

    }

    public static EditMessageReplyMarkup updateAddButtons(long userChatId, int messageId, String userNotifiesList, Iterable<Region> regions) {

        EditMessageReplyMarkup editMessage = new EditMessageReplyMarkup();
        editMessage.setChatId(userChatId);
        editMessage.setMessageId(messageId);

        List<List<String>> Buttons = new ArrayList<>();
        List<String> all_btn = new ArrayList<>();
        all_btn.add(Consts.ALL_REGIONS_ID);
        Buttons.add(all_btn);

        List<String> userNotifiesList_splitted = List.of(userNotifiesList.split(";"));

        for (Region region : regions) {
            if (userNotifiesList_splitted.contains(String.valueOf(region.getId()))) continue;
            List<String> btn = new ArrayList<>();
            btn.add(region.getRegionName());
            Buttons.add(btn);
        }

        editMessage.setReplyMarkup(BotMessages.createInlineKeyboardMarkup(Buttons, Consts.UNIQUE_ADD_ID));

        return editMessage;

    }

}