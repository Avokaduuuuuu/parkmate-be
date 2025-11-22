package com.parkmate.kafka.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationEventType {

    // Account verification events
    MEMBER_VERIFICATION("MEMBER_VERIFICATION", "Member account verification"),
    PARTNER_VERIFICATION("PARTNER_VERIFICATION", "Partner account verification"),

    // Reservation events
    RESERVATION_CREATED("RESERVATION_CREATED", "Reservation created"),
    RESERVATION_ACTIVATED("RESERVATION_ACTIVATED", "Reservation activated"),
    RESERVATION_CANCELLED("RESERVATION_CANCELLED", "Reservation cancelled"),
    RESERVATION_COMPLETED("RESERVATION_COMPLETED", "Reservation completed"),
    RESERVATION_REMINDER("RESERVATION_REMINDER", "Upcoming reservation reminder"),
    RESERVATION_NO_SHOW("RESERVATION_NO_SHOW", "Reservation no-show detected"),

    // Subscription events
    SUBSCRIPTION_EXPIRING("SUBSCRIPTION_EXPIRING", "Subscription expiring soon"),
    SUBSCRIPTION_RENEWED("SUBSCRIPTION_RENEWED", "Subscription renewed successfully"),
    SUBSCRIPTION_RENEWAL_FAILED("SUBSCRIPTION_RENEWAL_FAILED", "Subscription renewal failed"),

    // Wallet/Payment events
    LOW_WALLET_BALANCE("LOW_WALLET_BALANCE", "Wallet balance insufficient"),

    // Approval/Rejection events
    PARTNER_APPROVED("PARTNER_APPROVED", "Partner registration approved"),
    PARTNER_REJECTED("PARTNER_REJECTED", "Partner registration rejected");

    private final String value;
    private final String description;

}
