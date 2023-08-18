package com.kepchyk1101.AirAlertsTGBot.service.bot;

import com.kepchyk1101.AirAlertsTGBot.config.BotConfig;
import com.kepchyk1101.AirAlertsTGBot.model.*;
import com.kepchyk1101.AirAlertsTGBot.service.utils.Consts;
import com.kepchyk1101.AirAlertsTGBot.service.alerts.AlertsController;
import com.kepchyk1101.AirAlertsTGBot.service.alerts.data.AlertData;
import com.kepchyk1101.AirAlertsTGBot.service.alerts.data.AlertInnerData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
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
    @Autowired private AdRepository adRepository;

    private final BotConfig config;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm");
    private final AlertsController alertsController;

    private final String START_COMMAND = "/start";
    private final String HELP_COMMAND = "/help";
    private final String ADD_COMMAND = "/add";
    private final String REMOVE_COMMAND = "/remove";
    private final String SUBS_COMMAND = "/subs";

    /*
        Первичная настройка бота что-ли, хз как это назвать, но главное оно работает и я понимаю +- как xD
    */
    public TelegramBot(BotConfig config) {
        this.config = config;
        alertsController = new AlertsController(config);
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
                case START_COMMAND -> start_CommandInit(userChatId, update.getMessage().getChat().getFirstName());
                case HELP_COMMAND -> help_CommandInit(userChatId);
                case ADD_COMMAND -> add_CommandInit(userChatId);
                case REMOVE_COMMAND -> remove_CommandInit(userChatId);
                case SUBS_COMMAND -> subs_CommandInit(userChatId);
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
                    log.info("Пользователь: {} успешно добавил {} в свой список уведомлений.", userChatId, Consts.ALL_REGIONS_ID);
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
                    log.info("Пользователь: {} успешно удалил {} со своего списка уведомлений.", userChatId, Consts.ALL_REGIONS_ID);
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
                        List<String> userNotifiesListSplitted = List.of(userNotifiesList.split(";"));

                        if (userNotifiesList.equals(Consts.ALL_ID)) {

                            executeSendMessage(BotMessages.createAnswer(userChatId, Consts.REGION_ADDING_ERROR_MESSAGE));

                        } else if (userNotifiesList.equals(Consts.NOTHING_ID)) {

                            user.setNotifiesList(String.valueOf(selectedRegionId));
                            userRepository.save(user);
                            executeSendMessage(BotMessages.createAnswer(userChatId, Consts.REGION_SUCCESSFULLY_ADDED_MESSAGE.replace("{regionName}", regionName)));
                            executeEditMarkup(BotMessages.updateAddButtons(userChatId, messageId, userRepository.findById(userChatId).get().getNotifiesList(), regionRepository.findAll()));
                            log.info("Пользователь: {} успешно добавил {} в свой список уведомлений.", userChatId, regionName);

                        } else if (userNotifiesListSplitted.contains(String.valueOf(selectedRegionId))) {
                            executeSendMessage(BotMessages.createAnswer(userChatId, Consts.REGION_ADD_ERROR_MESSAGE
                                    .replace("{regionName}", regionName)));
                        } else if (!userNotifiesListSplitted.contains(String.valueOf(selectedRegionId))) {
                            user.setNotifiesList(userNotifiesList += (";" + selectedRegionId));
                            userRepository.save(user);
                            executeSendMessage(BotMessages.createAnswer(userChatId, Consts.REGION_SUCCESSFULLY_ADDED_MESSAGE.replace("{regionName}", regionName)));
                            executeEditMarkup(BotMessages.updateAddButtons(userChatId, messageId, userNotifiesList, regionRepository.findAll()));
                            log.info("Пользователь: {} успешно добавил {} в свой список уведомлений.", userChatId, regionName);
                        }

                    }
                }

            } else if (callBackQuery.contains(Consts.UNIQUE_REMOVE_ID)) {
                User user = userRepository.findById(userChatId).get();
                String userNotifiesList = user.getNotifiesList();
                List<String> userNotifiesListSplitted = List.of(userNotifiesList.split(";"));
                Region selectedRegion = regionRepository.findByregionName(callBackQuery.replace(Consts.UNIQUE_REMOVE_ID, ""));
                if (userNotifiesListSplitted.contains(String.valueOf(selectedRegion.getId()))) {
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
                    log.info("Пользователь: {} успешно удалил {} со своего списка уведомлений.", userChatId, selectedRegion.getRegionName());
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

        log.info("Пользователь: {} успешно воспользовался командой: {}", userChatId, START_COMMAND);

    }

    /*
        Инициализатор команды бота
    */
    private void help_CommandInit(long userChatId) {
        executeSendMessage(BotMessages.createAnswer(userChatId, Consts.HELP_MESSAGE));
        log.info("Пользователь: {} успешно воспользовался командой: {}", userChatId, HELP_COMMAND);
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

            List<String> userNotifiesListSplitted = List.of(userNotifiesList.split(";"));

            for (Region region : regionRepository.findAll()) {

                if (userNotifiesListSplitted.contains(String.valueOf(region.getId()))) continue;

                List<String> btn = new ArrayList<>();
                btn.add(region.getRegionName());
                Buttons.add(btn);

            }

            executeSendMessage(BotMessages.createAnswer(userChatId, Consts.ADD_REGIONS_MESSAGE, Buttons, Consts.UNIQUE_ADD_ID));

        }

        log.info("Пользователь: {} успешно воспользовался командой: {}", userChatId, ADD_COMMAND);

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
            List<String> userNotifiesListSplitted = List.of(userNotifiesList.split(";"));
            List<List<String>> Buttons = new ArrayList<>();
            List<String> all_btn = new ArrayList<>();
            all_btn.add(Consts.ALL_REGIONS_ID);
            Buttons.add(all_btn);
            for (Region region : regionRepository.findAll()) {
                if (userNotifiesListSplitted.contains(String.valueOf(region.getId()))) {
                    List<String> btn = new ArrayList<>();
                    btn.add(region.getRegionName());
                    Buttons.add(btn);
                }
            }
            executeSendMessage(BotMessages.createAnswer(userChatId, Consts.REGION_REMOVING_MESSAGE, Buttons, Consts.UNIQUE_REMOVE_ID));
        }

        log.info("Пользователь: {} успешно воспользовался командой: {}", userChatId, REMOVE_COMMAND);

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
            List<String> userNotifiesListSplitted = List.of(userNotifiesList.split(";"));
            for (String id : userNotifiesListSplitted)
                regionNames += "    · " + regionRepository.findById(Long.parseLong(id)).get().getRegionName() + "\n";

            executeSendMessage(BotMessages.createAnswer(userChatId, Consts.REGION_LIST_MESSAGE
                    .replace("{regionName}", regionNames)));

        }

        log.info("Пользователь: {} успешно воспользовался командой: {}", userChatId, SUBS_COMMAND);

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
        log.info("Зарегистрирован новый пользователь. USERID: {}, FIRSTNAME: {}", userChatId, firstName);

    }

    /*
        "Запускатор" отправки сообщений пользователю
    */
    private void executeSendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения пользователю: " + e);
        }
    }

    /*
        "Запускатор" редактирования кнопок у сообщений бота
    */
    private void executeEditMarkup(EditMessageReplyMarkup editMessageReplyMarkup) {
        try {
            execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            log.error("Ошибка редактирования клавиатуры: " + e);
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
                .replace("{time}", dateFormatter.format(date));

        log.info("Оповещение пользователей запущено ...");
        int notifiedUsers = 0;
        long startTime = System.currentTimeMillis();
        for (User user : userRepository.findAll()) {
            List<String> userNotifiesList = List.of(user.getNotifiesList().split(";"));
            if (userNotifiesList.contains(Consts.ALL_ID)) {
                executeSendMessage(BotMessages.createAnswer(user.getId(), message));
                notifiedUsers++;
            } else if (userNotifiesList.contains(String.valueOf(region.getId()))) {
                executeSendMessage(BotMessages.createAnswer(user.getId(), message));
                notifiedUsers++;
            }
        }
        long endTime = System.currentTimeMillis();
        log.info("Оповещение пользователей завершено за: " + (endTime - startTime) + " мс. Уведомлено: " + notifiedUsers + " пользователей.");

    }

    /*
        Тут скрипт, который раз в N-ое время получает обновлённые данные о тревогах.
    */
    @Scheduled(fixedDelayString = "${fixedDelay.in.milliseconds}")
    public void getAlertData() {
        try {
            AlertData alertData = alertsController.getAlertData();
            log.info("Информация о тревогах получена.");
            updateInfoAlerts(alertData);
        } catch (URISyntaxException e) {
            log.error("Ошибка создания ENDPOINT-URL: " + e);
        } catch (InterruptedException | IOException e) {
            log.error("Ошибка отправки запроса на сервера/преобразования json в object "  + e);
        }
    }

    @Scheduled(fixedDelay = 60000)
    public void sendAds() {

        log.info("Рассылка заготовленной информации ...");
        int notifiedUsers = 0;
        for (Ad ad : adRepository.findAll()) {
            String recipients = ad.getRecipients();
            String adMessage = ad.getAd();
            if (recipients.equals("all")) {
                for (User user : userRepository.findAll()) {
                    executeSendMessage(BotMessages.createAnswer(user.getId(), adMessage));
                    notifiedUsers++;
                }
            } else {
                String[] recipientsArray = recipients.split(";");
                for (String recipient : recipientsArray) {
                    executeSendMessage(BotMessages.createAnswer((Long.parseLong(recipient)), adMessage));
                    notifiedUsers++;
                }
            }
            log.info("Заготовленной информацией уведомлено: {} пользователей.", notifiedUsers);
            log.info("Удаление заготовленной информации ...");
            adRepository.delete(ad);
            log.info("Заготовленная информация удалена.");
            break;
        }

    }

}