package com.landmarket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetEmail(String toEmail, String token) {
//        String resetLink = frontendUrl + "/reset-password?token=" + token;
    	String resetLink = frontendUrl + "/?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("LandMart - Reset Your Password");
        message.setText(
            "Hello,\n\n" +
            "You requested to reset your password on LandMart.\n\n" +
            "Click the link below to reset your password:\n" +
            resetLink + "\n\n" +
            "This link will expire in 30 minutes.\n\n" +
            "If you did not request this, please ignore this email.\n\n" +
            "Best regards,\n" +
            "LandMart Team"
        );

        mailSender.send(message);
    }
    public void sendInquiryEmail(String toEmail, String buyerEmail, String messageText) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("New Inquiry for Your Land Listing - LandMart");

        message.setText(
        	    "Hello,\n\n" +

        	    "You have received a new inquiry for your land listing on LandMart.com.\n\n" +

        	    "📌 Inquiry Details:\n" +
        	  
        	    
        	    "👤 Buyer Email : " + buyerEmail + "\n" +
        	    
        	    " MESSAGE      : " + messageText + "\n" +

   
        	    "Thank you for using LandMart.com.\n\n" +
			"Application Created by Karthik  .\n\n" +

        	    "Best regards,\n" +
        	    "LandMart Team\n" +
        	    "Your Trusted Land Marketplace"
        	);

        mailSender.send(message);
    }
}