package com.equipodinamita.base.Repository;

import org.springframework.data.domain.Pageable;//Permite dividir el contenido web en varias paginas
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;//Metodos automaticos: save, findById, FindAll, DeleteId,COUNT

import com.equipodinamita.base.models.Alimento;//Ayuda con filtros ,Busquedas Combinadas, Formularios Dinamicos

public interface AlimentoRepository extends JpaRepository <Alimento, Integer>{
    Slice<Alimento> findAllBy(Pageable pageable);
    boolean existsByNombreIgnoreCase(String nombre);
}