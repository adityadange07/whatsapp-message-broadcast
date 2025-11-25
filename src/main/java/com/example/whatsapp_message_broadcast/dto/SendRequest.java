package com.example.whatsapp_message_broadcast.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class SendRequest {

    @NotBlank
    private String to;

    @NotBlank
    private String templateName;

    private String language = "hi_IN";

    private String[] parameters;

}
