package com.kepchyk1101.AirAlertsTGBot.service.alerts;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kepchyk1101.AirAlertsTGBot.config.BotConfig;
import com.kepchyk1101.AirAlertsTGBot.model.Region;
import com.kepchyk1101.AirAlertsTGBot.model.RegionRepository;
import com.kepchyk1101.AirAlertsTGBot.model.User;
import com.kepchyk1101.AirAlertsTGBot.model.UserRepository;
import com.kepchyk1101.AirAlertsTGBot.service.alerts.data.AlertData;
import com.kepchyk1101.AirAlertsTGBot.service.alerts.data.AlertInnerData;
import com.kepchyk1101.AirAlertsTGBot.service.bot.MessageSender;
import com.kepchyk1101.AirAlertsTGBot.utils.Consts;
import com.kepchyk1101.AirAlertsTGBot.utils.TextFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class AlertsController {

    private final BotConfig config;
    private static final ObjectMapper jsonMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final HttpClient client = HttpClient.newHttpClient();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired private UserRepository userRepository;
    @Autowired private RegionRepository regionRepository;

    @Autowired private MessageSender messageSender;

    public AlertsController(BotConfig config) {
        this.config = config;
    }

    // Тут инициализируется запрос к alerts.com.ua
    public AlertData getAlertData() throws URISyntaxException, IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(config.getApiEndpointUrl()))
                .header("X-API-Key", config.getApiKey())
                .build();
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

        return jsonMapper.readValue(httpResponse.body(), AlertData.class);

    }

    // Тут обновляем данные о тревогах, если они вообще изменились
    public void updateInfoAlerts(AlertData alertData) {

        for (AlertInnerData aid : alertData.getStates()) {
            int id = aid.getId();
            boolean newAlertStatus = aid.isAlert();
            Region region = regionRepository.findById( (long) id).get();
            boolean oldAlertStatus = region.isAlertStatus();
            if (oldAlertStatus != newAlertStatus) {
                region.setAlertStatus(newAlertStatus);
                region.setChangedAt(aid.getChanged());
                regionRepository.save(region);
                alertUsers(region, newAlertStatus);
            }
        }

    }

    // Тут оповещаем пользователей о смене информации о тревоге
    public void alertUsers(Region region, boolean alertStatus) {

        LocalDateTime time = LocalDateTime.parse(region.getChangedAt(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String message = alertStatus ? Consts.START_ALERT : Consts.CANCEL_ALERT;

        message = TextFormatter.format(message, new HashMap<>() {{
            put("{regionName}", region.getRegionName());
            put("{time}", dateFormatter.format(time));
        }});

        log.info("Оповещение пользователей запущено ...");
        int notifiedUsers = 0;
        long startTime = System.currentTimeMillis();
        for (User user : userRepository.findAll()) {
            List<String> userNotifiesList = List.of(user.getNotifiesList().split(";"));
            if (userNotifiesList.contains(Consts.ALL_ID)) {
                messageSender.sendMessage(user.getId(), message);
                notifiedUsers++;
            } else if (userNotifiesList.contains(String.valueOf(region.getId()))) {
                messageSender.sendMessage(user.getId(), message);
                notifiedUsers++;
            }
        }
        long endTime = System.currentTimeMillis();
        log.info("Оповещение пользователей завершено за: " + (endTime - startTime) + " мс. Уведомлено: " + notifiedUsers + " пользователей.");

    }

}