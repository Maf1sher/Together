package org.mafisher.togetherbackend.service;


import jakarta.mail.MessagingException;
import org.mafisher.togetherbackend.email.EmailTemplateName;

public interface EmailService {

    void sendEmail(
            String to,
            String usrnmae,
            EmailTemplateName emailTemplate,
            String confirmationURL,
            String subject
    ) throws MessagingException;

}
