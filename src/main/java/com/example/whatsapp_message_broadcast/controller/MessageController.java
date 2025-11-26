package com.example.whatsapp_message_broadcast.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import com.example.whatsapp_message_broadcast.dto.CsvContact;
import com.example.whatsapp_message_broadcast.dto.FileBroadcastRequest;
import com.example.whatsapp_message_broadcast.dto.ImageBroadcastRequest;
import com.example.whatsapp_message_broadcast.dto.ImageMessageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping("/send-image")
    public ResponseEntity<Map<String, Object>> sendImage(
            @RequestBody @Valid ImageMessageRequest request) {
        try {
            return ResponseEntity.ok(whatsAppService.sendImageByUrl(
                    request.getTo(), request.getImageUrl(), request.getCaption()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/upload-and-send-image")
    public ResponseEntity<Map<String, Object>> uploadAndSendImage(
            @RequestParam("to") String to,
            @RequestParam(value = "caption", required = false) String caption,
            @RequestParam("file") MultipartFile file) {
        try {
            // Upload image to WhatsApp and get Media ID
            String mediaId = whatsAppService.uploadMedia(file);

            // Send image using Media ID
            return ResponseEntity.ok(whatsAppService.sendImageByMediaId(to, mediaId, caption));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/broadcast-image")
    public ResponseEntity<List<Map<String, Object>>> broadcastImage(
            @RequestBody @Valid ImageBroadcastRequest request) {
        try {
            List<String> recipients = request.getRecipient();
            if (recipients == null || recipients.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok(whatsAppService.broadcastImage(
                    request.getImageUrl(), request.getCaption(), recipients));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/broadcast-image-from-file")
    public ResponseEntity<List<Map<String, Object>>> broadcastImageFromFile(
            @RequestParam("filename") String filename,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            @RequestParam(value = "caption", required = false) String caption,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        try {
            // Get contacts from saved file
            List<CsvContact> contacts = contactService.getContactsFromFile(filename);
            List<String> phoneNumbers = contactService.extractPhoneNumbers(contacts);

            // Determine image source
            String finalImageUrl = imageUrl;
            if (imageFile != null && !imageFile.isEmpty()) {
                // Upload image and use Media ID
                String mediaId = whatsAppService.uploadMedia(imageFile);

                // Send to each recipient using Media ID
                List<Map<String, Object>> responses = new java.util.ArrayList<>();
                for (String phone : phoneNumbers) {
                    try {
                        responses.add(whatsAppService.sendImageByMediaId(phone, mediaId, caption));
                    } catch (Exception e) {
                        Map<String, Object> error = new java.util.HashMap<>();
                        error.put("recipient", phone);
                        error.put("error", e.getMessage());
                        responses.add(error);
                    }
                }
                return ResponseEntity.ok(responses);
            } else if (finalImageUrl != null && !finalImageUrl.isEmpty()) {
                // Use URL
                return ResponseEntity.ok(whatsAppService.broadcastImage(finalImageUrl, caption, phoneNumbers));
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
