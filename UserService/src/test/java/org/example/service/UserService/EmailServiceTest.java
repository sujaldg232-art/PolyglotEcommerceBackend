package org.example.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emailService = new EmailService(mailSender);
    }

    @Test
    void testSendPasswordResetEmailSendsCorrectMessage() {
        String emailTo = "user@example.com";

        EmailService.ResultOfOtp result = emailService.sendPasswordResetEmail(emailTo);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertNotNull(sentMessage);
        assertEquals(emailTo, Objects.requireNonNull(sentMessage.getTo())[0]);
        assertEquals("Password Reset Request", sentMessage.getSubject());
        assertEquals("dhawansujal1@gmail.com", sentMessage.getFrom());
        assertTrue(Objects.requireNonNull(sentMessage.getText()).contains(result.getOtp()));
    }

    @Test
    void testSendPasswordResetEmailReturnsValidResult() {
        String emailTo = "user@example.com";
        LocalTime beforeCall = LocalTime.now().minusSeconds(1);

        EmailService.ResultOfOtp result = emailService.sendPasswordResetEmail(emailTo);

        assertNotNull(result);
        assertNotNull(result.getOtp());
        assertEquals(6, result.getOtp().length());
        assertTrue(Integer.parseInt(result.getOtp()) >= 100000);
        assertTrue(Integer.parseInt(result.getOtp()) <= 999999);
        assertTrue(result.getLocalTime().isAfter(beforeCall));
    }
}