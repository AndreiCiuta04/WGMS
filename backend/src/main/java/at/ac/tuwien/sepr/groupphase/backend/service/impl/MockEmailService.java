package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

@Service
@ConditionalOnProperty(name = "wgms.mail.provider", havingValue = "mock", matchIfMissing = true)
public class MockEmailService implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void sendEmail(String to, String subject, String body) {
        LOGGER.info("[MOCK EMAIL] Sending email to: {}\nSubject: {}\nBody:\n{}", to, subject, body);
    }
}
