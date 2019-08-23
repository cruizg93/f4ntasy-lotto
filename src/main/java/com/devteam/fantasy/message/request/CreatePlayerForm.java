package com.devteam.fantasy.message.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

public class CreatePlayerForm {

    @NotBlank
    @Size(min = 1, max = 50)
    private String name;

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;


    @NotBlank
    @Size(min = 1, max = 40)
    private String password;

    @NotBlank
    private String utype;

    @NotBlank
    private String mtype;

    @NotBlank
    private String dtype;

    @NotBlank
    private String dparam1;

    @NotBlank
    private String dparam2;

    @NotBlank
    private String ctype;

    @NotBlank
    private String cparam1;

    @NotBlank
    private String cparam2;


    private String cparam3;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDtype() {
        return dtype;
    }

    public void setDtype(String dtype) {
        this.dtype = dtype;
    }

    public String getDparam1() {
        return dparam1;
    }

    public void setDparam1(String dparam1) {
        this.dparam1 = dparam1;
    }

    public String getDparam2() {
        return dparam2;
    }

    public void setDparam2(String dparam2) {
        this.dparam2 = dparam2;
    }

    public String getCtype() {
        return ctype;
    }

    public void setCtype(String ctype) {
        this.ctype = ctype;
    }

    public String getCparam1() {
        return cparam1;
    }

    public void setCparam1(String cparam1) {
        this.cparam1 = cparam1;
    }

    public String getCparam2() {
        return cparam2;
    }

    public void setCparam2(String cparam2) {
        this.cparam2 = cparam2;
    }

    public String getCparam3() {
        return cparam3;
    }

    public void setCparam3(String cparam3) {
        this.cparam3 = cparam3;
    }

    public String getMtype() {
        return mtype;
    }

    public void setMtype(String mtype) {
        this.mtype = mtype;
    }

    public String getUtype() {
        return utype;
    }

    public void setUtype(String utype) {
        this.utype = utype;
    }

    @Override
    public String toString() {
        return "CreatePlayerForm{" +
                "name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", utype='" + utype + '\'' +
                ", mtype='" + mtype + '\'' +
                ", dtype='" + dtype + '\'' +
                ", dparam1='" + dparam1 + '\'' +
                ", dparam2='" + dparam2 + '\'' +
                ", ctype='" + ctype + '\'' +
                ", cparam1='" + cparam1 + '\'' +
                ", cparam2='" + cparam2 + '\'' +
                ", cparam3='" + cparam3 + '\'' +
                '}';
    }
}
