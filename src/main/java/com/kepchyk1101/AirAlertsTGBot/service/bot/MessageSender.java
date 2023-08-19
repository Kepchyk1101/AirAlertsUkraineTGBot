package com.kepchyk1101.AirAlertsTGBot.service.bot;

import com.kepchyk1101.AirAlertsTGBot.model.Region;
import com.kepchyk1101.AirAlertsTGBot.utils.Consts;
import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class MessageSender {

    @Autowired
    private Executors executors;

    public void sendMessage(long userChatId, String text) {

        SendMessage sendMessage = SendMessage
                .builder()
                .chatId(userChatId)
                .text(EmojiParser.parseToUnicode(text))
                .parseMode("HTML")
                .build();

        executors.executeSendMessage(sendMessage);

    }

    public void sendMessage(long userChatId, String text, List<List<String>> buttons, @Nullable String CBQIdentify) {

        SendMessage sendMessage = SendMessage
                .builder()
                .chatId(userChatId)
                .text(EmojiParser.parseToUnicode(text))
                .replyMarkup(createInlineKeyBoardMarkup(buttons, CBQIdentify))
                .parseMode("HTML")
                .build();

        executors.executeSendMessage(sendMessage);

    }

    private InlineKeyboardMarkup createInlineKeyBoardMarkup(List<List<String>> buttons, @Nullable String CBQIdentify) {

        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        for (List<String> rowInLine : buttons) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (String buttonsName : rowInLine) {
                InlineKeyboardButton button = new InlineKeyboardButton(buttonsName);
                if (CBQIdentify != null)
                    button.setCallbackData(CBQIdentify + buttonsName);
                else
                    button.setCallbackData(buttonsName);
                row.add(button);
            }
            rowsInLine.add(row);
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rowsInLine);

        return markup;

    }

    public void removeAllButtons(long userChatId, int messageId) {

        EditMessageReplyMarkup editMessage = new EditMessageReplyMarkup();
        editMessage.setChatId(userChatId);
        editMessage.setMessageId(messageId);
        editMessage.setReplyMarkup(null);

        executors.executeEditMessageReplyMarkup(editMessage);

    }

    public void updateAddButtons(long userChatId, int messageId, String userNotifiesList, Iterable<Region> regions) {

        EditMessageReplyMarkup editMessage = new EditMessageReplyMarkup();
        editMessage.setChatId(userChatId);
        editMessage.setMessageId(messageId);

        List<List<String>> buttons = new ArrayList<>();
        List<String> allButton = new ArrayList<>();
        allButton.add(Consts.ALL_REGIONS_ID);
        buttons.add(allButton);

        List<String> userNotifiesListSplitted = List.of(userNotifiesList.split(";"));

        for (Region region : regions) {
            if (userNotifiesListSplitted.contains(String.valueOf(region.getId()))) continue;
            List<String> button = new ArrayList<>();
            button.add(region.getRegionName());
            buttons.add(button);
        }

        editMessage.setReplyMarkup(createInlineKeyBoardMarkup(buttons, Consts.UNIQUE_ADD_ID));

        executors.executeEditMessageReplyMarkup(editMessage);

    }

    public void updateRemoveButtons(long userChatId, int messageId, String userNotifiesList, Iterable<Region> regions) {

        EditMessageReplyMarkup editMessage = new EditMessageReplyMarkup();
        editMessage.setChatId(userChatId);
        editMessage.setMessageId(messageId);

        List<List<String>> buttons = new ArrayList<>();
        List<String> allButton = new ArrayList<>();
        allButton.add(Consts.ALL_REGIONS_ID);
        buttons.add(allButton);

        List<String> userNotifiesListSplitted = List.of(userNotifiesList.split(";"));

        for (Region region : regions) {
            if (!userNotifiesListSplitted.contains(String.valueOf(region.getId()))) continue;
            List<String> button = new ArrayList<>();
            button.add(region.getRegionName());
            buttons.add(button);
        }

        editMessage.setReplyMarkup(createInlineKeyBoardMarkup(buttons, Consts.UNIQUE_REMOVE_ID));

        executors.executeEditMessageReplyMarkup(editMessage);

    }

}