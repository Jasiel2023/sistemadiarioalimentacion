package com.equipodinamita.base.models;



import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


@Entity
public class Persona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;
      @NotBlank(message = "El apellido no puede estar vacío")
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private RolEnum rol;

    private Float estaturaCm;
    private Float pesoKg;
    private Date fechaNacimiento;
    private String telefono;

    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;

    

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return this.nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return this.apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
       
    public Float getEstaturaCm() {
        return this.estaturaCm;
    }

    public void setEstaturaCm(Float estaturaCm) {
        this.estaturaCm = estaturaCm;
    }

    public Float getPesoKg() {
        return this.pesoKg;
    }

    public void setPesoKg(Float pesoKg) {
        this.pesoKg = pesoKg;
    }

    public Date getFechaNacimiento() {
        return this.fechaNacimiento;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getTelefono() {
        return this.telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
 

    public RolEnum getRol() {
        return this.rol;
    }

    public void setRol(RolEnum rol) {
        this.rol = rol;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
