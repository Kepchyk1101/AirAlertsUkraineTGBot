package com.kepchyk1101.AirAlertsTGBot.service.bot;

import com.kepchyk1101.AirAlertsTGBot.model.*;
import com.kepchyk1101.AirAlertsTGBot.service.alerts.AlertsController;
import com.kepchyk1101.AirAlertsTGBot.service.alerts.data.AlertData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;

@Slf4j
@Component
public class ScheduledTasks {

    @Autowired private UserRepository userRepository;
    @Autowired private RegionRepository regionRepository;
    @Autowired private AdRepository adRepository;

    @Autowired private MessageSender messageSender;

    @Autowired private AlertsController alertsController;

    @Scheduled(fixedDelayString = "${fixedDelay.in.milliseconds}")
    public void getAlertData() {
        try {
            AlertData alertData = alertsController.getAlertData();
            log.info("Информация о тревогах получена.");
            alertsController.updateInfoAlerts(alertData);
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
                    messageSender.sendMessage(user.getId(), adMessage);
                    notifiedUsers++;
                }
            } else {
                String[] recipientsArray = recipients.split(";");
                for (String recipient : recipientsArray) {
                    messageSender.sendMessage((Long.parseLong(recipient)), adMessage);
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