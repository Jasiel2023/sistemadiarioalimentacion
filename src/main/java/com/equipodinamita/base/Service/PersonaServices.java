package com.equipodinamita.base.Service;


import org.springframework.stereotype.Service;

import com.equipodinamita.base.models.Persona;

import com.equipodinamita.base.Repository.PersonaRepository;


@Service
public class PersonaServices {
    private PersonaRepository personaRepository;


     public PersonaServices(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    


    //Actualizar persona
    public Persona updatePersona(Integer id, Persona personaActualizada) throws Exception {
      
            Persona persona = personaRepository.findById(id).
            orElseThrow(() -> new Exception("No se encontro la persona con id " + id));
            persona.setNombre(personaActualizada.getNombre());
            persona.setApellido(personaActualizada.getApellido());
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
