package com.java.counter.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Base64;
import java.util.Map;

@Service
public class GmailApiService {

    @Value("${google.gmail.client-id}") private String clientId;
    @Value("${google.gmail.client-secret}") private String clientSecret;
    @Value("${google.gmail.refresh-token}") private String refreshToken;
    @Value("${google.gmail.sender-email}") private String senderEmail;

    private final WebClient webClient = WebClient.create();

    // 1. Hàm tự động lấy Access Token mới bằng Refresh Token
    private String getAccessToken() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("refresh_token", refreshToken);
        formData.add("grant_type", "refresh_token");

        Map<String, Object> response = webClient.post()
                .uri("https://mail.google.com/")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(Map.class)
                .block(); // Đợi lấy token đồng bộ

        return (String) response.get("access_token");
    }

    // 2. Hàm gửi mail chính thức
    public void sendEmail(String to, String subject, String htmlContent) {
        try {
            String accessToken = getAccessToken();

            // Tạo nội dung email theo chuẩn MIME thô (Raw MIME message)
            String mimeMessage =
                    "From: " + senderEmail + "\r\n" +
                            "To: " + to + "\r\n" +
                            "Subject: " + subject + "\r\n" +
                            "MIME-Version: 1.0\r\n" +
                            "Content-Type: text/html; charset=utf-8\r\n\r\n" +
                            htmlContent;

            // Mã hóa nội dung email sang dạng Base64 URL-safe đúng chuẩn Google API yêu cầu
            String encodedMessage = Base64.getUrlEncoder().encodeToString(mimeMessage.getBytes("UTF-8"));

            // Gửi dữ liệu qua cổng HTTPS (443) tới Google API
            this.webClient.post()
                    .uri("https://mail.google.com/")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("raw", encodedMessage))
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                            res -> System.out.println("Gửi mail thành công qua Gmail API! ID: " + res),
                            err -> System.err.println("Lỗi khi gọi Gmail API: " + err.getMessage())
                    );

        } catch (Exception e) {
            System.err.println("Lỗi xử lý chuỗi mã hóa email: " + e.getMessage());
        }
    }
}