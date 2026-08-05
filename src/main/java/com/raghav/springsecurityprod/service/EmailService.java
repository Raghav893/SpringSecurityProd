    package com.raghav.springsecurityprod.service;

    import com.resend.Resend;
    import com.resend.core.exception.ResendException;
    import com.resend.services.emails.model.CreateEmailOptions;
    import com.resend.services.emails.model.CreateEmailResponse;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;

    @Service
    @Slf4j
    public class EmailService {

        @Value("${resend.api-key}")
        private String resendApiKey;

        @Value("${resend.from-address}")
        private String fromAddress;

        private final Resend resend;
        public EmailService(@Value("${resend.api-key}") String apiKey) {
            this.resend = new Resend(apiKey);
        }
        public void sendVerificationEmail(String toEmail,String verificationLink){
            try {
                String htmlBody = """
                        <p>Welcome! Please verify your email by clicking the link below:</p>
                        <p><a href="%s">Verify Email</a><p>
                        <p>This link expires in 24 hours.</p>
                        """.formatted(verificationLink);
                CreateEmailOptions params = CreateEmailOptions.builder()
                        .from(fromAddress)
                        .to(toEmail)
                        .subject("App onboarding test")
                        .html(htmlBody)
                        .build();
                CreateEmailResponse response = resend.emails().send(params);
            } catch (ResendException e) {
                throw new RuntimeException(e);
            }
        }
        public void sendForgotPasswordEmail(String toEmail,String ForgotPasswordLink){
            try {
                String htmlBody = """
                        <p>Welcome! Please verify your email by clicking the link below:</p>
                        <p><a href="%s">Forgot password link</a><p>
                        <p>This link expires in 6 minutes.</p>
                        """.formatted(ForgotPasswordLink);
                CreateEmailOptions params = CreateEmailOptions.builder()
                        .from(fromAddress)
                        .to(toEmail)
                        .subject("Forgot password auth test ")
                        .html(htmlBody)
                        .build();
                CreateEmailResponse response = resend.emails().send(params);
            } catch (ResendException e) {
                throw new RuntimeException(e);
            }
        }
    }
