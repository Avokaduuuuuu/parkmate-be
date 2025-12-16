package com.parkmate.email;

public interface EmailService {

    void sendPartnerVerificationEmail(String toEmail, String token, String recipientName);

    void sendMemberVerificationEmail(String toEmail, String token, String recipientName);

    void sendPasswordResetEmail(String toEmail, String resetCode, String recipientName);

}
