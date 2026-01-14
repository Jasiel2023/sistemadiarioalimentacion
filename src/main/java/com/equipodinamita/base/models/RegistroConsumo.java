package com.equipodinamita.base.models;

import com.vaadin.copilot.shaded.classgraph.nonapi.json.Id;

import jakarta.persistence.*;

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
