
package com.bootcamp.clima.repository;

import com.bootcamp.clima.entity.Consulta;
import jakarta.persistence.Tuple;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Integer> {
    
    

    @Query(value = "SELECT c.id AS id, c.fecha AS fecha, c.tipo_consulta AS tipo, c.resultado AS resultado, c.nombre_usuario AS usuario FROM Consultas c WHERE c.nombre_usuario=:nombreUsuario", nativeQuery = true)
    public List<Tuple> findAllByNombreUsuario(@Param("nombreUsuario") String nombreUsuario);
}
