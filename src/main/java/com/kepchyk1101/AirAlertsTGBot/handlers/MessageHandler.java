package com.kepchyk1101.AirAlertsTGBot.handlers;

import com.kepchyk1101.AirAlertsTGBot.model.Region;
import com.kepchyk1101.AirAlertsTGBot.model.RegionRepository;
import com.kepchyk1101.AirAlertsTGBot.model.User;
import com.kepchyk1101.AirAlertsTGBot.model.UserRepository;
import com.kepchyk1101.AirAlertsTGBot.service.auth.AuthService;
import com.kepchyk1101.AirAlertsTGBot.service.bot.MessageSender;
import com.kepchyk1101.AirAlertsTGBot.utils.Consts;
import com.kepchyk1101.AirAlertsTGBot.utils.TextFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class MessageHandler implements IHandler {

    @Autowired private UserRepository userRepository;
    @Autowired private RegionRepository regionRepository;

    @Autowired private MessageSender messageSender;

    @Autowired private AuthService authService;

    private final String BOT_IDENTIFY = "@AirAlertsUkraine_bot";

    private final String START_COMMAND = "/start";
    private final String HELP_COMMAND = "/help";
    private final String ADD_COMMAND = "/add";
    private final String REMOVE_COMMAND = "/remove";
    private final String SUBS_COMMAND = "/subs";
    private final String STATUS_COMMAND = "/status";

    @Override
    public void handle(Update update) {

        final Message message = update.getMessage();
        final long userChatId = message.getChatId();
        final String userText = message.getText();

        // Во-первых проверяем что пришла хотя-бы вообще команда, чтобы лишний раз не делать проверок
        if (userText.startsWith("/")) {

            // Если эта команда от пользователя - реагируем
            if (message.isUserMessage()) {

                switch (userText) {
                    case START_COMMAND -> startCommand(userChatId, message);
                    case HELP_COMMAND -> helpCommand(userChatId);
                    case ADD_COMMAND -> addCommand(userChatId);
                    case REMOVE_COMMAND -> removeCommand(userChatId);
                    case SUBS_COMMAND -> subsCommand(userChatId);
                    case STATUS_COMMAND -> statusCommand(userChatId);
                }

            // Если эта команда от группового чата - реагируем только если в конце приписка @AirAlertsUkraine_bot
            } else if (message.isGroupMessage()) {

                switch (userText) {
                    case START_COMMAND + BOT_IDENTIFY -> startCommand(userChatId, message);
                    case HELP_COMMAND + BOT_IDENTIFY -> helpCommand(userChatId);
                    case ADD_COMMAND + BOT_IDENTIFY -> addCommand(userChatId);
                    case REMOVE_COMMAND + BOT_IDENTIFY -> removeCommand(userChatId);
                    case STATUS_COMMAND + BOT_IDENTIFY -> statusCommand(userChatId);
                }

            }

        }

    }

    private void startCommand(long userChatId, Message message) {

        messageSender.sendMessage(userChatId, Consts.START_MESSAGE);

        if (userRepository.findById(userChatId).isEmpty()) {

            authService.registerUser(userChatId, message);

            List<List<String>> buttons = new ArrayList<>();
            List<String> allButton = new ArrayList<>();
            allButton.add(Consts.ALL_REGIONS_ID);
            buttons.add(allButton);
            for (Region region : regionRepository.findAll()) {
                List<String> buttonList = new ArrayList<>();
                buttonList.add(region.getRegionName());
                buttons.add(buttonList);
            }
            messageSender.sendMessage(userChatId, Consts.ADD_REGIONS_MESSAGE, buttons, Consts.UNIQUE_ADD_ID);
        } else
            messageSender.sendMessage(userChatId, Consts.ALREADY_REGISTERED_MESSAGE);

        log.info("Пользователь: {} успешно воспользовался командой: {}", userChatId, START_COMMAND);

    }

    private void helpCommand(long userChatId) {

        messageSender.sendMessage(userChatId, Consts.HELP_MESSAGE);
        log.info("Пользователь: {} успешно воспользовался командой: {}", userChatId, HELP_COMMAND);

    }

    private void addCommand(long userChatId) {

        User user = userRepository.findById(userChatId).get();
        String userNotifiesList = user.getNotifiesList();

        List<List<String>> buttons = new ArrayList<>();
        List<String> allButton = new ArrayList<>();
        allButton.add(Consts.ALL_REGIONS_ID);
        buttons.add(allButton);

        if (userNotifiesList.equals(Consts.ALL_ID)) {

            messageSender.sendMessage(userChatId, Consts.REGION_ADDING_ERROR_MESSAGE);

        } else if (userNotifiesList.equals(Consts.NOTHING_ID)) {

            for (Region region : regionRepository.findAll()) {
                List<String> buttonList = new ArrayList<>();
                buttonList.add(region.getRegionName());
                buttons.add(buttonList);
            }
            messageSender.sendMessage(userChatId, Consts.ADD_REGIONS_MESSAGE, buttons, Consts.UNIQUE_ADD_ID);

        } else {

            List<String> userNotifiesListSplitted = List.of(userNotifiesList.split(";"));

            for (Region region : regionRepository.findAll()) {

                if (userNotifiesListSplitted.contains(String.valueOf(region.getId()))) continue;

                List<String> btn = new ArrayList<>();
                btn.add(region.getRegionName());
                buttons.add(btn);

            }

            messageSender.sendMessage(userChatId, Consts.ADD_REGIONS_MESSAGE, buttons, Consts.UNIQUE_ADD_ID);

        }

        log.info("Пользователь: {} успешно воспользовался командой: {}", userChatId, ADD_COMMAND);

    }

    private void removeCommand(long userChatId) {

        User user = userRepository.findById(userChatId).get();
        String userNotifiesList = user.getNotifiesList();

        if (userNotifiesList.equals(Consts.NOTHING_ID)) {
            messageSender.sendMessage(userChatId, TextFormatter.format(Consts.REGION_REMOVING_ERROR_MESSAGE, new HashMap<>() {{
                put("{regionName}", "регіон чи місто");
            }}));
        } else if (userNotifiesList.equals(Consts.ALL_ID)) {
            messageSender.sendMessage(userChatId, Consts.REGION_REMOVING_MESSAGE, List.of(List.of(Consts.ALL_REGIONS_ID)), Consts.UNIQUE_REMOVE_ID);
        } else {
            List<String> userNotifiesListSplitted = List.of(userNotifiesList.split(";"));
            List<List<String>> buttons = new ArrayList<>();
            List<String> allButton = new ArrayList<>();
            allButton.add(Consts.ALL_REGIONS_ID);
            buttons.add(allButton);
            for (Region region : regionRepository.findAll()) {
                if (userNotifiesListSplitted.contains(String.valueOf(region.getId()))) {
                    List<String> button = new ArrayList<>();
                    button.add(region.getRegionName());
                    buttons.add(button);
                }
            }
            messageSender.sendMessage(userChatId ,Consts.REGION_REMOVING_MESSAGE, buttons, Consts.UNIQUE_REMOVE_ID);
        }

        log.info("Пользователь: {} успешно воспользовался командой: {}", userChatId, REMOVE_COMMAND);

    }

    private void subsCommand(long userChatId) {

        User user = userRepository.findById(userChatId).get();
        String userNotifiesList = user.getNotifiesList();

        if (userNotifiesList.equals(Consts.ALL_ID)) {

            messageSender.sendMessage(userChatId, TextFormatter.format(Consts.REGION_LIST_MESSAGE, new HashMap<>() {{
                put("{regionName}", "    · " + Consts.ALL_REGIONS_ID + "\n");
            }}));

        } else if (userNotifiesList.equals(Consts.NOTHING_ID)) {

            messageSender.sendMessage(userChatId, Consts.REGION_LIST_ERROR_MESSAGE);

        } else {

            StringBuilder regionNames = new StringBuilder();
            List<String> userNotifiesListSplitted = List.of(userNotifiesList.split(";"));
            for (String id : userNotifiesListSplitted)
                regionNames.append("    · ").append(regionRepository.findById(Long.parseLong(id)).get().getRegionName()).append("\n");

            messageSender.sendMessage(userChatId, TextFormatter.format(Consts.REGION_LIST_MESSAGE, new HashMap<>() {{
                put("{regionName}", regionNames.toString());
            }}));

        }

        log.info("Пользователь: {} успешно воспользовался командой: {}", userChatId, SUBS_COMMAND);

    }

    private void statusCommand(long userChatId) {

        StringBuilder statuses = new StringBuilder("");

        for (Region region : regionRepository.findAll()) {

            if (region.isAlertStatus())
                statuses.append("🔴 · " + region.getRegionName() + "\n");
            else
                statuses.append("🟢 · " + region.getRegionName() + "\n");

        }

        messageSender.sendMessage(userChatId, TextFormatter.format(Consts.REGIONS_ALERTSTATUS_MESSAGE, new HashMap<>() {{
            put("{region}", String.valueOf(statuses));
        }}));

        log.info("Пользователь: {} успешно воспользовался командой: {}", userChatId, STATUS_COMMAND);

    }

}
