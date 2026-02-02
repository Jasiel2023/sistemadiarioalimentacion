package com.equipodinamita.base.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.equipodinamita.base.Repository.ConsumoDiarioRepository;
import com.equipodinamita.base.Repository.RegistroConsumoRepository;
import com.equipodinamita.base.models.Alimento;
import com.equipodinamita.base.models.ConsumoDiario;
import com.equipodinamita.base.models.Cuenta;
import com.equipodinamita.base.models.HorarioAlimenticioEnum;
import com.equipodinamita.base.models.RegistroConsumo;

@Service // Capa de Negocio
public class RegistroConsumoService {

    private final RegistroConsumoRepository registroConsumoRepository;
    private final ConsumoDiarioRepository consumoDiarioRepository;

    public RegistroConsumoService(RegistroConsumoRepository registroConsumoRepository,
            ConsumoDiarioRepository consumoDiarioRepository) {
        this.registroConsumoRepository = registroConsumoRepository;
        this.consumoDiarioRepository = consumoDiarioRepository;
    }

    /**
     * Crea un registro de consumo asociado a un ConsumoDiario específico.
     * Este es el método principal para crear registros por fecha.
     */
    @Transactional
    public RegistroConsumo crearRegistro(Alimento alimento, Float cantidad, HorarioAlimenticioEnum horarioAlimenticio,
            Cuenta cuenta, ConsumoDiario consumoDiario) {

        if (alimento == null) {
            throw new IllegalArgumentException("El alimento es obligatorio");
        }

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }

        if (horarioAlimenticio == null) {
            throw new IllegalArgumentException("El horario de comida es obligatorio");
        }

        if (cuenta == null) {
            throw new IllegalArgumentException("La cuenta del usuario es obligatoria");
        }

        if (consumoDiario == null) {
            throw new IllegalArgumentException("El consumo diario es obligatorio");
        }

        RegistroConsumo rc = new RegistroConsumo();
        rc.setAlimento(alimento);
        rc.setCantidad(cantidad);
        rc.setHorarioAlimenticio(horarioAlimenticio);
        rc.setCuenta(cuenta);
        rc.setConsumoDiario(consumoDiario);

        // Marcar el ConsumoDiario como no guardado (hay cambios pendientes)
        marcarConsumoDiarioComoNoGuardado(consumoDiario);

        return registroConsumoRepository.save(rc);
    }

    /**
     * Método legacy para compatibilidad (sin fecha específica, usa hoy).
     * 
     * @deprecated Usar crearRegistro con ConsumoDiario
     */
    @Transactional
    public void crearRegistro(Alimento alimento, Float cantidad, HorarioAlimenticioEnum horarioAlimenticio,
            Cuenta cuenta) {
        // Este método ya no debería usarse, pero lo mantenemos para compatibilidad
        // Los registros creados así no tendrán ConsumoDiario asociado
        if (alimento == null) {
            throw new IllegalArgumentException("El alimento es obligatorio");
        }

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }

        if (horarioAlimenticio == null) {
            throw new IllegalArgumentException("El horario de comida es obligatorio");
        }

        if (cuenta == null) {
            throw new IllegalArgumentException("La cuenta del usuario es obligatoria");
        }

        RegistroConsumo rc = new RegistroConsumo();
        rc.setAlimento(alimento);
        rc.setCantidad(cantidad);
        rc.setHorarioAlimenticio(horarioAlimenticio);
        rc.setCuenta(cuenta);

        registroConsumoRepository.save(rc);
    }

    @Transactional
    public RegistroConsumo actualizarRegistro(RegistroConsumo rc) {
        if (rc.getId() == null) {
            throw new IllegalArgumentException("Registro sin ID");
        }
        if (rc.getAlimento() == null || rc.getCantidad() == null || rc.getCantidad() <= 0) {
            throw new IllegalArgumentException("Datos de consumo inválidos");
        }

        // Marcar el ConsumoDiario como no guardado (hay cambios pendientes)
        if (rc.getConsumoDiario() != null) {
            marcarConsumoDiarioComoNoGuardado(rc.getConsumoDiario());
        }

        return registroConsumoRepository.save(rc);
    }

    @Transactional
    public void eliminarRegistro(Integer id) {
        // Obtener el registro antes de eliminarlo para poder marcar el ConsumoDiario
        Optional<RegistroConsumo> registroOpt = registroConsumoRepository.findById(id);
        if (registroOpt.isPresent()) {
            RegistroConsumo registro = registroOpt.get();
            if (registro.getConsumoDiario() != null) {
                marcarConsumoDiarioComoNoGuardado(registro.getConsumoDiario());
            }
        }
        registroConsumoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<RegistroConsumo> list(Cuenta cuenta, Pageable pageable) {
        return registroConsumoRepository.findByCuenta(cuenta, pageable).toList();
    }

    /**
     * Lista registros por ConsumoDiario (fecha específica)
     */
    @Transactional(readOnly = true)
    public List<RegistroConsumo> listByConsumoDiario(ConsumoDiario consumoDiario, Pageable pageable) {
        return registroConsumoRepository.findByConsumoDiario(consumoDiario, pageable).toList();
    }

    /**
     * Lista todos los registros de un ConsumoDiario
     */
    @Transactional(readOnly = true)
    public List<RegistroConsumo> findAllByConsumoDiario(ConsumoDiario consumoDiario) {
        return registroConsumoRepository.findAllByConsumoDiario(consumoDiario);
    }

    @Transactional(readOnly = true)
    public List<RegistroConsumo> listByHorarioAlimenticio(Cuenta cuenta, HorarioAlimenticioEnum horarioAlimenticio,
            Pageable pageable) {
        return registroConsumoRepository.findByCuentaAndHorarioAlimenticio(cuenta, horarioAlimenticio, pageable)
                .toList();
    }

    /**
     * Lista registros por ConsumoDiario y horario alimenticio
     */
    @Transactional(readOnly = true)
    public List<RegistroConsumo> listByConsumoDiarioAndHorario(ConsumoDiario consumoDiario,
            HorarioAlimenticioEnum horarioAlimenticio, Pageable pageable) {
        return registroConsumoRepository.findByConsumoDiarioAndHorarioAlimenticio(
                consumoDiario, horarioAlimenticio, pageable).toList();
    }

    /**
     * Obtiene registros por ConsumoDiario y horario (sin paginación)
     */
    @Transactional(readOnly = true)
    public List<RegistroConsumo> findByConsumoDiarioAndHorario(ConsumoDiario consumoDiario,
            HorarioAlimenticioEnum horarioAlimenticio) {
        return registroConsumoRepository.findByConsumoDiarioAndHorarioAlimenticio(consumoDiario, horarioAlimenticio);
    }

    @Transactional(readOnly = true)
    public List<RegistroConsumo> findAllByCuenta(Cuenta cuenta) {
        return registroConsumoRepository.findAllByCuenta(cuenta);
    }

    public void delete(Integer id) {
        registroConsumoRepository.deleteById(id);
    }

    /**
     * Marca el ConsumoDiario como no guardado.
     * Esto indica que hay cambios pendientes de guardar.
     */
    private void marcarConsumoDiarioComoNoGuardado(ConsumoDiario consumoDiario) {
        if (consumoDiario != null && consumoDiario.getId() != null) {
            consumoDiario.setGuardado(false);
            consumoDiarioRepository.save(consumoDiario);
        }
    }

    private String safeEnum(Enum<?> value) {
        return value != null ? value.name() : "—";
    }

    private String safeText(String value) {
        return value != null ? value : "—";
    }

}
