package org.xjtuhub.auth;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

public class SmtpEmailSender implements EmailSender {
    private final JavaMailSender javaMailSender;
    private final AuthProperties authProperties;

    public SmtpEmailSender(JavaMailSender javaMailSender, AuthProperties authProperties) {
        this.javaMailSender = javaMailSender;
        this.authProperties = authProperties;
    }

    @Override
    public void send(EmailMessage message) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setTo(message.to());
            helper.setSubject(message.subject());
            helper.setText(message.body(), false);

            String fromAddress = authProperties.getEmail().getFromAddress();
            String fromName = authProperties.getEmail().getFromName();
            if (fromAddress != null && !fromAddress.isBlank()) {
                if (fromName != null && !fromName.isBlank()) {
                    helper.setFrom(fromAddress, fromName);
                } else {
                    helper.setFrom(fromAddress);
                }
            }

            javaMailSender.send(mimeMessage);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to send email.", ex);
        }
    }
}
