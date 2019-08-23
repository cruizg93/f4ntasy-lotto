package com.devteam.fantasy.message.request;

import javax.validation.constraints.NotNull;

public class UpdatePasswordForm {

    @NotNull
    private String username;

    @NotNull
    private String password;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
