package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "wgms.mail.provider", havingValue = "resend")
public class ResendEmailService implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${wgms.mail.apiKey:}")
    private String apiKey;

    @Value("${wgms.mail.from:WGMS <noreply@shr8.sbs>}")
    private String fromAddress;

    @Override
    public void sendEmail(String to, String subject, String body) {
        LOGGER.info("Sending email via Resend provider to: {}, subject: {}", to, subject);

        String url = "https://api.resend.com/emails";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> payload = new HashMap<>();
        payload.put("from", fromAddress);
        payload.put("to", to);
        payload.put("subject", subject);
        payload.put("html", body);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            restTemplate.postForObject(url, request, String.class);
            LOGGER.info("Email sent successfully via Resend to {}", to);
        } catch (Exception e) {
            LOGGER.error("Failed to send email via Resend to {}: {}", to, e.getMessage(), e);
        }
    }
}
