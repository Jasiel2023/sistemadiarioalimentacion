package com.equipodinamita.examplefeature.Repository;

import org.springframework.data.domain.Pageable;//Permite dividir el contenido web en varias paginas
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;//Metodos automaticos: save, findById, FindAll, DeleteId,COUNT
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;//Ayuda con filtros ,Busquedas Combinadas, Formularios Dinamicos

import com.equipodinamita.examplefeature.models.Task;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    // If you don't need a total row count, Slice is better than Page as it only performs a select query.
    // Page performs both a select and a count query.
    Slice<Task> findAllBy(Pageable pageable);
}
