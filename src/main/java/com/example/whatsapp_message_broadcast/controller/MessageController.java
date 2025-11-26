package com.example.whatsapp_message_broadcast.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

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
}
