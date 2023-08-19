package com.kepchyk1101.AirAlertsTGBot.service.auth;

import com.kepchyk1101.AirAlertsTGBot.model.User;
import com.kepchyk1101.AirAlertsTGBot.model.UserRepository;
import com.kepchyk1101.AirAlertsTGBot.utils.Consts;
import com.kepchyk1101.AirAlertsTGBot.utils.Types;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Slf4j
@Component
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    // Тут регистрируем пользователя/групповой чат в бд
    public void registerUser(long userChatId, Message message) {

        String userName = message.getChat().getUserName();

        User user = new User();
        user.setId(userChatId);
        user.setUsername(userName);
        user.setNotifiesList(Consts.NOTHING_ID);

        if (message.isUserMessage())
            user.setType(Types.USER.name());
        else if (message.isGroupMessage())
            user.setType(Types.GROUP.name());
        else
            user.setType(Types.UNDEFINED.name());

        userRepository.save(user);

        log.info("Зарегистрирована новая сущность. ID: {}, USERNAME: {}, TYPE: {}", userChatId, userName, user.getType());

    }

}