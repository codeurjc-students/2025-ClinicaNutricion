package com.jorgeleal.clinicanutricion.service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.springframework.scheduling.annotation.Async;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final SesClient ses;

    public EmailService() {
        this.ses = SesClient.builder()
            .region(Region.EU_WEST_3)
            .build();
    }

    @Async
    public void sendAppointmentConfirmation(String toAddress, String patientName,
                                            LocalDate date, LocalTime time) {
        try {
            String subject = "Confirmación de tu cita";
            String htmlBody = String.format(
                "<h1>Hola %s,</h1>"
            + "<p>Tu cita ha sido confirmada para el %s a las %s.</p>",
                patientName,
                date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                time.format(DateTimeFormatter.ofPattern("HH:mm"))
            );

            Destination destination = Destination.builder()
                .toAddresses(toAddress)
                .build();

            Content contentSubject = Content.builder()
                .data(subject)
                .build();
            Content htmlContent = Content.builder()
                .data(htmlBody)
                .build();
            Body body = Body.builder()
                .html(htmlContent)
                .build();

            Message message = Message.builder()
                .subject(contentSubject)
                .body(body)
                .build();

            SendEmailRequest request = SendEmailRequest.builder()
                .source("no-reply@clinicanutricion.es")
                .destination(destination)
                .message(message)
                .build();

            ses.sendEmail(request);
         } catch (SesException e) {
            log.error("Error enviando email de confirmación a {}: {}", toAddress, e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            log.error("Error genérico en el envío de email: ", e);
        }
    }
}
