package com.shecancode.attendence.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.shecancode.attendence.registration.Exception.EmailDeliveryException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


@Slf4j
@Service
public class EmailService {

    private static final URI BREVO_ENDPOINT = URI.create("https://api.brevo.com/v3/smtp/email");

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final TemplateEngine templateEngine;
    private final String appName;
    private final String fromAddress;
    private final String fromName;
    private final String apiKey;

    private static final DateTimeFormatter EXPIRY_FMT =
            DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm 'UTC'").withZone(ZoneOffset.UTC);

    public EmailService(
            ObjectMapper objectMapper,
            TemplateEngine templateEngine,
            @Value("${app.name}") String appName,
            @Value("${app.mail.from-address}") String fromAddress,
            @Value("${app.mail.from-name}") String fromName,
            @Value("${app.mail.brevo-api-key}") String apiKey
    ) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = objectMapper;
        this.templateEngine = templateEngine;
        this.appName = appName;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
        this.apiKey = apiKey;
    }

    public void sendStudentInvitation(String toEmail, String programName, String cohortNumber,
                                      String activationUrl, Instant expiresAt) {
        Context ctx = new Context();
        ctx.setVariable("appName", appName);
        ctx.setVariable("email", toEmail);
        ctx.setVariable("programName", programName);
        ctx.setVariable("cohortNumber", cohortNumber);
        ctx.setVariable("activationUrl", activationUrl);
        ctx.setVariable("expiresAt", EXPIRY_FMT.format(expiresAt));

        String html = templateEngine.process("email/student-invitation", ctx);
        send(toEmail, "Activate your " + appName + " student account", html);
    }

    public void sendTrainerInvitation(String toEmail, String fullName,
                                      String activationUrl, Instant expiresAt) {
        Context ctx = new Context();
        ctx.setVariable("appName", appName);
        ctx.setVariable("email", toEmail);
        ctx.setVariable("fullName", fullName);
        ctx.setVariable("activationUrl", activationUrl);
        ctx.setVariable("expiresAt", EXPIRY_FMT.format(expiresAt));

        String html = templateEngine.process("email/trainer-invitation", ctx);
        send(toEmail, "Activate your " + appName + " trainer account", html);
    }

    private void send(String toEmail, String subject, String htmlBody) {
        ObjectNode payload = objectMapper.createObjectNode();
        ObjectNode sender = payload.putObject("sender");
        sender.put("email", fromAddress);
        sender.put("name", fromName);
        payload.putArray("to").addObject().put("email", toEmail);
        payload.put("subject", subject);
        payload.put("htmlContent", htmlBody);

        HttpRequest request;
        try {
            request = HttpRequest.newBuilder(BREVO_ENDPOINT)
                    .timeout(Duration.ofSeconds(10))
                    .header("api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();
        } catch (IOException e) {
            throw new EmailDeliveryException("Failed to build email payload for " + toEmail, e);
        }

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Email '{}' sent to [{}]", subject, toEmail);
                return;
            }
            log.error("Brevo API rejected email to [{}]: HTTP {} - {}",
                    toEmail, response.statusCode(), response.body());
            throw new EmailDeliveryException(
                    "Brevo API returned HTTP " + response.statusCode() + " for " + toEmail);
        } catch (IOException e) {
            log.error("Failed to reach Brevo API sending email to [{}]: {}", toEmail, e.getMessage());
            throw new EmailDeliveryException("Could not reach Brevo API for " + toEmail, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EmailDeliveryException("Interrupted while sending email to " + toEmail, e);
        }
    }
}
