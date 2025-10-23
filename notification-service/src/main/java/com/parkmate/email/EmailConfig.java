package com.parkmate.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    private String smtpAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private String starttlsEnable;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.starttls.enable", starttlsEnable);
        props.put("mail.transport.protocol", "smtp");

        // Performance optimization
        props.put("mail.smtp.connectiontimeout", 5000); // 5s timeout
        props.put("mail.smtp.timeout", 5000); // 5s timeout
        props.put("mail.smtp.writetimeout", 5000); // 5s write timeout
        props.put("mail.smtp.connectionpool.max", 20); // Connection pool
        props.put("mail.smtp.connectionpool.timeout", 60000); // 1 min idle timeout
        props.put("mail.smtp.sendpartial", true); // Send partial on error

        return mailSender;
    }
}