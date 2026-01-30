package com.equipodinamita.examplefeature.models;

import java.time.Instant;//Mapea tabla en la bassse de datos
import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity //Indica que esta clase se podra guardar en la base de datos
@Table(name = "task") //Define el nombre de la tabla
public class Task {

    public static final int DESCRIPTION_MAX_LENGTH = 300;

    @Id // Clave primaria
    @GeneratedValue(strategy = GenerationType.SEQUENCE)//Generacion de ID automaticamente en la BD
    @Column(name = "task_id")//Nombre explicito de la columna
    private Long id;

    @Column(name = "description", nullable = false, length = DESCRIPTION_MAX_LENGTH)//Descripcion
    private String description = "";

    @Column(name = "creation_date", nullable = false)//Fecha de creacion
    private Instant creationDate;

    @Column(name = "due_date")//Fecha y hora actual Actual
    @Nullable
    private LocalDate dueDate;

    protected Task() { // To keep Hibernate happy
    }

    public Task(String description, Instant creationDate) {
        setDescription(description);
        this.creationDate = creationDate;
    }

    public @Nullable Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new IllegalArgumentException("Description length exceeds " + DESCRIPTION_MAX_LENGTH);
        }
        this.description = description;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public @Nullable LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(@Nullable LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        Task other = (Task) obj;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        // Hashcode should never change during the lifetime of an object. Because of
        // this we can't use getId() to calculate the hashcode. Unless you have sets
        // with lots of entities in them, returning the same hashcode should not be a
        // problem.
        return getClass().hashCode();
    }
}
