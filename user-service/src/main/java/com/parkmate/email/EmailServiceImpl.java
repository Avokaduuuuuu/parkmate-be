package com.parkmate.email;

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

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void sendVerificationEmail(String toEmail, String token, String recipientName) {
        try {
            String verificationUrl = frontendUrl + "/verify-email?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Xác thực email đăng ký ParkMate");
            message.setText(buildVerificationEmailText(recipientName, verificationUrl));

            mailSender.send(message);
            log.info("Verification email sent successfully to: {}", toEmail);

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

    private String buildVerificationEmailText(String recipientName, String verificationUrl) {
        return String.format("""
                Xin chào %s,
                
                Cảm ơn bạn đã đăng ký trở thành đối tác của ParkMate!
                
                Vui lòng click vào link bên dưới để xác thực email của bạn:
                %s
                
                Link này sẽ hết hạn sau 24 giờ.
                
                Nếu bạn không thực hiện đăng ký này, vui lòng bỏ qua email này.
                
                Trân trọng,
                ParkMate Team
                """, recipientName, verificationUrl);
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