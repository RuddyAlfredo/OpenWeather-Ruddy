package com.bootcamp.clima.security.dto;

import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NuevoUsuario {
    
    @NotBlank
    private String nombre;
    
    @NotBlank
    private String nombreUsuario;
    
    @Email
    private String email;
    
    @NotBlank
    private String password;

    private Set<String> roles = new HashSet<>();
    
}
