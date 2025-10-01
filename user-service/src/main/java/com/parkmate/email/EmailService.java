package com.parkmate.email;

public interface EmailService {

    void sendVerificationEmail(String toEmail, String token, String recipientName);

    void sendApprovalEmail(String toEmail, String recipientName, String username, String tempPassword);

    void sendRejectionEmail(String toEmail, String recipientName, String reason);

}
