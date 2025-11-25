package com.example.whatsapp_message_broadcast.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
public class BroadcastRequest {

    @NotBlank
    private String templateName;

    private String language = "hi_IN";

    private String[] parameters;

    private List<String> recipient;

}
