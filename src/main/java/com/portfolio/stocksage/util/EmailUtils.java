package com.portfolio.stocksage.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class for sending emails
 */
@Component
@Slf4j
public class EmailUtils {

    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;
    private final boolean emailEnabled;

    @Value("${spring.mail.username:noreply@stocksage.com}")
    private String emailFrom;

    @Value("${application.name:StockSage}")
    private String applicationName;

    /**
     * Constructor with conditional dependencies to handle cases where email is not configured
     */
    public EmailUtils(
            @org.springframework.beans.factory.annotation.Autowired(required = false) JavaMailSender emailSender,
            @org.springframework.beans.factory.annotation.Autowired(required = false) TemplateEngine templateEngine,
            @Value("${application.email.enabled:false}") boolean emailEnabled) {

        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
        this.emailEnabled = emailEnabled;

        if (!emailEnabled) {
            log.warn("Email functionality is disabled. Configure application.email.enabled=true to enable.");
        } else if (emailSender == null) {
            log.warn("JavaMailSender is not available. Email sending will be disabled.");
        }
    }

    /**
     * Send an email using a Thymeleaf template
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param templateName Thymeleaf template name
     * @param variables Variables to be used in the template
     * @return CompletableFuture indicating whether the email was sent successfully
     */
    @Async
    public CompletableFuture<Boolean> sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        if (!isEmailFunctionalityAvailable()) {
            log.info("Email sending is disabled. Would have sent template email to: {} with subject: {}", to, subject);
            return CompletableFuture.completedFuture(false);
        }

        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailFrom);
            helper.setTo(to);
            helper.setSubject(subject);

            // Add application name to the variables
            variables.put("applicationName", applicationName);

            // Process the Thymeleaf template
            Context context = new Context();
            variables.forEach(context::setVariable);
            String htmlContent = templateEngine.process(templateName, context);

            helper.setText(htmlContent, true);

            emailSender.send(message);
            log.info("Email sent to: {} with subject: {}", to, subject);

            return CompletableFuture.completedFuture(true);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {} with subject: {}", to, subject, e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Send a simple text email
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param text Email text content
     * @return CompletableFuture indicating whether the email was sent successfully
     */
    @Async
    public CompletableFuture<Boolean> sendSimpleEmail(String to, String subject, String text) {
        if (!isEmailFunctionalityAvailable()) {
            log.info("Email sending is disabled. Would have sent simple email to: {} with subject: {}", to, subject);
            return CompletableFuture.completedFuture(false);
        }

        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(emailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);

            emailSender.send(message);
            log.info("Simple email sent to: {} with subject: {}", to, subject);

            return CompletableFuture.completedFuture(true);
        } catch (MessagingException e) {
            log.error("Failed to send simple email to: {} with subject: {}", to, subject, e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Send a password reset email
     *
     * @param to Recipient email address
     * @param resetToken Password reset token
     * @param resetUrl Password reset URL
     * @return CompletableFuture indicating whether the email was sent successfully
     */
    @Async
    public CompletableFuture<Boolean> sendPasswordResetEmail(String to, String resetToken, String resetUrl) {
        if (!isEmailFunctionalityAvailable()) {
            log.info("Email sending is disabled. Would have sent password reset email to: {}", to);
            return CompletableFuture.completedFuture(false);
        }

        String subject = applicationName + " - Password Reset";

        Context context = new Context();
        context.setVariable("resetToken", resetToken);
        context.setVariable("resetUrl", resetUrl);
        context.setVariable("applicationName", applicationName);

        String htmlContent = templateEngine.process("email/password-reset", context);

        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            emailSender.send(message);
            log.info("Password reset email sent to: {}", to);

            return CompletableFuture.completedFuture(true);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", to, e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Send a welcome email to a new user
     *
     * @param to Recipient email address
     * @param fullName User's full name
     * @param username User's username
     * @return CompletableFuture indicating whether the email was sent successfully
     */
    @Async
    public CompletableFuture<Boolean> sendWelcomeEmail(String to, String fullName, String username) {
        if (!isEmailFunctionalityAvailable()) {
            log.info("Email sending is disabled. Would have sent welcome email to: {}", to);
            return CompletableFuture.completedFuture(false);
        }

        String subject = "Welcome to " + applicationName;

        Context context = new Context();
        context.setVariable("fullName", fullName);
        context.setVariable("username", username);
        context.setVariable("applicationName", applicationName);

        String htmlContent = templateEngine.process("email/welcome", context);

        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            emailSender.send(message);
            log.info("Welcome email sent to: {}", to);

            return CompletableFuture.completedFuture(true);
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to: {}", to, e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Check if email functionality is available
     */
    private boolean isEmailFunctionalityAvailable() {
        return emailEnabled && emailSender != null && templateEngine != null;
    }
}