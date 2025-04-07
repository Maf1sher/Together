package org.mafisher.togetherbackend.service;

import jakarta.mail.BodyPart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mafisher.togetherbackend.service.impl.EmailServiceImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private EmailServiceImpl emailService;

    private MimeMessage mimeMessage;

    @Captor
    private ArgumentCaptor<Context> contextCaptor;

    @BeforeEach
    void setUp() throws Exception {
        mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendEmail_DefaultTemplate_Success() throws Exception {
        String to = "test@example.com";
        String username = "user123";
        String confirmationUrl = "http://example.com/confirm";
        String subject = "Potwierdź email";
        String expectedContent = "<html>Potwierdź</html>";

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(expectedContent);

        emailService.sendEmail(to, username, null, confirmationUrl, subject);

        verify(templateEngine).process(eq("confirm-email"), contextCaptor.capture());
        Context context = contextCaptor.getValue();
        assertEquals(username, context.getVariable("username"));
        assertEquals(confirmationUrl, context.getVariable("confirmationUrl"));

        assertEquals("together@main.com", mimeMessage.getFrom()[0].toString());
        assertEquals(to, mimeMessage.getAllRecipients()[0].toString());
        assertEquals(subject, mimeMessage.getSubject());

        assertTrue(extractHtmlContent(mimeMessage).contains(expectedContent));
        verify(mailSender).send(mimeMessage);
    }

    private String extractHtmlContent(MimeMessage message) throws Exception {
        if (message.getContent() instanceof MimeMultipart) {
            MimeMultipart multipart = (MimeMultipart) message.getContent();
            BodyPart bodyPart = multipart.getBodyPart(0);
            return (String) bodyPart.getContent();
        }
        return (String) message.getContent();
    }
}
