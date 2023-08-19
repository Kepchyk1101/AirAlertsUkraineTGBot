package com.kepchyk1101.AirAlertsTGBot.handlers;

import com.kepchyk1101.AirAlertsTGBot.model.Region;
import com.kepchyk1101.AirAlertsTGBot.model.RegionRepository;
import com.kepchyk1101.AirAlertsTGBot.model.User;
import com.kepchyk1101.AirAlertsTGBot.model.UserRepository;
import com.kepchyk1101.AirAlertsTGBot.service.bot.MessageSender;
import com.kepchyk1101.AirAlertsTGBot.utils.Consts;
import com.kepchyk1101.AirAlertsTGBot.utils.TextFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class CallbackQueryHandler implements IHandler {

    @Autowired private UserRepository userRepository;
    @Autowired private RegionRepository regionRepository;

    @Autowired private MessageSender messageSender;

    @Override
    public void handle(Update update) {

        CallbackQuery callbackQuery = update.getCallbackQuery();
        Message messageFromCallbackQuery = callbackQuery.getMessage();
        String callbackQueryData = callbackQuery.getData();
        final long userChatId = messageFromCallbackQuery.getChatId();
        final int messageId = messageFromCallbackQuery.getMessageId();

        // Нужно добавить все области пользователю в его список уведомлений
        if (callbackQueryData.equals(Consts.UNIQUE_ADD_ID + Consts.ALL_REGIONS_ID)) {

            addAllRegions(userChatId, messageId);

        // Нужно удалить все области пользователю из его списока уведомлений
        } else if (callbackQueryData.equals(Consts.UNIQUE_REMOVE_ID + Consts.ALL_REGIONS_ID)) {

            removeAllRegions(userChatId, messageId);

        // Нужно добавить область пользователю с его список уведомлений
        } else if (callbackQueryData.contains(Consts.UNIQUE_ADD_ID)) {

            addRegion(userChatId, messageId, callbackQueryData);

        // Нужно удалить область пользователю из его список уведомлений
        } else if (callbackQueryData.contains(Consts.UNIQUE_REMOVE_ID)) {

            removeRegion(userChatId, messageId, callbackQueryData);

        }

    }

    private void addAllRegions(long userChatId, int messageId) {

        User user = userRepository.findById(userChatId).get();
        if (!user.getNotifiesList().equals(Consts.ALL_ID)) {
            user.setNotifiesList(Consts.ALL_ID);
            userRepository.save(user);
            messageSender.removeAllButtons(userChatId, messageId);
            messageSender.sendMessage(userChatId, TextFormatter.format(Consts.REGION_SUCCESSFULLY_ADDED_MESSAGE, new HashMap<>() {{
                put("{regionName}", Consts.ALL_REGIONS_ID);
            }}));
            log.info("Пользователь: {} успешно добавил {} в свой список уведомлений.", userChatId, Consts.ALL_REGIONS_ID);
        } else
            messageSender.sendMessage(userChatId, Consts.REGION_ADDING_ERROR_MESSAGE);

    }

    private void removeAllRegions(long userChatId, int messageId) {

        User user = userRepository.findById(userChatId).get();
        if (!user.getNotifiesList().equals(Consts.NOTHING_ID)) {
            user.setNotifiesList(Consts.NOTHING_ID);
            userRepository.save(user);
            messageSender.removeAllButtons(userChatId, messageId);
            messageSender.sendMessage(userChatId, TextFormatter.format(Consts.REGION_SUCCESSFULLY_REMOVED_MESSAGE, new HashMap<>() {{
                put("{regionName}", Consts.ALL_REGIONS_ID);
            }}));
            log.info("Пользователь: {} успешно удалил {} со своего списка уведомлений.", userChatId, Consts.ALL_REGIONS_ID);
        } else
            messageSender.sendMessage(userChatId, TextFormatter.format(Consts.REGION_REMOVING_ERROR_MESSAGE, new HashMap<>() {{
                put("{regionName}", Consts.ALL_REGIONS_ID);
            }}));

    }

    private void addRegion(long userChatId, int messageId, String callbackQueryData) {

        for (Region region : regionRepository.findAll()) {
            String regionName = region.getRegionName();
            if (callbackQueryData.equals(Consts.UNIQUE_ADD_ID + regionName)) {

                User user = userRepository.findById(userChatId).get();
                String userNotifiesList = user.getNotifiesList();
                Region selectedRegion = regionRepository.findByregionName(regionName);
                long selectedRegionId = selectedRegion.getId();
                List<String> userNotifiesListSplitted = List.of(userNotifiesList.split(";"));

                if (userNotifiesList.equals(Consts.ALL_ID)) {

                    messageSender.sendMessage(userChatId, Consts.REGION_ADDING_ERROR_MESSAGE);

                } else if (userNotifiesList.equals(Consts.NOTHING_ID)) {

                    user.setNotifiesList(String.valueOf(selectedRegionId));
                    userRepository.save(user);
                    messageSender.sendMessage(userChatId, TextFormatter.format(Consts.REGION_SUCCESSFULLY_ADDED_MESSAGE, new HashMap<>() {{
                        put("{regionName}", regionName);
                    }}));
                    messageSender.updateAddButtons(userChatId, messageId, userRepository.findById(userChatId).get().getNotifiesList(), regionRepository.findAll());
                    log.info("Пользователь: {} успешно добавил {} в свой список уведомлений.", userChatId, regionName);

                } else if (userNotifiesListSplitted.contains(String.valueOf(selectedRegionId))) {
                    messageSender.sendMessage(userChatId, TextFormatter.format(Consts.REGION_ADD_ERROR_MESSAGE, new HashMap<>() {{
                        put("regionName", regionName);
                    }}));
                } else if (!userNotifiesListSplitted.contains(String.valueOf(selectedRegionId))) {
                    user.setNotifiesList(userNotifiesList += (";" + selectedRegionId));
                    userRepository.save(user);
                    messageSender.sendMessage(userChatId, TextFormatter.format(Consts.REGION_SUCCESSFULLY_ADDED_MESSAGE, new HashMap<>() {{
                        put("{regionName}", regionName);
                    }}));
                    messageSender.updateAddButtons(userChatId, messageId, userNotifiesList, regionRepository.findAll());
                    log.info("Пользователь: {} успешно добавил {} в свой список уведомлений.", userChatId, regionName);
                }

            }
        }

    }

    private void removeRegion(long userChatId, int messageId, String callbackQueryData) {

        User user = userRepository.findById(userChatId).get();
        String userNotifiesList = user.getNotifiesList();
        List<String> userNotifiesListSplitted = List.of(userNotifiesList.split(";"));
        Region selectedRegion = regionRepository.findByregionName(callbackQueryData.replace(Consts.UNIQUE_REMOVE_ID, ""));
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
                messageSender.removeAllButtons(userChatId, messageId);
            }

            user.setNotifiesList(userNotifiesList);
            userRepository.save(user);
            if (!userNotifiesList.equals(Consts.NOTHING_ID))
                messageSender.updateRemoveButtons(userChatId, messageId, userNotifiesList, regionRepository.findAll());
            messageSender.updateRemoveButtons(userChatId, messageId, userNotifiesList, regionRepository.findAll());
            messageSender.sendMessage(userChatId, TextFormatter.format(Consts.REGION_SUCCESSFULLY_REMOVED_MESSAGE, new HashMap<>() {{
                put("{regionName}", selectedRegion.getRegionName());
            }}));
            log.info("Пользователь: {} успешно удалил {} со своего списка уведомлений.", userChatId, selectedRegion.getRegionName());
        }

    }

}