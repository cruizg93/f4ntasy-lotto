package com.devteam.fantasy.message.response;

import javax.validation.constraints.NotBlank;

public class SimpleResponse {

    @NotBlank
    private String response;

    public SimpleResponse(@NotBlank String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
