package com.equipodinamita.base.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.equipodinamita.base.models.Cuenta;
import com.equipodinamita.base.models.Persona;

public interface CuentaRepository extends JpaRepository<Cuenta, Integer> {
      Optional<Cuenta> findByEmail(String email);
    

    boolean existsByEmail(String email);

}
