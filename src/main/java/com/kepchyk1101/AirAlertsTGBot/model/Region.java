package com.kepchyk1101.AirAlertsTGBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "regions")
public class Region {

    @Id
    private Long id;
    private String regionName;
    private boolean alertStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public boolean isAlertStatus() {
        return alertStatus;
    }

    public void setAlertStatus(boolean alertStatus) {
        this.alertStatus = alertStatus;
    }

}