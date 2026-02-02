package com.equipodinamita.base.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Alimento {
    public static final int DESCRIPTION_MAX_LENGTH = 300;


    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;
    private Float calorias;
    private Float proteinas;
    private Float carbohidratos;
    private Float grasas;
    private Float porcionBase;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", length = 50, nullable = false)
    private UnidadEnum unidadMedida;

    @Enumerated(EnumType.STRING)
    private CategoriaEnum categoria;

    @OneToMany(mappedBy = "alimento", cascade = CascadeType.ALL)
    private List<RegistroConsumo> registrosConsumo = new ArrayList<>();

    public List<RegistroConsumo> getRegistrosConsumo() {
        return this.registrosConsumo;
    }

    public void setRegistrosConsumo(List<RegistroConsumo> registrosConsumo) {
        this.registrosConsumo = registrosConsumo;
    }

    protected Alimento(){
    }

    public Alimento(String nombre, Float calorias, Float proteinas, Float carbohidratos, Float grasas, Float porcionBase, UnidadEnum unidadMedida, CategoriaEnum categoria){
        setNombre(nombre);
        setCalorias(calorias);
        setProteinas(proteinas);
        setCarbohidratos(carbohidratos);
        setGrasas(grasas);
        setPorcionBase(porcionBase);
        setUnidadMedida(unidadMedida);
        setCategoria(categoria);
    }


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

    public Float getCalorias() {
        return this.calorias;
    }

    public void setCalorias(Float calorias) {
        this.calorias = calorias;
    }

    public Float getProteinas() {
        return this.proteinas;
    }

    public void setProteinas(Float proteinas) {
        this.proteinas = proteinas;
    }

    public Float getCarbohidratos() {
        return this.carbohidratos;
    }

    public void setCarbohidratos(Float carbohidratos) {
        this.carbohidratos = carbohidratos;
    }

    public Float getGrasas() {
        return this.grasas;
    }

    public void setGrasas(Float grasas) {
        this.grasas = grasas;
    }

    public Float getPorcionBase() {
        return this.porcionBase;
    }

    public void setPorcionBase(Float porcionBase) {
        this.porcionBase = porcionBase;
    }
    
    public UnidadEnum getUnidadMedida() {
        return this.unidadMedida;
    }

    public void setUnidadMedida(UnidadEnum unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

        public CategoriaEnum getCategoria() {
        return this.categoria;
    }

    public void setCategoria(CategoriaEnum categoria) {
        this.categoria = categoria;
    }

}
