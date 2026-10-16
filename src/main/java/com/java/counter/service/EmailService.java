package com.java.counter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;
import com.sendgrid.helpers.mail.objects.*;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${sendgrid.api-key}")
    private String apiKey;

//    public void sendEmail(String to, String subject, String text) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom(fromAddress);
//        message.setTo(to);
//        message.setSubject(subject);
//        message.setText(text);
//        javaMailSender.send(message);
//    }

    public void sendEmail(String toEmail, String subject, String content) throws IOException {
        Email from = new Email("tempab102@gmail.com"); // Email đã xác thực trên SendGrid
        Email to = new Email(toEmail);
        Content emailContent = new Content("text/plain", content);
        Mail mail = new Mail(from, subject, to, emailContent);

        // Lấy API Key từ Environment Variable cấu hình trên Render
        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println("Status Code: " + response.getStatusCode());
        } catch (IOException ex) {
            throw ex;
        }
    }

    public void sendPasswordResetOtp(String to, String otp) throws IOException {
        sendEmail(
                to,
                "Mã OTP đặt lại mật khẩu Counter",
                "Mã OTP đặt lại mật khẩu của bạn là: " + otp
                        + "\nMã có hiệu lực trong 5 phút và chỉ sử dụng được một lần."
                        + "\nNếu bạn không yêu cầu đổi mật khẩu, hãy bỏ qua email này."
        );
    }
}
