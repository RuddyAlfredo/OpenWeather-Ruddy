
package com.bootcamp.clima.dto;

import com.bootcamp.clima.security.enums.TipoConsulta;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConsultaDto implements Serializable {
    
    private String fecha;

    private TipoConsulta tipoConsulta;
    
    private String url;
    
    private String resultado;
    
    private String nombreUsuario;

    public ConsultaDto(String fecha, TipoConsulta tipoConsulta, String url, String resultado, String nombreUsuario) {
        this.fecha = fecha;
        this.tipoConsulta = tipoConsulta;
        this.url = url;
        this.resultado = resultado;
        this.nombreUsuario = nombreUsuario;
    }
    
}
