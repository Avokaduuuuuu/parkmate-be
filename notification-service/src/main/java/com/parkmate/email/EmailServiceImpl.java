package com.parkmate.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendPartnerVerificationEmail(String toEmail, String token, String recipientName) {
        try {
            sendEmail(toEmail, "ParkMate Partner Registration Verification",
                    buildVerificationEmailText(recipientName, token));
            log.info("Partner verification email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Override
    public void sendMemberVerificationEmail(String toEmail, String token, String recipientName) {
        try {
            sendEmail(toEmail, "ParkMate Member Registration Verification",
                    buildMemberVerificationEmailText(recipientName, token));
            log.info("Member verification email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    private void sendEmail(String toEmail, String subject, String text) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(text, false);

        mailSender.send(message);
    }


    // Helper methods để build email content
    private String buildMemberVerificationEmailText(String recipientName, String verificationToken) {
        return String.format("""
                Hi %s,
                
                Here is your ParkMate verification code:
                %s
                
                ParkMate Team
                """, recipientName, verificationToken);
    }


    private String buildVerificationEmailText(String recipientName, String verificationToken) {
        return String.format("""
                Hi %s,
                
                Thank you for being ParkMate Partner
                
                Here is your verification code:
                %s
                
                ParkMate Team
                """, recipientName, verificationToken);
    }

}