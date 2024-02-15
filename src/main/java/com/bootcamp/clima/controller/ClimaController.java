
package com.bootcamp.clima.controller;

import com.bootcamp.clima.dto.Mensaje;
import com.bootcamp.clima.security.controller.AuthController;
import com.bootcamp.clima.security.jwt.JwtProvider;
import com.bootcamp.clima.service.ConsultaService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clima")
@CrossOrigin(origins = "*")
public class ClimaController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    ConsultaService consultaService;
    
    @Autowired
    JwtProvider jwtProvider;
    
    private int RATE_LIMIT = 1;

    private int TIME_DURATION = 1;
    
    private final Bucket bucket;
    
    public ClimaController(){
        Bandwidth limit = Bandwidth.classic(RATE_LIMIT, Refill.greedy(RATE_LIMIT, Duration.ofMinutes(TIME_DURATION)));
        this.bucket = Bucket.builder()
            .addLimit(limit)
            .build();
    }
    
    @Value("${api.key}")
    private String key;
    
    @GetMapping("/actual/{nombreCiudad}") 
    public ResponseEntity<Mensaje> climaActual(@PathVariable("nombreCiudad") String nombreCiudad, HttpServletRequest req) throws MalformedURLException{
        log.info("Iniciando petición por Nombre de Ciudad");
        
        if (bucket.tryConsume(1)) {
            return consultaService.climaActual(nombreCiudad, req);
        }
        
        return new ResponseEntity<Mensaje>(new Mensaje("Se ha alcanzado el límite de peticiones a la API", null, null),HttpStatus.TOO_MANY_REQUESTS);
    }
    
    
//=================================================================================================================================================    
    @GetMapping("/cinco-dias/{nombreCiudad}")
    public ResponseEntity<Mensaje> pronosticoCincoDias(@PathVariable("nombreCiudad") String nombreCiudad, HttpServletRequest req) throws MalformedURLException{
        log.info("Iniciando petición de Pronóstico de 5 Días por Nombre de Ciudad");
        
        if (bucket.tryConsume(1)) {
            return consultaService.pronosticoCincoDias(nombreCiudad, req);
        }
        return new ResponseEntity<Mensaje>(new Mensaje("Se ha alcanzado el límite de peticiones a la API", null, null),HttpStatus.TOO_MANY_REQUESTS);
    }
    
    
//=================================================================================================================================================     
    @GetMapping("/contaminacion-aire/{nombreCiudad}")
    @Cacheable(value = "consultasCache", key = "'Contaminacion_Aire_'+#nombreCiudad")
    public ResponseEntity<Mensaje> contaminacionDelAire(@PathVariable("nombreCiudad") String nombreCiudad, HttpServletRequest req) throws MalformedURLException{
        log.info("Iniciando petición de Contaminacion del Aire por Nombre de Ciudad");
        
        if (bucket.tryConsume(1)) {
            return consultaService.contaminacionDelAire(nombreCiudad, req);
        }
        return new ResponseEntity<Mensaje>(new Mensaje("Se ha alcanzado el límite de peticiones a la API", null, null),HttpStatus.TOO_MANY_REQUESTS);
    }
    
    
//=================================================================================================================================================     
    @GetMapping("/consultas")
    public ResponseEntity<Mensaje> consultasPorUsuario(HttpServletRequest req){
        
        String usuario = jwtProvider.getNombreUsuario(req);
        log.info("Buscando Consultas del Usuario:" + usuario);

        if (bucket.tryConsume(1)) {
            return consultaService.listByNombreUsuario(usuario);
        }
        return new ResponseEntity<Mensaje>(new Mensaje("Se ha alcanzado el límite de peticiones a la API", null, null),HttpStatus.TOO_MANY_REQUESTS);
    }
}