package com.equipodinamita.base.models;

import java.sql.Date;

public class Cliente  extends Persona {
    private Float estaturaCm;
    private Float pesoKg;
    private Date fechaNacimiento;
    private String telefono;

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

    
}
