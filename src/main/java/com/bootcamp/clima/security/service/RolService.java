package com.bootcamp.clima.security.service;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bootcamp.clima.security.repository.RolRepository;
import com.bootcamp.clima.security.entity.Rol;
import com.bootcamp.clima.security.enums.RolNombre;

@Service
@Transactional
public class RolService {
    
    @Autowired //Inyectamos el repository para accesar a sus metodos
    RolRepository rolRepository;

    public Optional<Rol> getByRolNombre(RolNombre rolNombre){
        return rolRepository.findByRolNombre(rolNombre);
    }

    public void save(Rol rol){
        rolRepository.save(rol);
    }
}
