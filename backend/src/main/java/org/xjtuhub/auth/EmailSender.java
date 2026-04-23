package org.xjtuhub.auth;

public interface EmailSender {
    void send(EmailMessage message);

    record EmailMessage(
            String to,
            String subject,
            String body
    ) {
    }
}
