package com.kepchyk1101.AirAlertsTGBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "users")
public class User {

    @Id
    private Long id;
    private String firstName;
    private String notifiesList;

    public User() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNotifiesList() {
        return notifiesList;
    }

    public void setNotifiesList(String notifiesList) {
        this.notifiesList = notifiesList;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

}