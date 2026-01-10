package com.equipodinamita.base.models;

public class RegistroConsumo {
   private int cantidad;
   private HorarioAlimenticioEnum horarioAlimenticio;
   private ConsumoDiario consumoDiario;
   private Alimento alimento;

    public Alimento getAlimento() {
         return this.alimento;
    }
    public void setAlimento(Alimento alimento) {
         this.alimento = alimento;
    }

   public ConsumoDiario getConsumoDiario() {
       return this.consumoDiario;
   }

   public void setConsumoDiario(ConsumoDiario consumoDiario) {
       this.consumoDiario = consumoDiario;
   }

   public int getCantidad() {
       return this.cantidad;
   }

   public void setCantidad(int cantidad) {
       this.cantidad = cantidad;
   }

   public HorarioAlimenticioEnum getHorarioAlimenticio() {
       return this.horarioAlimenticio;
   }

   public void setHorarioAlimenticio(HorarioAlimenticioEnum horarioAlimenticio) {
       this.horarioAlimenticio = horarioAlimenticio;
   }


}
