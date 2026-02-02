package com.equipodinamita.base.Service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.equipodinamita.base.Repository.RegistroConsumoRepository;
import com.equipodinamita.base.models.Alimento;
import com.equipodinamita.base.models.Cuenta;
import com.equipodinamita.base.models.HorarioAlimenticioEnum;
import com.equipodinamita.base.models.RegistroConsumo;

@Service // Capa de Negocio
public class RegistroConsumoService {

    private final RegistroConsumoRepository registroConsumoRepository;

    public RegistroConsumoService(RegistroConsumoRepository registroConsumoRepository) {
        this.registroConsumoRepository = registroConsumoRepository;
    }

    @Transactional
    public void crearRegistro(Alimento alimento, Float cantidad, HorarioAlimenticioEnum horarioAlimenticio,
            Cuenta cuenta) {

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
        return registroConsumoRepository.save(rc);
    }

    @Transactional
    public void eliminarRegistro(Integer id) {
        registroConsumoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<RegistroConsumo> list(Cuenta cuenta, Pageable pageable) {
        return registroConsumoRepository.findByCuenta(cuenta, pageable).toList();
    }

    @Transactional(readOnly = true)
    public List<RegistroConsumo> listByHorarioAlimenticio(Cuenta cuenta, HorarioAlimenticioEnum horarioAlimenticio,
            Pageable pageable) {
        return registroConsumoRepository.findByCuentaAndHorarioAlimenticio(cuenta, horarioAlimenticio, pageable)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RegistroConsumo> findAllByCuenta(Cuenta cuenta) {
        return registroConsumoRepository.findAllByCuenta(cuenta);
    }

    public void delete(Integer id) {
        registroConsumoRepository.deleteById(id);
    }

    private String safeEnum(Enum<?> value) {
        return value != null ? value.name() : "—";
    }

    private String safeText(String value) {
        return value != null ? value : "—";
    }

}
