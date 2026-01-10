package com.equipodinamita.base.models;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class ConsumoDiario {
    private Date fecha;

    private List<RegistroConsumo> registros = new ArrayList<>();

    public Date getFecha() {
        return this.fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public List<RegistroConsumo> getRegistros() {
        return this.registros;
    }

    public void setRegistros(List<RegistroConsumo> registros) {
        this.registros = registros;
    }
}
