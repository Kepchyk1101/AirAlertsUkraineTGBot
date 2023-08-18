package com.kepchyk1101.AirAlertsTGBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Getter
@Entity(name = "ads")
public class Ad {

    @Id
    private Long id;
    private String recipients;
    private String ad;

}
