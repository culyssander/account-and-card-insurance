package com.santander.msnotificationservices.services;

import com.santander.msnotificationservices.dto.NotificationDto;

public interface NotificationServices {
    void sendNotification(NotificationDto notificationDto, String templateName, String context);
}
