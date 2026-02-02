package com.equipodinamita.base.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.equipodinamita.base.models.ConsumoDiario;
import com.equipodinamita.base.models.Cuenta;

public interface ConsumoDiarioRepository extends JpaRepository<ConsumoDiario, Integer> {

    /**
     * Busca un consumo diario por cuenta y fecha
     */
    Optional<ConsumoDiario> findByCuentaAndFecha(Cuenta cuenta, Date fecha);

    /**
     * Busca todos los consumos diarios de una cuenta
     */
    List<ConsumoDiario> findAllByCuenta(Cuenta cuenta);

    /**
     * Busca todos los consumos diarios de una cuenta ordenados por fecha
     * descendente
     */
    List<ConsumoDiario> findAllByCuentaOrderByFechaDesc(Cuenta cuenta);

    /**
     * Verifica si existe un consumo diario para una cuenta y fecha específica
     */
    boolean existsByCuentaAndFecha(Cuenta cuenta, Date fecha);
}
