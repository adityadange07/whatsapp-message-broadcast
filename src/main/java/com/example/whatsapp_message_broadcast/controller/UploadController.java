package com.example.whatsapp_message_broadcast.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.whatsapp_message_broadcast.dto.CsvContact;
import com.example.whatsapp_message_broadcast.util.CsvUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class UploadController {

    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<CsvContact>> uploadFile(
            @RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity
                    .ok(CsvUtils.parseContacts(file.getInputStream()));
        } catch (java.io.IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
