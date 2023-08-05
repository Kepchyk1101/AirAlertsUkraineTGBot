package com.kepchyk1101.AirAlertsTGBot.service.alerts.data;

public class AlertData {

    private AlertInnerData[] states;
    private String last_update;

    public AlertData() {}

    public AlertData(AlertInnerData[] states, String last_update) {
        this.states = states;
        this.last_update = last_update;
    }

    public AlertInnerData[] getStates() {
        return states;
    }

    public String getLast_update() {
        return last_update;
    }

}