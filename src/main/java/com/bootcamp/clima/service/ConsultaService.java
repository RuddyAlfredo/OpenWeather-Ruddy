package com.bootcamp.clima.service;

import com.bootcamp.clima.controller.ClimaController;
import com.bootcamp.clima.dto.Mensaje;
import com.bootcamp.clima.entity.Consulta;
import com.bootcamp.clima.repository.ConsultaRepository;
import com.bootcamp.clima.security.controller.AuthController;
import com.bootcamp.clima.security.enums.TipoConsulta;
import com.bootcamp.clima.security.jwt.JwtProvider;
import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jose.shaded.json.JSONObject;
import com.nimbusds.jose.shaded.json.parser.JSONParser;
import com.nimbusds.jose.shaded.json.parser.ParseException;
import jakarta.persistence.Tuple;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

@Service
@Transactional
public class ConsultaService {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired //Inyectamos el repository para accesar a sus metodos
    ConsultaRepository consultaRepository;

    @Autowired
    JwtProvider jwtProvider;

    @Value("${api.key}")
    private String key;

// ENDPOINT 1 - CLIMA ACTUAL ===============================================================================================================================================
    @Cacheable(value = "consultasCache", key = "'Clima_Actual_'+#nombreCiudad")
    public ResponseEntity<Mensaje> climaActual(@PathVariable("nombreCiudad") String nombreCiudad, HttpServletRequest req) throws MalformedURLException {
        JSONObject jsonCoord = getCoordanadasPorNombreDeCiudad(nombreCiudad); //Obteber lat y on por el nombre de la ciudad
        String usuario = jwtProvider.getNombreUsuario(req);

        double lon = (double) jsonCoord.get("lon");
        double lat = (double) jsonCoord.get("lat");

        log.info("Consultando Clima Actual de " + nombreCiudad + "...");
        URL url = new URL("https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + key + "&lang=es");
        log.info("Endpoint visitado: https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + key);

        String res = connection(url);// Resultado de consulta por lon y lat

        try {
            var jsonRes = parsearJSON(res);

            log.warn("Resultado JSON del Clima Actual: " + jsonRes);

            LocalDateTime fechaHora = LocalDateTime.now(); // Fecha y hora de la consulta
            Consulta consulta = new Consulta();
            consulta.setFecha(fechaHora);
            consulta.setTipoConsulta(TipoConsulta.POR_NOMBRE_DE_CIUDAD.toString() + "( " + nombreCiudad + ")");
            consulta.setUrl(url.toString());
            consulta.setResultado(jsonRes.get("main").toString());
            consulta.setNombreUsuario(usuario);

            save(consulta);

            return new ResponseEntity<Mensaje>(new Mensaje("Clima Actual para la ciudad de: " + nombreCiudad, (JSONObject) jsonRes.get("main"), null), HttpStatus.OK);

        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(ClimaController.class.getName()).log(Level.SEVERE, null, ex);
            return new ResponseEntity<Mensaje>(new Mensaje("Ha ocurrido una Excepcion" + ex.getMessage(), null, null), HttpStatus.BAD_REQUEST);
        }

    }

// ENDPOINT 2 - PRONOSTICO CINCO DIAS ===============================================================================================================================================
    @Cacheable(value = "consultasCache", key = "'Cinco_Dias_'+#nombreCiudad")
    public ResponseEntity<Mensaje> pronosticoCincoDias(@PathVariable("nombreCiudad") String nombreCiudad, HttpServletRequest req) throws MalformedURLException {
        JSONObject jsonCoord = getCoordanadasPorNombreDeCiudad(nombreCiudad); //Obteber lat y on por el nombre de la ciudad
        String usuario = jwtProvider.getNombreUsuario(req);

        double lon = (double) jsonCoord.get("lon");
        double lat = (double) jsonCoord.get("lat");

        log.info("Consultando Clima proximos 5 dias de " + nombreCiudad + "...");
        URL url = new URL("https://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lon + "&appid=" + key + "&lang=es&cnt=5");
        log.info("Endpoint visitado: https://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lon + "&appid=" + key);

        String res = connection(url);// Resultado de consulta por lon y lat

        try {
            var jsonRes = parsearJSON(res);

            log.warn("Resultado JSON proximos 5 Dias: " + jsonRes);

            LocalDateTime fechaHora = LocalDateTime.now(); // Fecha y hora de la consulta
            Consulta consulta = new Consulta();
            consulta.setFecha(fechaHora);
            consulta.setTipoConsulta(TipoConsulta.PRONOSTICO_DE_5_DIAS.toString() + "( " + nombreCiudad + ")");
            consulta.setUrl(url.toString());

            JSONArray lista = (JSONArray) jsonRes.get("list");
            String resultado = "Resumne del Resultado: ";
            for (int i = 0; i < lista.size(); i++) {
                resultado += "[" + parsearJSON(lista.get(i).toString()).get("dt_txt").toString() + "] -> " + parsearJSON(lista.get(i).toString()).get("weather").toString() + ", ";
            }

            consulta.setResultado(resultado);
            consulta.setNombreUsuario(usuario);

            save(consulta);

            return new ResponseEntity<Mensaje>(new Mensaje("Clima proximos 5 dias para la ciudad de " + nombreCiudad, null, (JSONArray) jsonRes.get("list")), HttpStatus.OK);

        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(ClimaController.class.getName()).log(Level.SEVERE, null, ex);
            return new ResponseEntity<Mensaje>(new Mensaje("Ha ocurrido una Excepcion" + ex.getMessage(), null, null), HttpStatus.BAD_REQUEST);
        }
    }

// ENDPOINT 3 - CONTAMINACION DEL AIRE ===============================================================================================================================================    
    @Cacheable(value = "consultasCache", key = "'Contaminacion_Aire_'+#nombreCiudad")
    public ResponseEntity<Mensaje> contaminacionDelAire(@PathVariable("nombreCiudad") String nombreCiudad, HttpServletRequest req) throws MalformedURLException {
        JSONObject jsonCoord = getCoordanadasPorNombreDeCiudad(nombreCiudad); //Obteber lat y on por el nombre de la ciudad
        String usuario = jwtProvider.getNombreUsuario(req);

        double lon = (double) jsonCoord.get("lon");
        double lat = (double) jsonCoord.get("lat");

        log.info("Consultando Contaminacion del Aire de " + nombreCiudad + "...");
        URL url = new URL("https://api.openweathermap.org/data/2.5/air_pollution?lat=" + lat + "&lon=" + lon + "&appid=" + key + "&lang=es&cnt=5");
        log.info("Endpoint visitado: https://api.openweathermap.org/data/2.5/air_pollution?lat=" + lat + "&lon=" + lon + "&appid=" + key);

        String res = connection(url);// Resultado de consulta por lon y lat

        try {
            var jsonRes = parsearJSON(res);

            log.warn("Resultado JSON Contaminacion del Aire: " + jsonRes);

            LocalDateTime fechaHora = LocalDateTime.now(); // Fecha y hora de la consulta
            Consulta consulta = new Consulta();
            consulta.setFecha(fechaHora);
            consulta.setTipoConsulta(TipoConsulta.CONTAMIANCION_DEL_AIRE.toString() + "( " + nombreCiudad + ")");

            JSONArray lista = (JSONArray) jsonRes.get("list");
            String resultado = "";
            for (int i = 0; i < lista.size(); i++) {
                resultado = parsearJSON(lista.get(i).toString()).get("components").toString();
            }
            consulta.setResultado(resultado);
            consulta.setNombreUsuario(usuario);

            save(consulta);

            return new ResponseEntity<Mensaje>(new Mensaje("Contaminacion del Aire para " + nombreCiudad, null, (JSONArray) jsonRes.get("list")), HttpStatus.CREATED);

        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(ClimaController.class.getName()).log(Level.SEVERE, null, ex);
            return new ResponseEntity<Mensaje>(new Mensaje("Ha ocurrido una Excepcion" + ex.getMessage(), null, null), HttpStatus.BAD_REQUEST);
        }
    }

// ENDPOINT 4 - CONSULTAS POR USUARIO ===============================================================================================================================================    
    @Cacheable(value = "consultasCache", key = "#nombreUsuario")
    public ResponseEntity<Mensaje> listByNombreUsuario(String nombreUsuario) {
        List<Tuple> consultas = consultaRepository.findAllByNombreUsuario(nombreUsuario);

        JSONArray jsonArray = new JSONArray();
        
        for (Tuple consulta : consultas) {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("id", consulta.get("id"));
            jsonObject.put("fecha", consulta.get("fecha"));
            jsonObject.put("tipo", consulta.get("tipo"));
            jsonObject.put("url", consulta.get("url"));
            jsonObject.put("resultado", consulta.get("resultado"));
            jsonObject.put("usuario", consulta.get("usuario"));

            jsonArray.add(jsonObject);
        }
        return new ResponseEntity<Mensaje>(new Mensaje("Consultas del Usuario: " + nombreUsuario, null, (JSONArray) jsonArray), HttpStatus.OK);
    }

    
    public void save(Consulta consulta) {
        consultaRepository.save(consulta);
    }

//=================================================================================================================================================     
//=================================================================================================================================================     
    public JSONObject getCoordanadasPorNombreDeCiudad(String nombreCiudad) throws MalformedURLException {
        log.info("Obteniendo Coordenadas de: " + nombreCiudad);

        try {
            URL url = new URL("http://api.openweathermap.org/geo/1.0/direct?q=" + nombreCiudad + "&limit=1&appid=" + key + "&lang=es");

            String res = connection(url);
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(res);
            JSONArray obj2 = (JSONArray) obj;
            log.warn("Resultado Coordenadas: Lon " + parsearJSON(obj2.get(0).toString()).get("lon") + " -- Lat " + parsearJSON(obj2.get(0).toString()).get("lat"));

            return parsearJSON(obj2.get(0).toString());

        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(ClimaController.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    //=================================================================================================================================================    

    private String connection(URL url) {
        try {
            HttpURLConnection conn;
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.connect();

            if (conn.getResponseCode() != 200) {
                log.error("Fallo http " + conn.getResponseCode());
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String output, acum = "";
            while ((output = reader.readLine()) != null) {
                acum += output;
            }

            return acum;

        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(ClimaController.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }
    //=================================================================================================================================================    

    private JSONObject parsearJSON(String acumulado) throws ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(acumulado);
        JSONObject json = (JSONObject) obj;

        return json;
    }
}
