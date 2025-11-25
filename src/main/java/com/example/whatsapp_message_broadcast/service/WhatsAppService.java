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
        payLoad.put("messaging_product","whatsapp");
        payLoad.put("to", request.getTo());
        payLoad.put("type", "template");

        Map<String, Object> template = new HashMap<>();
        template.put("name", request.getTemplateName());
        template.put("language", Map.of("code", request.getLanguage()));

        if (request.getParameters() != null && request.getParameters().length > 0) {
            List<Map<String,Object>> components = new ArrayList<>();
            Map<String,Object> body = new HashMap<>();
            List<Map<String,String>> params = new ArrayList<>();

            for (String p : request.getParameters()) {
                params.add(Map.of("type","text","text",p));
            }

            body.put("type","body");
            body.put("parameters",params);
            components.add(body);
            template.put("components", components);
        }

        return template;
    }

}
