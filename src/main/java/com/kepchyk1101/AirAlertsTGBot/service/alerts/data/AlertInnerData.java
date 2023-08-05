package com.kepchyk1101.AirAlertsTGBot.service.alerts.data;

public class AlertInnerData {

    private int id;
    private boolean alert;

    private AlertInnerData() {}

    public AlertInnerData(int id, boolean alert) {
        this.id = id;
        this.alert = alert;
    }

    public int getId() {
        return id;
    }

    public boolean isAlert() {
        return alert;
    }

}