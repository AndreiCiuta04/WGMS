package at.ac.tuwien.sepr.groupphase.backend.service;

/**
 * Sends HTML emails through the configured provider.
 * All HTML bodies must be rendered via {@link EmailTemplateRenderer} so WGMS branding stays consistent.
 */
public interface EmailService {

    void sendEmail(String to, String subject, String body);

}
