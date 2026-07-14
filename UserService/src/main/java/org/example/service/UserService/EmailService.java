package org.example.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalTime;

@Service
public class EmailService {

    private final JavaMailSender MailSender;

    @Autowired
    public  EmailService(JavaMailSender javaMailSender){
        MailSender = javaMailSender;
    }


    public ResultOfOtp sendPasswordResetEmail(String emailTo) {
        SecureRandom secureRandom = new SecureRandom();
        String otpCode = String.valueOf(100000 + secureRandom.nextInt(900000));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailTo);
        message.setSubject("Password Reset Request");

        String emailBody = "Hello,\n\n" +
                "We received a request to reset your password. " +
                "Your One-Time Password (OTP) is: " + otpCode + "\n\n" +
                "This OTP is valid for 15 minutes. " +
                "If you did not request this password reset, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Support Team";

        message.setText(emailBody);
        message.setFrom("dhawansujal1@gmail.com");

        MailSender.send(message);
        ResultOfOtp result = new ResultOfOtp(otpCode,LocalTime.now());

        return result;
    }

    public static class ResultOfOtp{
        private final String otp;
        private final LocalTime localTime;

        public ResultOfOtp(String otp,LocalTime localTime){
            this.otp = otp;
            this.localTime = localTime;
        }

        public String getOtp() {
            return otp;
        }

        public LocalTime getLocalTime() {
            return localTime;
        }
    }
}
