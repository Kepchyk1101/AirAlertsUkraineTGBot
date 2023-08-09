package com.kepchyk1101.AirAlertsTGBot.service.alerts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kepchyk1101.AirAlertsTGBot.config.BotConfig;
import com.kepchyk1101.AirAlertsTGBot.service.alerts.data.AlertData;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/*
    Контроллер запросов к alerts.com.ua, получение данных о тревогах, не более
*/
public class AlertsController {

    private final BotConfig config;
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final HttpClient client = HttpClient.newHttpClient();

    public AlertsController(BotConfig config) {
        this.config = config;
    }

    public AlertData getAlertData() throws URISyntaxException, IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(config.getApiEndpointUrl()))
                .header("X-API-Key", config.getApiKey())
                .build();
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

        return jsonMapper.readValue(httpResponse.body(), AlertData.class);

    }

}
