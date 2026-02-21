package com.abhijit.gymbilling.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.whatsapp.from}")
    private String fromNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public void sendMessage(String to, String messageText) {

        try {

            Message message = Message.creator(
                    new PhoneNumber("whatsapp:" + to),
                    new PhoneNumber(fromNumber),
                    messageText
            ).create();

            System.out.println("Message Sent Successfully!");
            System.out.println("Message SID: " + message.getSid());

        } catch (Exception e) {
            System.out.println("Error Sending WhatsApp Message:");
            e.printStackTrace();
        }
    }
}