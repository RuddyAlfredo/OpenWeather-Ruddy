package com.bootcamp.clima.security.controller;

import java.util.HashSet;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.shaded.json.parser.ParseException;
import com.bootcamp.clima.dto.Mensaje;
import com.bootcamp.clima.security.dto.JwtDto;
import com.bootcamp.clima.security.dto.LoginUsuario;
import com.bootcamp.clima.security.dto.NuevoUsuario;
import com.bootcamp.clima.security.entity.Rol;
import com.bootcamp.clima.security.entity.Usuario;
import com.bootcamp.clima.security.enums.RolNombre;
import com.bootcamp.clima.security.jwt.JwtProvider;
import com.bootcamp.clima.security.service.RolService;
import com.bootcamp.clima.security.service.UsuarioDetailsServiceImpl;
import com.bootcamp.clima.security.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@CrossOrigin
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    UsuarioDetailsServiceImpl usuarioDetailsService;
    
    @Autowired
    PasswordEncoder passwordEncoder;
    
    @Autowired
    AuthenticationManager authenticationManager;
    
    @Autowired
    UsuarioService usuarioService;
    
    @Autowired
    RolService rolService;
    
    @Autowired
    JwtProvider jwtProvider;

    @PostMapping("")
    public ResponseEntity<Mensaje> nuevo(@Valid @RequestBody NuevoUsuario nuevoUsuario, BindingResult bindingResult){
        logger.error(nuevoUsuario.toString());
        if (bindingResult.hasErrors())
            return new ResponseEntity<Mensaje>(new Mensaje ("Verifique los datos introducidos", null, null), HttpStatus.BAD_REQUEST);   

        if (usuarioService.existsByNombreUsuario(nuevoUsuario.getNombreUsuario()))
            return new ResponseEntity<Mensaje>(new Mensaje ("El nombre" + nuevoUsuario.getNombreUsuario() + " ys se encuentra registrado", null, null), HttpStatus.BAD_REQUEST); 

        if (usuarioService.existsByEmail(nuevoUsuario.getEmail()))
            return new ResponseEntity<Mensaje>(new Mensaje ("El email " + nuevoUsuario.getEmail() + " ya se encuentra registrado", null, null), HttpStatus.BAD_REQUEST);
    
        Usuario usuario = new Usuario(nuevoUsuario.getNombre(), nuevoUsuario.getNombreUsuario(), nuevoUsuario.getEmail(), passwordEncoder.encode(nuevoUsuario.getPassword()));

        Set<Rol> roles = new HashSet<>();
        roles.add(rolService.getByRolNombre(RolNombre.ROLE_USER).get());
        
        if (nuevoUsuario.getRoles().contains("admin")) {
            roles.add(rolService.getByRolNombre(RolNombre.ROLE_ADMIN).get());
        }
        usuario.setRoles(roles);
        usuarioService.save(usuario);

        return new ResponseEntity<Mensaje>(new Mensaje ("Usuario refistrado con  exito", null, null), HttpStatus.CREATED);

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginUsuario loginUsuario, BindingResult bindingResult){
        logger.error(loginUsuario.toString());
        
        if (bindingResult.hasErrors())
            return new ResponseEntity<Mensaje>(new Mensaje ("Usuario inválido", null, null), HttpStatus.UNAUTHORIZED);   

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginUsuario.getNombreUsuario(), loginUsuario.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtProvider.generateToken(authentication);
        JwtDto jwtDto = new JwtDto(jwt);
                
        return new ResponseEntity<JwtDto>(jwtDto, HttpStatus.ACCEPTED);  
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtDto> refresh(@RequestBody JwtDto jwtDto) throws ParseException, java.text.ParseException{
    
        String token = jwtProvider.refreshToken(jwtDto);
        JwtDto jwt = new JwtDto(token);
                
        return new ResponseEntity<JwtDto>(jwt, HttpStatus.OK);  
    }
}
