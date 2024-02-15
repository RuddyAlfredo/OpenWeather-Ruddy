package com.bootcamp.clima.dto;

import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jose.shaded.json.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
public class Mensaje {
    private String mensaje;
    private JSONObject respuesta;
    private JSONArray respuestaLista;
}
