package com.equipodinamita.controller.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.equipodinamita.base.models.Persona;

public interface PersonaRepository extends JpaRepository<Persona, Integer> {
    Optional<Persona> findByEmail(String email);
    

    boolean existsByEmail(String email);
}
