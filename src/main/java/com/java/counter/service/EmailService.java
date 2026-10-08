package com.java.counter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
    }

    public void sendPasswordResetOtp(String to, String otp) {
        sendEmail(
                to,
                "Mã OTP đặt lại mật khẩu Counter",
                "Mã OTP đặt lại mật khẩu của bạn là: " + otp
                        + "\nMã có hiệu lực trong 5 phút và chỉ sử dụng được một lần."
                        + "\nNếu bạn không yêu cầu đổi mật khẩu, hãy bỏ qua email này."
        );
    }
}
