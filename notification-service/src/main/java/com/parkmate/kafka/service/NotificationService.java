package com.parkmate.kafka.service;

import com.parkmate.kafka.event.NotificationEvent;

public interface NotificationService {

    void sendNotification(NotificationEvent notificationEvent);

    void sendEmailNotification(NotificationEvent notificationEvent);

    void sendPushNotification(NotificationEvent notificationEvent);

}
