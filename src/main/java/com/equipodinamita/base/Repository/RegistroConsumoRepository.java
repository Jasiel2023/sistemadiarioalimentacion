package com.equipodinamita.base.Repository;

import org.springframework.data.domain.Pageable;//Permite dividir el contenido web en varias paginas
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;//Metodos automaticos: save, findById, FindAll, DeleteId,COUNT

import com.equipodinamita.base.models.HorarioAlimenticioEnum;
import com.equipodinamita.base.models.RegistroConsumo;

public interface RegistroConsumoRepository extends JpaRepository<RegistroConsumo, Integer> {
    Slice<RegistroConsumo> findAllBy(Pageable pageable);

    Slice<RegistroConsumo> findByHorarioAlimenticio(HorarioAlimenticioEnum horarioAlimenticio, Pageable pageable);
}