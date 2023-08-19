package com.kepchyk1101.AirAlertsTGBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity(name = "users")
public class User {

    @Id
    private Long id;
    private String username;
    private String notifiesList;
    private String type;

}