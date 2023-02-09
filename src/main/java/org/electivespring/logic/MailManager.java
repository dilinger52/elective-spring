package org.electivespring.logic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import org.electivespring.controller.RegisterServlet;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MailManager {

    private static final String ELECTIVE_EMAIL = "elective.org@gmail.com";
    private static final Logger logger = LogManager.getLogger(MailManager.class);

    public static void sendConfirmationLetter(String firstName, String lastName, String password, String email) throws MessagingException {
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", "smtp.gmail.com");
        properties.setProperty("mail.smtp.port", "587");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.put("mail.debug", "true");
        properties.put("mail.smtp.ssl.trust", "*");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session mailSession = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(ELECTIVE_EMAIL, "gzevhywxoyxveziw");
            }
        });

        logger.debug("Preparing message...");

        MimeMessage message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(ELECTIVE_EMAIL));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
        message.setSubject("Confirm your email!");
        message.setText("To confirm your email click on the link below:\n" +
                "http://localhost:8080/elective/mailconfirmed?first_name=" + firstName +
                "&last_name=" + lastName +
                "&password=" + password +
                "&email=" + email);
        Transport.send(message);
        logger.debug("message send");

    }
}
