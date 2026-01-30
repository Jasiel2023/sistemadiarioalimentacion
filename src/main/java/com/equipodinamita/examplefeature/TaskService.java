package com.equipodinamita.examplefeature;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.equipodinamita.examplefeature.Repository.TaskRepository;
import com.equipodinamita.examplefeature.models.Task;

@Service //Capa de Negocio
public class TaskService {

    private final TaskRepository taskRepository;

    TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional//Abre transaccion, ejecuta metodo,ok? , realiza estos en SQL por parte de Spring
    public void createTask(String description, @Nullable LocalDate dueDate) {
        var task = new Task(description, Instant.now());
        task.setDueDate(dueDate);
        taskRepository.saveAndFlush(task);//Guardado ek la base de datos
    }

    @Transactional(readOnly = true)
    public List<Task> list(Pageable pageable) {
        return taskRepository.findAllBy(pageable).toList();
    }

}
