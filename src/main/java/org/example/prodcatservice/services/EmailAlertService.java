package org.example.prodcatservice.services;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailAlertService {

    private static final Logger logger = LoggerFactory.getLogger(EmailAlertService.class);

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.alert.from}")
    private String fromEmail;

    @Value("${sendgrid.alert.to}")
    private String toEmail;

    public void sendLowStockAlert(String productName, Long productId, int stock) {
        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        String subject = "🚨 Low Stock Alert: " + productName;
        String body = String.format(
                "⚠️ The stock for product [%s] (ID: %d) has dropped to %d.\nPlease consider restocking soon.",
                productName, productId, stock
        );
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = getSendGridInstance();
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
            logger.info("✅ Low stock alert email sent for product {}", productId);
        } catch (IOException ex) {
            logger.error("❌ Failed to send low stock alert for product {}: {}", productId, ex.getMessage());
        }
    }

    // ✅ Exposed for testing
    protected SendGrid getSendGridInstance() {
        return new SendGrid(sendGridApiKey);
    }
}
