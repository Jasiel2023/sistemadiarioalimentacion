package com.equipodinamita.base.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.equipodinamita.base.Repository.ConsumoDiarioRepository;
import com.equipodinamita.base.Repository.RegistroConsumoRepository;
import com.equipodinamita.base.models.Alimento;
import com.equipodinamita.base.models.ConsumoDiario;
import com.equipodinamita.base.models.Cuenta;
import com.equipodinamita.base.models.HorarioAlimenticioEnum;
import com.equipodinamita.base.models.RegistroConsumo;

@Service
public class ConsumoDiarioService {

    private final RegistroConsumoRepository registroConsumoRepository;
    private final ConsumoDiarioRepository consumoDiarioRepository;

    public ConsumoDiarioService(RegistroConsumoRepository registroConsumoRepository,
            ConsumoDiarioRepository consumoDiarioRepository) {
        this.registroConsumoRepository = registroConsumoRepository;
        this.consumoDiarioRepository = consumoDiarioRepository;
    }

    /**
     * Obtiene o crea un ConsumoDiario para una fecha específica.
     * Este es el método principal para obtener el contenedor de registros de un
     * día.
     * 
     * @param cuenta la cuenta del usuario
     * @param fecha  la fecha del consumo diario
     * @return ConsumoDiario existente o nuevo
     */
    @Transactional
    public ConsumoDiario obtenerOCrearConsumoDiario(Cuenta cuenta, LocalDate fecha) {
        Date sqlFecha = Date.valueOf(fecha);
        Optional<ConsumoDiario> existente = consumoDiarioRepository.findByCuentaAndFecha(cuenta, sqlFecha);

        if (existente.isPresent()) {
            return existente.get();
        }

        // Crear nuevo ConsumoDiario para esta fecha
        ConsumoDiario nuevo = new ConsumoDiario();
        nuevo.setCuenta(cuenta);
        nuevo.setFecha(sqlFecha);
        return consumoDiarioRepository.save(nuevo);
    }

    /**
     * Obtiene o crea un ConsumoDiario para la fecha actual (hoy).
     * 
     * @param cuenta la cuenta del usuario
     * @return ConsumoDiario de hoy
     */
    @Transactional
    public ConsumoDiario obtenerOCrearConsumoDiarioHoy(Cuenta cuenta) {
        return obtenerOCrearConsumoDiario(cuenta, LocalDate.now());
    }

    /**
     * Guarda el consumo diario del usuario para la fecha actual.
     * Recalcula los totales nutricionales basándose en los registros asociados.
     * 
     * @param cuenta la cuenta del usuario
     * @return ConsumoDiario guardado
     */
    @Transactional
    public ConsumoDiario guardarConsumoDiario(Cuenta cuenta) {
        return guardarConsumoDiario(cuenta, LocalDate.now());
    }

    /**
     * Guarda el consumo diario del usuario para una fecha específica.
     * Recalcula los totales nutricionales basándose en los registros asociados.
     * 
     * @param cuenta la cuenta del usuario
     * @param fecha  la fecha del consumo
     * @return ConsumoDiario guardado
     */
    @Transactional
    public ConsumoDiario guardarConsumoDiario(Cuenta cuenta, LocalDate fecha) {
        ConsumoDiario consumoDiario = obtenerOCrearConsumoDiario(cuenta, fecha);

        // Obtener los registros asociados a este ConsumoDiario
        List<RegistroConsumo> registros = registroConsumoRepository.findAllByConsumoDiario(consumoDiario);

        // Recalcular totales
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

        consumoDiario.setCalorias(totalCalorias);
        consumoDiario.setProteinas(totalProteinas);
        consumoDiario.setCarbohidratos(totalCarbohidratos);
        consumoDiario.setGrasas(totalGrasas);
        consumoDiario.setTotalRegistros(registros.size());
        consumoDiario.setGuardado(true); // Marcar como guardado explícitamente

        return consumoDiarioRepository.save(consumoDiario);
    }

    /**
     * Obtiene todos los consumos diarios guardados de un usuario
     */
    @Transactional(readOnly = true)
    public List<ConsumoDiario> obtenerHistorialConsumo(Cuenta cuenta) {
        return consumoDiarioRepository.findAllByCuentaOrderByFechaDesc(cuenta);
    }

    /**
     * Obtiene el consumo diario de una fecha específica
     */
    @Transactional(readOnly = true)
    public Optional<ConsumoDiario> obtenerConsumoPorFecha(Cuenta cuenta, Date fecha) {
        return consumoDiarioRepository.findByCuentaAndFecha(cuenta, fecha);
    }

    /**
     * Obtiene el consumo diario de una fecha específica (usando LocalDate)
     */
    @Transactional(readOnly = true)
    public Optional<ConsumoDiario> obtenerConsumoPorFecha(Cuenta cuenta, LocalDate fecha) {
        return consumoDiarioRepository.findByCuentaAndFecha(cuenta, Date.valueOf(fecha));
    }

    /**
     * Verifica si el consumo de una fecha específica ha sido guardado
     * 
     * @param cuenta la cuenta del usuario
     * @param fecha  la fecha a verificar
     * @return true si el consumo ha sido guardado, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean consumoGuardado(Cuenta cuenta, Date fecha) {
        Optional<ConsumoDiario> consumo = consumoDiarioRepository.findByCuentaAndFecha(cuenta, fecha);
        return consumo.isPresent() && consumo.get().isGuardado();
    }

    /**
     * Verifica si el consumo de una fecha específica ha sido guardado
     * EXPLÍCITAMENTE
     * (no solo creado automáticamente)
     * 
     * @param cuenta la cuenta del usuario
     * @param fecha  la fecha a verificar
     * @return true si el consumo de esa fecha ha sido guardado explícitamente
     */
    @Transactional(readOnly = true)
    public boolean consumoGuardado(Cuenta cuenta, LocalDate fecha) {
        Date sqlFecha = Date.valueOf(fecha);
        Optional<ConsumoDiario> consumo = consumoDiarioRepository.findByCuentaAndFecha(cuenta, sqlFecha);
        return consumo.isPresent() && consumo.get().isGuardado();
    }

    /**
     * Verifica si hay registros de consumo para una fecha específica.
     * 
     * @param cuenta la cuenta del usuario
     * @param fecha  la fecha a verificar
     * @return true si hay al menos un registro de consumo para esa fecha
     */
    @Transactional(readOnly = true)
    public boolean tieneRegistrosEnFecha(Cuenta cuenta, LocalDate fecha) {
        Date sqlFecha = Date.valueOf(fecha);
        Optional<ConsumoDiario> consumo = consumoDiarioRepository.findByCuentaAndFecha(cuenta, sqlFecha);
        if (consumo.isPresent()) {
            List<RegistroConsumo> registros = registroConsumoRepository.findAllByConsumoDiario(consumo.get());
            return !registros.isEmpty();
        }
        return false;
    }

    /**
     * Verifica si hay registros de consumo para hoy.
     * 
     * @param cuenta la cuenta del usuario
     * @return true si hay al menos un registro de consumo para hoy
     */
    @Transactional(readOnly = true)
    public boolean tieneRegistrosHoy(Cuenta cuenta) {
        return tieneRegistrosEnFecha(cuenta, LocalDate.now());
    }

    /**
     * Verifica si se requiere guardar el consumo de una fecha.
     * Solo requiere guardar si hay registros Y no se ha guardado aún.
     * 
     * @param cuenta la cuenta del usuario
     * @param fecha  la fecha a verificar
     * @return true si hay registros sin guardar
     */
    @Transactional(readOnly = true)
    public boolean requiereGuardarConsumo(Cuenta cuenta, LocalDate fecha) {
        // Si no hay registros, no se requiere guardar
        if (!tieneRegistrosEnFecha(cuenta, fecha)) {
            return false;
        }
        // Si hay registros, verificar si ya se guardó
        return !consumoGuardado(cuenta, fecha);
    }

    /**
     * Verifica si se requiere guardar el consumo de hoy.
     * Solo requiere guardar si hay registros Y no se ha guardado aún.
     * 
     * @param cuenta la cuenta del usuario
     * @return true si hay registros de hoy sin guardar
     */
    @Transactional(readOnly = true)
    public boolean requiereGuardarConsumoHoy(Cuenta cuenta) {
        return requiereGuardarConsumo(cuenta, LocalDate.now());
    }

    /**
     * Verifica si el consumo de hoy ha sido guardado EXPLÍCITAMENTE por el usuario
     * (no solo creado automáticamente)
     * 
     * @param cuenta la cuenta del usuario
     * @return true si el consumo de hoy ha sido guardado explícitamente
     */
    @Transactional(readOnly = true)
    public boolean consumoDeHoyGuardado(Cuenta cuenta) {
        Date fechaHoy = Date.valueOf(LocalDate.now());
        Optional<ConsumoDiario> consumo = consumoDiarioRepository.findByCuentaAndFecha(cuenta, fechaHoy);
        return consumo.isPresent() && consumo.get().isGuardado();
    }

    /**
     * Obtiene los registros de consumo de una fecha específica
     */
    @Transactional(readOnly = true)
    public List<RegistroConsumo> obtenerRegistrosPorFecha(Cuenta cuenta, LocalDate fecha) {
        Optional<ConsumoDiario> consumoDiario = obtenerConsumoPorFecha(cuenta, fecha);
        if (consumoDiario.isPresent()) {
            return registroConsumoRepository.findAllByConsumoDiario(consumoDiario.get());
        }
        return List.of();
    }

    /**
     * Obtiene los registros de consumo de una fecha específica filtrados por
     * horario
     */
    @Transactional(readOnly = true)
    public List<RegistroConsumo> obtenerRegistrosPorFechaYHorario(Cuenta cuenta, LocalDate fecha,
            HorarioAlimenticioEnum horario) {
        Optional<ConsumoDiario> consumoDiario = obtenerConsumoPorFecha(cuenta, fecha);
        if (consumoDiario.isPresent()) {
            return registroConsumoRepository.findByConsumoDiarioAndHorarioAlimenticio(consumoDiario.get(), horario);
        }
        return List.of();
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
     * Calcula el consumo por cada tipo de comida para un ConsumoDiario específico
     * (fecha).
     * 
     * @param consumoDiario el consumo diario de la fecha específica
     * @return Map con el horario alimenticio como clave y su ConsumoDiario
     */
    @Transactional(readOnly = true)
    public Map<HorarioAlimenticioEnum, ConsumoDiario> calcularConsumoPorCadaHorarioYFecha(ConsumoDiario consumoDiario) {
        Map<HorarioAlimenticioEnum, ConsumoDiario> consumoPorHorario = new HashMap<>();

        if (consumoDiario == null) {
            return consumoPorHorario;
        }

        for (HorarioAlimenticioEnum horario : HorarioAlimenticioEnum.values()) {
            List<RegistroConsumo> registros = registroConsumoRepository
                    .findByConsumoDiarioAndHorarioAlimenticio(consumoDiario, horario);
            ConsumoDiario totalesHorario = calcularTotalesDeRegistros(registros);
            consumoPorHorario.put(horario, totalesHorario);
        }

        return consumoPorHorario;
    }

    /**
     * Calcula el TOTAL de consumo diario (suma de todos los registros) del usuario.
     * 
     * @deprecated Usar calcularTotalConsumoDiarioPorFecha para filtrar por fecha
     * @param cuenta la cuenta del usuario
     * @return ConsumoDiario con los totales (no promedios) de TODOS los registros
     */
    @Transactional(readOnly = true)
    public ConsumoDiario calcularTotalConsumoDiario(Cuenta cuenta) {
        List<RegistroConsumo> registros = registroConsumoRepository.findAllByCuenta(cuenta);
        return calcularTotalesDeRegistros(registros);
    }

    /**
     * Calcula el TOTAL de consumo diario para un ConsumoDiario específico (fecha).
     * Solo suma los registros asociados a ese ConsumoDiario.
     * 
     * @param consumoDiario el consumo diario de la fecha específica
     * @return ConsumoDiario con los totales de esa fecha específica
     */
    @Transactional(readOnly = true)
    public ConsumoDiario calcularTotalConsumoDiarioPorFecha(ConsumoDiario consumoDiario) {
        if (consumoDiario == null) {
            return new ConsumoDiario();
        }
        List<RegistroConsumo> registros = registroConsumoRepository.findAllByConsumoDiario(consumoDiario);
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
