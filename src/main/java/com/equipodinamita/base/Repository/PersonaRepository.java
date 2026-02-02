package com.equipodinamita.base.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.equipodinamita.base.models.Persona;

public interface PersonaRepository extends JpaRepository<Persona, Integer> {
    
}
