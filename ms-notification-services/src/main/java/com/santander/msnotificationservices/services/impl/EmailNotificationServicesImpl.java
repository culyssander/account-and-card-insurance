package com.santander.msnotificationservices.services.impl;

import com.santander.msnotificationservices.dto.NotificationDto;
import com.santander.msnotificationservices.services.NotificationServices;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationServicesImpl implements NotificationServices {

    @Override
    public void sendNotification(NotificationDto notificationDto, String templateName, String context) {
        System.out.println("SEND EMAIL NOTIFICATION: " + notificationDto + " TEMPLATE: " + templateName + " CONTEXT: " + context);
    }

}
