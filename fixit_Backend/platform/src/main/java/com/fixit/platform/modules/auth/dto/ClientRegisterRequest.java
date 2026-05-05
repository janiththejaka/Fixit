package com.fixit.platform.modules.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientRegisterRequest {

    public String email;
    public String password;
}
