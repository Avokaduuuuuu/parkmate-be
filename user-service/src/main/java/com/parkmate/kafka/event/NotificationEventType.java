package com.parkmate.kafka.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationEventType {

    // Account verification events
    MEMBER_VERIFICATION("MEMBER_VERIFICATION", "Member account verification"),
    PARTNER_VERIFICATION("PARTNER_VERIFICATION", "Partner account verification"),

    // Reservation events (future use)
    RESERVATION_CREATED("RESERVATION_CREATED", "Reservation created"),
    RESERVATION_ACTIVATED("RESERVATION_ACTIVATED", "Reservation activated"),
    RESERVATION_CANCELLED("RESERVATION_CANCELLED", "Reservation cancelled"),
    RESERVATION_COMPLETED("RESERVATION_COMPLETED", "Reservation completed"),

    // Approval/Rejection events
    PARTNER_APPROVED("PARTNER_APPROVED", "Partner registration approved"),
    PARTNER_REJECTED("PARTNER_REJECTED", "Partner registration rejected"),
    ;

    private final String value;
    private final String description;

}
