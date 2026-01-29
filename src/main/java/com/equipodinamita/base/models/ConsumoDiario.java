package com.equipodinamita.base.models;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase para almacenar el resumen de consumo nutricional diario.
 * Puede representar tanto totales como promedios nutricionales.
 */
public class ConsumoDiario {
    private float calorias;
    private float carbohidratos;
    private float grasas;
    private float proteinas;
    private int totalRegistros;
    private Date fecha;
    private List<RegistroConsumo> registros = new ArrayList<>();

    public ConsumoDiario() {
        this.calorias = 0f;
        this.carbohidratos = 0f;
        this.grasas = 0f;
        this.proteinas = 0f;
        this.totalRegistros = 0;
    }

    // Getters y Setters
    public float getCalorias() {
        return this.calorias;
    }

    public void setCalorias(float calorias) {
        this.calorias = calorias;
    }

    public float getCarbohidratos() {
        return this.carbohidratos;
    }

    public void setCarbohidratos(float carbohidratos) {
        this.carbohidratos = carbohidratos;
    }

    public float getGrasas() {
        return this.grasas;
    }

    public void setGrasas(float grasas) {
        this.grasas = grasas;
    }

    public float getProteinas() {
        return this.proteinas;
    }

    public void setProteinas(float proteinas) {
        this.proteinas = proteinas;
    }

    public int getTotalRegistros() {
        return this.totalRegistros;
    }

    public void setTotalRegistros(int totalRegistros) {
        this.totalRegistros = totalRegistros;
    }

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

    @Override
    public String toString() {
        return String.format(
                "ConsumoDiario [Calorías=%.2f kcal, Proteínas=%.2f g, Carbohidratos=%.2f g, Grasas=%.2f g, Total Registros=%d]",
                calorias, proteinas, carbohidratos, grasas, totalRegistros);
    }
}
