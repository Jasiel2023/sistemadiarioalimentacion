package com.equipodinamita.base.models;



import jakarta.persistence.Entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;


@Entity
public class RegistroConsumo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
   private float cantidad;
    @ManyToOne
    @JoinColumn(name = "alimento_id")

   private Alimento alimento;

    public Alimento getAlimento() {
         return this.alimento;
    }
    public void setAlimento(Alimento alimento) {
         this.alimento = alimento;
    }
public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


   public Float getCantidad() {
       return this.cantidad;
   }

   public void setCantidad(Float cantidad) {
       this.cantidad = cantidad;
   }

}
