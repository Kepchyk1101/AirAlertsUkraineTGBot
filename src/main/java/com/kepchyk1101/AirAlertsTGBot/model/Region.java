package com.kepchyk1101.AirAlertsTGBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "regions")
public class Region {

    @Id
    private Long id;
    private String regionName;
    private boolean alertStatus;

}