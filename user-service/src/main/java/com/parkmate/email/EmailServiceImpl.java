package com.parkmate.email;

import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import com.parkmate.partnerRegistration.PartnerRegistration;
import com.parkmate.partnerRegistration.PartnerRegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final PartnerRegistrationRepository partnerRegistrationRepository;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void sendPartnerVerificationEmail(String toEmail, String token) {
        try {

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Xác thực email đăng ký ParkMate");
            PartnerRegistration partnerRegistration = partnerRegistrationRepository.findByContactPersonEmail(toEmail)
                    .orElseThrow(() -> new AppException(ErrorCode.PARTNER_REGISTRATION_NOT_FOUND));

            message.setText(buildVerificationEmailText(partnerRegistration.getContactPersonName(), token));

            mailSender.send(message);
            log.info("Partner verification email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Override
    public void sendMemberVerificationEmail(String toEmail, String token, String recipientName) {
        try {

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Xác thực email đăng ký ParkMate");
            message.setText(buildMemberVerificationEmailText(recipientName, token));

            mailSender.send(message);
            log.info("Member verification email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Override
    public void sendApprovalEmail(String toEmail, String recipientName,
                                  String username, String tempPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Đơn đăng ký đối tác đã được phê duyệt - ParkMate");
            message.setText(buildApprovalEmailText(recipientName, username, tempPassword));

            mailSender.send(message);
            log.info("Approval email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send approval email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send approval email", e);
        }
    }

    @Override
    public void sendRejectionEmail(String toEmail, String recipientName, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Đơn đăng ký đối tác cần chỉnh sửa - ParkMate");
            message.setText(buildRejectionEmailText(recipientName, reason));

            mailSender.send(message);
            log.info("Rejection email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send rejection email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send rejection email", e);
        }
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

    private String buildApprovalEmailText(String recipientName, String username, String tempPassword) {
        return String.format("""
                Xin chào %s,
                
                Chúc mừng! Đơn đăng ký đối tác của bạn đã được phê duyệt.
                
                Thông tin đăng nhập:
                - Username: %s
                - Mật khẩu tạm thời: %s
                
                Vui lòng đăng nhập và đổi mật khẩu ngay lập tức để bảo mật tài khoản.
                
                Link đăng nhập: %s/login
                
                Trân trọng,
                ParkMate Team
                """, recipientName, username, tempPassword, frontendUrl);
    }

    private String buildRejectionEmailText(String recipientName, String reason) {
        return String.format("""
                Xin chào %s,
                
                Đơn đăng ký đối tác của bạn cần được chỉnh sửa.
                
                Lý do:
                %s
                
                Vui lòng đăng nhập và cập nhật thông tin theo yêu cầu.
                
                Link đăng nhập: %s/login
                
                Nếu có thắc mắc, vui lòng liên hệ support@parkmate.com
                
                Trân trọng,
                ParkMate Team
                """, recipientName, reason, frontendUrl);
    }
}