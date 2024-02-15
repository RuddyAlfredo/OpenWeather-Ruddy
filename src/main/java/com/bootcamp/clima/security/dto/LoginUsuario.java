package com.bootcamp.clima.security.dto;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LoginUsuario {

    @NotBlank
    private String nombreUsuario;
    
    @NotBlank
    private String password;
    
}
