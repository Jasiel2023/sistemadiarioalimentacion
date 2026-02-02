package com.equipodinamita.base.models;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

/**
 * Entidad para almacenar el resumen de consumo nutricional diario.
 * Guarda los totales nutricionales por fecha para cada usuario.
 */
@Entity
public class ConsumoDiario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private float calorias;
    private float carbohidratos;
    private float grasas;
    private float proteinas;
    private int totalRegistros;
    private Date fecha;

    // Indica si el usuario ha guardado explícitamente este consumo diario
    private boolean guardado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id")
    private Cuenta cuenta;

    @OneToMany(mappedBy = "consumoDiario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RegistroConsumo> registros = new ArrayList<>();

    public ConsumoDiario() {
        this.calorias = 0f;
        this.carbohidratos = 0f;
        this.grasas = 0f;
        this.proteinas = 0f;
        this.totalRegistros = 0;
        this.guardado = false;
    }

    // Getter y Setter para id
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    // Getter y Setter para cuenta
    public Cuenta getCuenta() {
        return this.cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
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

    public boolean isGuardado() {
        return this.guardado;
    }

    public void setGuardado(boolean guardado) {
        this.guardado = guardado;
    }

    @Override
    public String toString() {
        return String.format(
                "ConsumoDiario [Calorías=%.2f kcal, Proteínas=%.2f g, Carbohidratos=%.2f g, Grasas=%.2f g, Total Registros=%d]",
                calorias, proteinas, carbohidratos, grasas, totalRegistros);
    }
}
