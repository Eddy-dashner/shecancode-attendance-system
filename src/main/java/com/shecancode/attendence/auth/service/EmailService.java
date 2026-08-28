package com.shecancode.attendence.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Sends application emails as HTML rendered from Thymeleaf templates under
 * {@code templates/email/}. All email-building logic lives here so controllers and
 * services never assemble message bodies themselves.
 */
@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String appName;
    private final String fromAddress;

    private static final DateTimeFormatter EXPIRY_FMT =
            DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm 'UTC'").withZone(ZoneOffset.UTC);

    public EmailService(
            JavaMailSender mailSender,
            TemplateEngine templateEngine,
            @Value("${app.name}") String appName,
            @Value("${app.mail.from-address}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.appName = appName;
        this.fromAddress = fromAddress;
    }

    /** Invitation for a newly enrolled student, including their program/cohort. */
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

    /** Invitation for a newly created trainer. */
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
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email '{}' sent to [{}]", subject, toEmail);
        } catch (MessagingException e) {
            // Wrap so it is handled uniformly (502) and never leaks SMTP internals.
            throw new MailSendException("Failed to build/send email to " + toEmail, e);
        } catch (MailException e) {
            log.error("SMTP failure sending email to [{}]: {}", toEmail, e.getMessage());
            throw e;
        }
    }
}
