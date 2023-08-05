package com.kepchyk1101.AirAlertsTGBot.service.bot;

import com.kepchyk1101.AirAlertsTGBot.config.BotConfig;
import com.kepchyk1101.AirAlertsTGBot.model.Region;
import com.kepchyk1101.AirAlertsTGBot.model.RegionRepository;
import com.kepchyk1101.AirAlertsTGBot.model.User;
import com.kepchyk1101.AirAlertsTGBot.model.UserRepository;
import com.kepchyk1101.AirAlertsTGBot.service.BotCommands;
import com.kepchyk1101.AirAlertsTGBot.service.Consts;
import com.kepchyk1101.AirAlertsTGBot.service.alerts.AlertsController;
import com.kepchyk1101.AirAlertsTGBot.service.alerts.data.AlertData;
import com.kepchyk1101.AirAlertsTGBot.service.alerts.data.AlertInnerData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired private UserRepository userRepository;
    @Autowired private RegionRepository regionRepository;

    private final BotConfig config;
    private final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");

    /*
        Первичная настройка бота что-ли, хз как это назвать, но главное оно работает и я понимаю +- как xD
    */
    public TelegramBot(BotConfig config) {

        this.config = config;

        List<BotCommand> botCommands = new ArrayList<>();

        for (BotCommands command : BotCommands.values())
            botCommands.add(new BotCommand(command.getCommand(), command.getDescription()));

        try {
            this.execute(new SetMyCommands(botCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("error: " + e);
        }

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

        /*
            Ну, тут вроде все более менее аккуратно и понятно.
        */
        if (update.hasMessage() && update.getMessage().hasText()) {

            String userMessage = update.getMessage().getText();
            long userChatId = update.getMessage().getChatId();

            switch (userMessage) {
                case "/start" -> start_CommandInit(userChatId, update.getMessage().getChat().getFirstName());
                case "/help" -> help_CommandInit(userChatId);
                case "/add" -> add_CommandInit(userChatId);
                case "/remove" -> remove_CommandInit(userChatId);
                case "/subs" -> subs_CommandInit(userChatId);
            }

        /*
            Тут начинается сущий п#зд$ц ...
            Как работает CallBackQuery здесь - знает наверное только я и Бог ..
                (и то, я скорее всего через время забуду как я писал тут вообще - и придется поднапрячься)
        */
        } else if (update.hasCallbackQuery()) {

            long userChatId = update.getCallbackQuery().getMessage().getChatId();
            String callBackQuery = update.getCallbackQuery().getData();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();

            if (callBackQuery.equals(Consts.UNIQUE_ADD_ID + Consts.ALL_REGIONS_ID)) {

                User user = userRepository.findById(userChatId).get();
                if (!user.getNotifiesList().equals(Consts.ALL_ID)) {
                    user.setNotifiesList(Consts.ALL_ID);
                    userRepository.save(user);
                    executeEditMarkup(BotMessages.removeAllButtons(userChatId, messageId));
                    executeSendMessage(BotMessages.createAnswer(userChatId, Consts.REGION_SUCCESSFULLY_ADDED_MESSAGE
                            .replace("{regionName}", Consts.ALL_REGIONS_ID)));
                } else
                    executeSendMessage(BotMessages.createAnswer(userChatId, Consts.REGION_ADDING_ERROR_MESSAGE));

            } else if (callBackQuery.equals(Consts.UNIQUE_REMOVE_ID + Consts.ALL_REGIONS_ID)) {

                User user = userRepository.findById(userChatId).get();
                if (!user.getNotifiesList().equals(Consts.NOTHING_ID)) {
                    user.setNotifiesList(Consts.NOTHING_ID);
                    userRepository.save(user);
                    executeEditMarkup(BotMessages.removeAllButtons(userChatId, messageId));
                    executeSendMessage(BotMessages.createAnswer(userChatId, Consts.REGION_SUCCESSFULLY_REMOVED_MESSAGE
                            .replace("{regionName}", Consts.ALL_REGIONS_ID)));
                } else
                    executeSendMessage(BotMessages.createAnswer(userChatId, Consts.REGION_REMOVING_ERROR_MESSAGE
                            .replace("{regionName}", Consts.ALL_REGIONS_ID)));

            } else if (callBackQuery.contains(Consts.UNIQUE_ADD_ID)) {

                for (Region region : regionRepository.findAll()) {
                    String regionName = region.getRegionName();
                    if (callBackQuery.equals(Consts.UNIQUE_ADD_ID + regionName)) {

                        User user = userRepository.findById(userChatId).get();
                        String userNotifiesList = user.getNotifiesList();
                        Region selectedRegion = regionRepository.findByregionName(regionName);
                        long selectedRegionId = selectedRegion.getId();
                        List<String> userNotifiesList_splitted = List.of(userNotifiesList.split(";"));

                        if (userNotifiesList.equals(Consts.ALL_ID)) {

                            executeSendMessage(BotMessages.createAnswer(userChatId, Consts.REGION_ADDING_ERROR_MESSAGE));

                        } else if (userNotifiesList.equals(Consts.NOTHING_ID)) {

                            user.setNotifiesList(String.valueOf(selectedRegionId));
                            userRepository.save(user);
                            executeSendMessage(BotMessages.createAnswer(userChatId, Consts.REGION_SUCCESSFULLY_ADDED_MESSAGE.replace("{regionName}", regionName)));
                            executeEditMarkup(BotMessages.updateAddButtons(userChatId, messageId, userRepository.findById(userChatId).get().getNotifiesList(), regionRepository.findAll()));

                        } else if (userNotifiesList_splitted.contains(String.valueOf(selectedRegionId))) {
                            executeSendMessage(BotMessages.createAnswer(userChatId, Consts.REGION_ADD_ERROR_MESSAGE
                                    .replace("{regionName}", regionName)));
                        } else if (!userNotifiesList_splitted.contains(String.valueOf(selectedRegionId))) {
                            user.setNotifiesList(userNotifiesList += (";" + selectedRegionId));
                            userRepository.save(user);
                            executeSendMessage(BotMessages.createAnswer(userChatId, Consts.REGION_SUCCESSFULLY_ADDED_MESSAGE.replace("{regionName}", regionName)));
                            executeEditMarkup(BotMessages.updateAddButtons(userChatId, messageId, userNotifiesList, regionRepository.findAll()));
                        }

                    }
                }

            } else if (callBackQuery.contains(Consts.UNIQUE_REMOVE_ID)) {
                User user = userRepository.findById(userChatId).get();
                String userNotifiesList = user.getNotifiesList();
                List<String> userNotifiesList_splitted = List.of(userNotifiesList.split(";"));
                Region selectedRegion = regionRepository.findByregionName(callBackQuery.replace(Consts.UNIQUE_REMOVE_ID, ""));
                if (userNotifiesList_splitted.contains(String.valueOf(selectedRegion.getId()))) {
                    userNotifiesList = userNotifiesList.replace(String.valueOf(selectedRegion.getId()), "");

                    if (userNotifiesList.contains(";;"))
                        userNotifiesList = userNotifiesList.replace(";;", ";");
                    else if (userNotifiesList.startsWith(";"))
                        userNotifiesList = userNotifiesList.replaceFirst(";", "");
                    else if (userNotifiesList.endsWith(";"))
                        userNotifiesList = userNotifiesList.substring(0, userNotifiesList.length() - 1);

                    if (userNotifiesList.equals("")) {
                        userNotifiesList = Consts.NOTHING_ID;
                        executeEditMarkup(BotMessages.removeAllButtons(userChatId, messageId));
                    }

                    user.setNotifiesList(userNotifiesList);
                    userRepository.save(user);
                    if (!userNotifiesList.equals(Consts.NOTHING_ID))
                        executeEditMarkup(BotMessages.updateRemoveButtons(userChatId, messageId, userNotifiesList, regionRepository.findAll()));
                    executeSendMessage(BotMessages.createAnswer(userChatId, Consts.REGION_SUCCESSFULLY_REMOVED_MESSAGE
                            .replace("{regionName}", selectedRegion.getRegionName())));
                }
            }


        }

    }

    /*
        Инициализатор команды бота
    */
    private void start_CommandInit(long userChatId, String firstName) {

        executeSendMessage(BotMessages.createAnswer(userChatId, Consts.START_MESSAGE));

        if (userRepository.findById(userChatId).isEmpty()) {
            registerUser(userChatId, firstName);
            List<List<String>> Buttons = new ArrayList<>();
            List<String> all_btn = new ArrayList<>();
            all_btn.add(Consts.ALL_REGIONS_ID);
            Buttons.add(all_btn);
            for (Region region : regionRepository.findAll()) {
                List<String> btnlist = new ArrayList<>();
                btnlist.add(region.getRegionName());
                Buttons.add(btnlist);
            }
            executeSendMessage(BotMessages.createAnswer(userChatId, Consts.ADD_REGIONS_MESSAGE, Buttons, Consts.UNIQUE_ADD_ID));
        } else
            executeSendMessage(BotMessages.createAnswer(userChatId, Consts.ALREADY_REGISTERED_MESSAGE));

    }

    /*
        Инициализатор команды бота
    */
    private void help_CommandInit(long userChatId) {

        executeSendMessage(BotMessages.createAnswer(userChatId, Consts.HELP_MESSAGE));

    }

    /*
        Инициализатор команды бота
    */
    private void add_CommandInit(long userChatId) {

        User user = userRepository.findById(userChatId).get();
        String userNotifiesList = user.getNotifiesList();

        List<List<String>> Buttons = new ArrayList<>();
        List<String> all_btn = new ArrayList<>();
        all_btn.add(Consts.ALL_REGIONS_ID);
        Buttons.add(all_btn);

        if (userNotifiesList.equals(Consts.ALL_ID)) {

            executeSendMessage(BotMessages.createAnswer(userChatId, Consts.REGION_ADDING_ERROR_MESSAGE));

        } else if (userNotifiesList.equals(Consts.NOTHING_ID)) {

            for (Region region : regionRepository.findAll()) {
                List<String> btnlist = new ArrayList<>();
                btnlist.add(region.getRegionName());
                Buttons.add(btnlist);
            }
            executeSendMessage(BotMessages.createAnswer(userChatId, Consts.ADD_REGIONS_MESSAGE, Buttons, Consts.UNIQUE_ADD_ID));

        } else {

            List<String> userNotifiesList_splitted = List.of(userNotifiesList.split(";"));

            for (Region region : regionRepository.findAll()) {

                if (userNotifiesList_splitted.contains(String.valueOf(region.getId()))) continue;

                List<String> btn = new ArrayList<>();
                btn.add(region.getRegionName());
                Buttons.add(btn);

            }

            executeSendMessage(BotMessages.createAnswer(userChatId, Consts.ADD_REGIONS_MESSAGE, Buttons, Consts.UNIQUE_ADD_ID));

        }

    }

    /*
        Инициализатор команды бота
    */
    private void remove_CommandInit(long userChatId) {

        User user = userRepository.findById(userChatId).get();
        String userNotifiesList = user.getNotifiesList();

        if (userNotifiesList.equals(Consts.NOTHING_ID)) {
            executeSendMessage(BotMessages.createAnswer(userChatId, Consts.REGION_REMOVING_ERROR_MESSAGE
                    .replace("{regionName}", "регіон чи місто")));
        } else if (userNotifiesList.equals(Consts.ALL_ID)) {
            executeSendMessage(BotMessages.createAnswer(userChatId, Consts.REGION_REMOVING_MESSAGE, List.of(List.of(Consts.ALL_REGIONS_ID)), Consts.UNIQUE_REMOVE_ID));
        } else {
            List<String> userNotifiesList_splitted = List.of(userNotifiesList.split(";"));
            List<List<String>> Buttons = new ArrayList<>();
            List<String> all_btn = new ArrayList<>();
            all_btn.add(Consts.ALL_REGIONS_ID);
            Buttons.add(all_btn);
            for (Region region : regionRepository.findAll()) {
                if (userNotifiesList_splitted.contains(String.valueOf(region.getId()))) {
                    List<String> btn = new ArrayList<>();
                    btn.add(region.getRegionName());
                    Buttons.add(btn);
                }
            }
            executeSendMessage(BotMessages.createAnswer(userChatId, Consts.REGION_REMOVING_MESSAGE, Buttons, Consts.UNIQUE_REMOVE_ID));
        }

    }

    /*
        Инициализатор команды бота
    */
    private void subs_CommandInit(long userChatId) {

        User user = userRepository.findById(userChatId).get();
        String userNotifiesList = user.getNotifiesList();

        if (userNotifiesList.equals(Consts.ALL_ID)) {

            executeSendMessage(BotMessages.createAnswer(userChatId, Consts.REGION_LIST_MESSAGE
                    .replace("{regionName}", "    · " + Consts.ALL_REGIONS_ID + "\n")));

        } else if (userNotifiesList.equals(Consts.NOTHING_ID)) {

            executeSendMessage(BotMessages.createAnswer(userChatId, Consts.REGION_LIST_ERROR_MESSAGE));

        } else {

            String regionNames = "";
            List<String> userNotifiesList_splitted = List.of(userNotifiesList.split(";"));
            for (String id : userNotifiesList_splitted)
                regionNames += "    · " + regionRepository.findById(Long.parseLong(id)).get().getRegionName() + "\n";

            executeSendMessage(BotMessages.createAnswer(userChatId, Consts.REGION_LIST_MESSAGE
                    .replace("{regionName}", regionNames)));

        }

    }

    /*
        Метод, для регистрации пользователя в бд.
    */
    private void registerUser(long userChatId, String firstName) {

        User user = new User();
        user.setId(userChatId);
        user.setFirstName(firstName);
        user.setNotifiesList(Consts.NOTHING_ID);

        userRepository.save(user);
        log.info("Зарегистрирован новый пользователь. userid: " + userChatId + " , firstName: " + firstName + " .");

    }

    /*
        "Запускатор" отправки сообщений пользователю
    */
    private void executeSendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("error: " + e);
        }
    }

    /*
        "Запускатор" редактирования кнопок у сообщений бота
    */
    private void executeEditMarkup(EditMessageReplyMarkup editMessageReplyMarkup) {
        try {
            execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            log.error("error: " + e);
        }
    }

    /*
        Тут обновляется информация о тревогах в бд. (непосредственно после получения этих данных)
    */
    public void updateInfoAlerts(AlertData alertData) {

        for (AlertInnerData aid : alertData.getStates()) {
            int id = aid.getId();
            boolean newAlertStatus = aid.isAlert();
            Region region = regionRepository.findById( (long) id).get();
            boolean oldAlertStatus = region.isAlertStatus();
            if (oldAlertStatus != newAlertStatus) {
                region.setAlertStatus(newAlertStatus);
                regionRepository.save(region);
                alertUsers(region, newAlertStatus);
            }
        }

    }

    /*
        Тут оповещение пользователей о тревоге в конкретной областе/городе
    */
    public void alertUsers(Region region, boolean alertStatus) {

        Date date = new Date();
        String message = alertStatus ? Consts.START_ALERT : Consts.CANCEL_ALERT;

        message = message
                .replace("{regionName}", region.getRegionName())
                .replace("{time}", formatter.format(date));

        log.info("Оповещение пользователей запущено ...");
        int notifiedUsers = 0;
        long startTime = System.currentTimeMillis();
        for (User user : userRepository.findAll()) {
            String userNotifiesList = user.getNotifiesList();
            List<String> userNotifiesList_splitted = List.of(userNotifiesList.split(";"));
            if (userNotifiesList_splitted.contains(Consts.ALL_ID)) {
                executeSendMessage(BotMessages.createAnswer(user.getId(), message));
                notifiedUsers++;
            } else if (userNotifiesList_splitted.contains(String.valueOf(region.getId()))) {
                executeSendMessage(BotMessages.createAnswer(user.getId(), message));
                notifiedUsers++;
            }
        }
        long endTime = System.currentTimeMillis();
        log.info("Оповещение пользователей завершено за: " + (endTime - startTime) + " мс. Уведомлено: " + notifiedUsers + " пользователей.");

    }

    /*
        Тут скрипт, который каждые 15 секунд получает обновлённые данные о тревогах.
    */
    @Scheduled(fixedDelay = 15000) // 1000 = 1 секунда
    public void getAlertData() {
        try {
            AlertData alertData = AlertsController.getAlertData();
            log.info("Информация о тревогах получена.");
            updateInfoAlerts(alertData);
        } catch (URISyntaxException | InterruptedException | IOException e) {
            log.error("error: " + e);
        }
    }

}