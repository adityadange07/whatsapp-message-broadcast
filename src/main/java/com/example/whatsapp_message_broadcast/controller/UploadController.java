package com.example.whatsapp_message_broadcast.controller;

import java.util.List;

import com.example.whatsapp_message_broadcast.service.GoogleSheetsService;
import com.example.whatsapp_message_broadcast.util.ExcelUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    private final GoogleSheetsService googleSheetsService;
    private final com.example.whatsapp_message_broadcast.service.FileStorageService fileStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<CsvContact>> uploadFile(
            @RequestParam("file") MultipartFile file) {
        try {
            // Step 1: Save file to storage (replaces if duplicate)
            java.io.File savedFile = fileStorageService.saveFile(file);

            // Step 2: Load file from storage
            java.io.InputStream inputStream = fileStorageService.loadFile(savedFile.getName());

            // Step 3: Parse based on file type
            String filename = savedFile.getName();
            if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                return ResponseEntity.ok(ExcelUtils.parseContacts(inputStream));
            } else {
                return ResponseEntity.ok(CsvUtils.parseContacts(inputStream));
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/google-sheet")
    public ResponseEntity<List<CsvContact>> importFromGoogleSheet(
            @RequestParam("spreadsheetId") String spreadsheetId,
            @RequestParam("range") String range) {
        try {
            return ResponseEntity.ok(googleSheetsService.getContactsFromSheet(spreadsheetId, range));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/files")
    public ResponseEntity<List<String>> listFiles() {
        try {
            return ResponseEntity.ok(fileStorageService.listFiles());
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
