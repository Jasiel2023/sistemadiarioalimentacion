package com.equipodinamita.controller.services;

import java.sql.Date;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.equipodinamita.base.models.Persona;
import com.equipodinamita.base.models.RolEnum;
import com.equipodinamita.controller.Repository.PersonaRepository;
import com.vaadin.copilot.shaded.checkerframework.checker.units.qual.t;

@Service
public class PersonaServices {
    private PersonaRepository personaRepository;

    public PersonaServices(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

   
    public Persona createPersona(Persona persona) throws Exception {
        // Lógica para crear una nueva persona
            if (personaRepository.existsByEmail(persona.getEmail())) {
                throw new Exception("El email ya está registrado");
            }

            if (persona.getRol() == null) {
                persona.setRol(RolEnum.CLIENTE);
            }

            return personaRepository.save(persona);
        
    }
    
    //Login

    public Persona autenticar(String email, String password ){
        Optional<Persona> usuarioEncontrado = personaRepository.findByEmail(email);
        
        if (usuarioEncontrado.isPresent()) {
            Persona persona = usuarioEncontrado.get();
            if (persona.getPassword().equals(password)) {
                return persona;
            }
        }
        return null; 
    }


    //Actualizar persona
    public Persona updatePersona(Integer id, Persona personaActualizada) throws Exception {
      
            Persona persona = personaRepository.findById(id).
            orElseThrow(() -> new Exception("No se encontro la persona con id " + id));
            persona.setNombre(personaActualizada.getNombre());
            persona.setApellido(personaActualizada.getApellido());
            persona.setEmail(personaActualizada.getEmail());
            persona.setEstaturaCm(personaActualizada.getEstaturaCm());
            persona.setPesoKg(personaActualizada.getPesoKg());
            persona.setFechaNacimiento(personaActualizada.getFechaNacimiento());
            persona.setTelefono(personaActualizada.getTelefono());

            return personaRepository.save(persona);
    }

    //Eliminar persona
    public void deletePersona(Integer id) throws Exception {
        if (!personaRepository.existsById(id)) {
            throw new Exception("No se encontra la persona con id " + id + " para eliminar");
        } 
        personaRepository.deleteById(id);
    }

}
