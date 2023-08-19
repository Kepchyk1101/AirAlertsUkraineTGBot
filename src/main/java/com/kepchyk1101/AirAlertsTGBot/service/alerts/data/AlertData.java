package com.kepchyk1101.AirAlertsTGBot.service.alerts.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AlertData {

    private AlertInnerData[] states;
    private String last_update;

}