package com.example.whatsapp_message_broadcast.service;

import com.example.whatsapp_message_broadcast.dto.SendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class WhatsAppService {

    private final RestTemplate restTemplate;

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.access-token}")
    private String accessToken;

    @Value("${whatsapp.api-version}")
    private String apiVersion;

    private String buildUrl() {
        return String.format("https://graph.facebook.com/%s/%s/messages", apiVersion, phoneNumberId);
    }

    public Map<String, Object> sendTemplateMessage(SendRequest request) {

        String url = buildUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        Map<String, Object> payLoad = new HashMap<>();
        payLoad.put("messaging_product", "whatsapp");
        payLoad.put("to", request.getTo());
        payLoad.put("type", "template");

        Map<String, Object> template = new HashMap<>();
        template.put("name", request.getTemplateName());
        template.put("language", Map.of("code", request.getLanguage()));

        if (request.getParameters() != null && request.getParameters().length > 0) {
            List<Map<String, Object>> components = new ArrayList<>();
            Map<String, Object> body = new HashMap<>();
            List<Map<String, String>> params = new ArrayList<>();

            for (String p : request.getParameters()) {
                params.add(Map.of("type", "text", "text", p));
            }

            body.put("type", "body");
            body.put("parameters", params);
            components.add(body);
            template.put("components", components);
        }
        payLoad.put("template", template);

        org.springframework.http.HttpEntity<Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(
                payLoad, headers);

        return restTemplate.postForObject(url, entity, Map.class);
    }

    public List<Map<String, Object>> broadcastMessage(
            com.example.whatsapp_message_broadcast.dto.BroadcastRequest request) {
        List<Map<String, Object>> responses = new ArrayList<>();
        for (String recipient : request.getRecipient()) {
            SendRequest sendRequest = new SendRequest();
            sendRequest.setTo(recipient);
            sendRequest.setTemplateName(request.getTemplateName());
            sendRequest.setLanguage(request.getLanguage());
            sendRequest.setParameters(request.getParameters());
            try {
                responses.add(sendTemplateMessage(sendRequest));
            } catch (Exception e) {
                Map<String, Object> error = new HashMap<>();
                error.put("recipient", recipient);
                error.put("error", e.getMessage());
                responses.add(error);
            }
        }
        return responses;
    }

    // Upload media file to WhatsApp and get Media ID
    public String uploadMedia(org.springframework.web.multipart.MultipartFile file) throws Exception {
        String url = String.format("https://graph.facebook.com/%s/%s/media", apiVersion, phoneNumberId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(accessToken);

        org.springframework.util.LinkedMultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
        body.add("messaging_product", "whatsapp");
        body.add("file", new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });
        body.add("type", file.getContentType());

        org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, Object>> entity = new org.springframework.http.HttpEntity<>(
                body, headers);

        Map response = restTemplate.postForObject(url, entity, Map.class);
        return (String) response.get("id");
    }

    // Send image message via URL
    public Map<String, Object> sendImageByUrl(String to, String imageUrl, String caption) {
        String url = buildUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        Map<String, Object> payload = new HashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("to", to);
        payload.put("type", "image");

        Map<String, Object> image = new HashMap<>();
        image.put("link", imageUrl);
        if (caption != null && !caption.isEmpty()) {
            image.put("caption", caption);
        }
        payload.put("image", image);

        org.springframework.http.HttpEntity<Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(
                payload, headers);

        return restTemplate.postForObject(url, entity, Map.class);
    }

    // Send image message via Media ID
    public Map<String, Object> sendImageByMediaId(String to, String mediaId, String caption) {
        String url = buildUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        Map<String, Object> payload = new HashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("to", to);
        payload.put("type", "image");

        Map<String, Object> image = new HashMap<>();
        image.put("id", mediaId);
        if (caption != null && !caption.isEmpty()) {
            image.put("caption", caption);
        }
        payload.put("image", image);

        org.springframework.http.HttpEntity<Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(
                payload, headers);

        return restTemplate.postForObject(url, entity, Map.class);
    }

    // Broadcast image to multiple recipients
    public List<Map<String, Object>> broadcastImage(String imageUrl, String caption, List<String> recipients) {
        List<Map<String, Object>> responses = new ArrayList<>();
        for (String recipient : recipients) {
            try {
                responses.add(sendImageByUrl(recipient, imageUrl, caption));
            } catch (Exception e) {
                Map<String, Object> error = new HashMap<>();
                error.put("recipient", recipient);
                error.put("error", e.getMessage());
                responses.add(error);
            }
        }
        return responses;
    }

}
