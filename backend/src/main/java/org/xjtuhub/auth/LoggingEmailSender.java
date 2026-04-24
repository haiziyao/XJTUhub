package org.xjtuhub.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingEmailSender implements EmailSender {
    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public void send(EmailMessage message) {
        log.info("Queue email delivery to={}, subject={}", message.to(), message.subject());
    }
}
