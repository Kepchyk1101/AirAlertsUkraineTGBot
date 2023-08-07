package com.kepchyk1101.AirAlertsTGBot.service.alerts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kepchyk1101.AirAlertsTGBot.service.alerts.data.AlertData;
import com.kepchyk1101.AirAlertsTGBot.service.Consts;

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

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient client = HttpClient.newHttpClient();

    public static AlertData getAlertData() throws URISyntaxException, IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(Consts.ENDPOINT_URL))
                .header("X-API-Key", Consts.API_KEY)
                .build();
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

        String response = httpResponse.body();

        return mapper.readValue(response, AlertData.class);

    }

}
