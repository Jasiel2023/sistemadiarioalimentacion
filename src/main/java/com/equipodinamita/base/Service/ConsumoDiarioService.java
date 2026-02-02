package com.equipodinamita.base.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.equipodinamita.base.Repository.RegistroConsumoRepository;
import com.equipodinamita.base.models.Alimento;
import com.equipodinamita.base.models.ConsumoDiario;
import com.equipodinamita.base.models.Cuenta;
import com.equipodinamita.base.models.HorarioAlimenticioEnum;
import com.equipodinamita.base.models.RegistroConsumo;

@Service
public class ConsumoDiarioService {

    private final RegistroConsumoRepository registroConsumoRepository;

    public ConsumoDiarioService(RegistroConsumoRepository registroConsumoRepository) {
        this.registroConsumoRepository = registroConsumoRepository;
    }

    /**
     * Calcula los valores nutricionales de un registro de consumo
     * basado en la cantidad consumida y la porción base del alimento.
     * 
     * Fórmula: (valor nutricional / porción base) * cantidad consumida
     */
    private float calcularValorProporcional(Float valorBase, Float porcionBase, Float cantidadConsumida) {
        if (valorBase == null || porcionBase == null || cantidadConsumida == null || porcionBase == 0) {
            return 0f;
        }
        return (valorBase / porcionBase) * cantidadConsumida;
    }

    /**
     * Calcula el promedio de consumo diario de TODOS los registros de consumo del
     * usuario.
     * 
     * @param cuenta la cuenta del usuario
     * @return ConsumoDiario con los promedios de calorías, proteínas,
     *         carbohidratos y grasas
     */
    @Transactional(readOnly = true)
    public ConsumoDiario calcularPromedioConsumoGeneral(Cuenta cuenta) {
        List<RegistroConsumo> registros = registroConsumoRepository.findAllByCuenta(cuenta);
        return calcularPromedioDeRegistros(registros);
    }

    /**
     * Calcula el promedio de consumo por tipo de comida (horario alimenticio) del
     * usuario.
     * 
     * @param cuenta             la cuenta del usuario
     * @param horarioAlimenticio el tipo de comida (DESAYUNO, ALMUERZO, CENA,
     *                           ENTRETIEMPOS)
     * @return ConsumoDiario para ese horario específico
     */
    @Transactional(readOnly = true)
    public ConsumoDiario calcularPromedioPorHorario(Cuenta cuenta, HorarioAlimenticioEnum horarioAlimenticio) {
        List<RegistroConsumo> registros = registroConsumoRepository.findAllByCuenta(cuenta)
                .stream()
                .filter(r -> r.getHorarioAlimenticio() == horarioAlimenticio)
                .toList();
        return calcularPromedioDeRegistros(registros);
    }

    /**
     * Calcula el promedio de consumo para CADA tipo de comida del usuario.
     * 
     * @param cuenta la cuenta del usuario
     * @return Map con el horario alimenticio como clave y su ConsumoDiario
     */
    @Transactional(readOnly = true)
    public Map<HorarioAlimenticioEnum, ConsumoDiario> calcularPromedioPorCadaHorario(Cuenta cuenta) {
        Map<HorarioAlimenticioEnum, ConsumoDiario> promediosPorHorario = new HashMap<>();

        for (HorarioAlimenticioEnum horario : HorarioAlimenticioEnum.values()) {
            ConsumoDiario promedio = calcularPromedioPorHorario(cuenta, horario);
            promediosPorHorario.put(horario, promedio);
        }

        return promediosPorHorario;
    }

    /**
     * Calcula el TOTAL de consumo diario (suma de todos los registros) del usuario.
     * 
     * @param cuenta la cuenta del usuario
     * @return ConsumoDiario con los totales (no promedios)
     */
    @Transactional(readOnly = true)
    public ConsumoDiario calcularTotalConsumoDiario(Cuenta cuenta) {
        List<RegistroConsumo> registros = registroConsumoRepository.findAllByCuenta(cuenta);
        return calcularTotalesDeRegistros(registros);
    }

    /**
     * Método auxiliar para calcular el promedio de una lista de registros.
     */
    private ConsumoDiario calcularPromedioDeRegistros(List<RegistroConsumo> registros) {
        ConsumoDiario resultado = new ConsumoDiario();

        if (registros == null || registros.isEmpty()) {
            return resultado;
        }

        float totalCalorias = 0f;
        float totalProteinas = 0f;
        float totalCarbohidratos = 0f;
        float totalGrasas = 0f;

        for (RegistroConsumo registro : registros) {
            Alimento alimento = registro.getAlimento();
            Float cantidad = registro.getCantidad();

            if (alimento != null && cantidad != null) {
                Float porcionBase = alimento.getPorcionBase();

                totalCalorias += calcularValorProporcional(alimento.getCalorias(), porcionBase, cantidad);
                totalProteinas += calcularValorProporcional(alimento.getProteinas(), porcionBase, cantidad);
                totalCarbohidratos += calcularValorProporcional(alimento.getCarbohidratos(), porcionBase, cantidad);
                totalGrasas += calcularValorProporcional(alimento.getGrasas(), porcionBase, cantidad);
            }
        }

        int totalRegistros = registros.size();
        resultado.setCalorias(totalCalorias / totalRegistros);
        resultado.setProteinas(totalProteinas / totalRegistros);
        resultado.setCarbohidratos(totalCarbohidratos / totalRegistros);
        resultado.setGrasas(totalGrasas / totalRegistros);
        resultado.setTotalRegistros(totalRegistros);
        resultado.setRegistros(registros);

        return resultado;
    }

    /**
     * Método auxiliar para calcular los totales de una lista de registros.
     */
    private ConsumoDiario calcularTotalesDeRegistros(List<RegistroConsumo> registros) {
        ConsumoDiario resultado = new ConsumoDiario();

        if (registros == null || registros.isEmpty()) {
            return resultado;
        }

        float totalCalorias = 0f;
        float totalProteinas = 0f;
        float totalCarbohidratos = 0f;
        float totalGrasas = 0f;

        for (RegistroConsumo registro : registros) {
            Alimento alimento = registro.getAlimento();
            Float cantidad = registro.getCantidad();

            if (alimento != null && cantidad != null) {
                Float porcionBase = alimento.getPorcionBase();

                totalCalorias += calcularValorProporcional(alimento.getCalorias(), porcionBase, cantidad);
                totalProteinas += calcularValorProporcional(alimento.getProteinas(), porcionBase, cantidad);
                totalCarbohidratos += calcularValorProporcional(alimento.getCarbohidratos(), porcionBase, cantidad);
                totalGrasas += calcularValorProporcional(alimento.getGrasas(), porcionBase, cantidad);
            }
        }

        resultado.setCalorias(totalCalorias);
        resultado.setProteinas(totalProteinas);
        resultado.setCarbohidratos(totalCarbohidratos);
        resultado.setGrasas(totalGrasas);
        resultado.setTotalRegistros(registros.size());
        resultado.setRegistros(registros);

        return resultado;
    }
}
