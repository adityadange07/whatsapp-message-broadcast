package com.example.whatsapp_message_broadcast.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import com.example.whatsapp_message_broadcast.dto.CsvContact;
import com.example.whatsapp_message_broadcast.dto.FileBroadcastRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.whatsapp_message_broadcast.dto.BroadcastRequest;
import com.example.whatsapp_message_broadcast.dto.SendRequest;
import com.example.whatsapp_message_broadcast.service.WhatsAppService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final WhatsAppService whatsAppService;
    private final com.example.whatsapp_message_broadcast.service.ContactService contactService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestBody @Valid SendRequest request) {
        return ResponseEntity.ok(whatsAppService.sendTemplateMessage(request));
    }

    @PostMapping("/broadcast")
    public ResponseEntity<List<Map<String, Object>>> broadcastMessage(
            @RequestBody @Valid BroadcastRequest request) {
        return ResponseEntity.ok(whatsAppService.broadcastMessage(request));
    }

    @PostMapping("/broadcast-from-file")
    public ResponseEntity<List<Map<String, Object>>> broadcastFromFile(
            @RequestBody @Valid FileBroadcastRequest request) {
        try {
            // Fetch contacts from saved file
            List<CsvContact> contacts = contactService
                    .getContactsFromFile(request.getFilename());

            // Extract phone numbers
            List<String> phoneNumbers = contactService.extractPhoneNumbers(contacts);

            // Create broadcast request
            BroadcastRequest broadcastRequest = new BroadcastRequest();
            broadcastRequest.setTemplateName(request.getTemplateName());
            broadcastRequest.setLanguage(request.getLanguage());
            broadcastRequest.setParameters(request.getParameters());
            broadcastRequest.setRecipient(phoneNumbers);

            // Broadcast messages
            return ResponseEntity.ok(whatsAppService.broadcastMessage(broadcastRequest));
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
