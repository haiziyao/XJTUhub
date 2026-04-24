package org.xjtuhub.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest
@Import(SmtpEmailSenderSelectionTests.MailSenderTestConfiguration.class)
class SmtpEmailSenderSelectionTests {

    @Autowired
    private EmailSender emailSender;

    @Test
    void smtpEmailSenderIsPreferredWhenJavaMailSenderExists() {
        assertThat(emailSender.getClass().getSimpleName()).isEqualTo("SmtpEmailSender");
    }

    @TestConfiguration
    static class MailSenderTestConfiguration {
        @Bean
        JavaMailSender javaMailSender() {
            return mock(JavaMailSender.class);
        }
    }
}
