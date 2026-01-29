package com.equipodinamita.base.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.equipodinamita.base.Repository.RegistroConsumoRepository;
import com.equipodinamita.base.models.Alimento;
import com.equipodinamita.base.models.HorarioAlimenticioEnum;
import com.equipodinamita.base.models.RegistroConsumo;

@Service
public class ConsumoDiarioService {

    private final RegistroConsumoRepository registroConsumoRepository;

    public ConsumoDiarioService(RegistroConsumoRepository registroConsumoRepository) {
        this.registroConsumoRepository = registroConsumoRepository;
    }

    /**
     * Clase interna para almacenar el resumen de promedios nutricionales
     */
    public static class PromedioNutricional {
        private float promedioCalorias;
        private float promedioProteinas;
        private float promedioCarbohidratos;
        private float promedioGrasas;
        private int totalRegistros;

        public PromedioNutricional() {
            this.promedioCalorias = 0f;
            this.promedioProteinas = 0f;
            this.promedioCarbohidratos = 0f;
            this.promedioGrasas = 0f;
            this.totalRegistros = 0;
        }

        // Getters y Setters
        public float getPromedioCalorias() {
            return promedioCalorias;
        }

        public void setPromedioCalorias(float promedioCalorias) {
            this.promedioCalorias = promedioCalorias;
        }

        public float getPromedioProteinas() {
            return promedioProteinas;
        }

        public void setPromedioProteinas(float promedioProteinas) {
            this.promedioProteinas = promedioProteinas;
        }

        public float getPromedioCarbohidratos() {
            return promedioCarbohidratos;
        }

        public void setPromedioCarbohidratos(float promedioCarbohidratos) {
            this.promedioCarbohidratos = promedioCarbohidratos;
        }

        public float getPromedioGrasas() {
            return promedioGrasas;
        }

        public void setPromedioGrasas(float promedioGrasas) {
            this.promedioGrasas = promedioGrasas;
        }

        public int getTotalRegistros() {
            return totalRegistros;
        }

        public void setTotalRegistros(int totalRegistros) {
            this.totalRegistros = totalRegistros;
        }

        @Override
        public String toString() {
            return String.format(
                    "PromedioNutricional [Calorías=%.2f, Proteínas=%.2f g, Carbohidratos=%.2f g, Grasas=%.2f g, Total Registros=%d]",
                    promedioCalorias, promedioProteinas, promedioCarbohidratos, promedioGrasas, totalRegistros);
        }
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
     * Calcula el promedio de consumo diario de TODOS los registros de consumo.
     * 
     * @return PromedioNutricional con los promedios de calorías, proteínas,
     *         carbohidratos y grasas
     */
    @Transactional(readOnly = true)
    public PromedioNutricional calcularPromedioConsumoGeneral() {
        List<RegistroConsumo> registros = registroConsumoRepository.findAll();
        return calcularPromedioDeRegistros(registros);
    }

    /**
     * Calcula el promedio de consumo por tipo de comida (horario alimenticio).
     * 
     * @param horarioAlimenticio el tipo de comida (DESAYUNO, ALMUERZO, CENA,
     *                           ENTRETIEMPOS)
     * @return PromedioNutricional para ese horario específico
     */
    @Transactional(readOnly = true)
    public PromedioNutricional calcularPromedioPorHorario(HorarioAlimenticioEnum horarioAlimenticio) {
        List<RegistroConsumo> registros = registroConsumoRepository.findAll()
                .stream()
                .filter(r -> r.getHorarioAlimenticio() == horarioAlimenticio)
                .toList();
        return calcularPromedioDeRegistros(registros);
    }

    /**
     * Calcula el promedio de consumo para CADA tipo de comida.
     * 
     * @return Map con el horario alimenticio como clave y su promedio nutricional
     */
    @Transactional(readOnly = true)
    public Map<HorarioAlimenticioEnum, PromedioNutricional> calcularPromedioPorCadaHorario() {
        Map<HorarioAlimenticioEnum, PromedioNutricional> promediosPorHorario = new HashMap<>();

        for (HorarioAlimenticioEnum horario : HorarioAlimenticioEnum.values()) {
            PromedioNutricional promedio = calcularPromedioPorHorario(horario);
            promediosPorHorario.put(horario, promedio);
        }

        return promediosPorHorario;
    }

    /**
     * Calcula el TOTAL de consumo diario (suma de todos los registros).
     * 
     * @return PromedioNutricional con los totales (no promedios)
     */
    @Transactional(readOnly = true)
    public PromedioNutricional calcularTotalConsumoDiario() {
        List<RegistroConsumo> registros = registroConsumoRepository.findAll();
        return calcularTotalesDeRegistros(registros);
    }

    /**
     * Método auxiliar para calcular el promedio de una lista de registros.
     */
    private PromedioNutricional calcularPromedioDeRegistros(List<RegistroConsumo> registros) {
        PromedioNutricional resultado = new PromedioNutricional();

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
        resultado.setPromedioCalorias(totalCalorias / totalRegistros);
        resultado.setPromedioProteinas(totalProteinas / totalRegistros);
        resultado.setPromedioCarbohidratos(totalCarbohidratos / totalRegistros);
        resultado.setPromedioGrasas(totalGrasas / totalRegistros);
        resultado.setTotalRegistros(totalRegistros);

        return resultado;
    }

    /**
     * Método auxiliar para calcular los totales de una lista de registros.
     */
    private PromedioNutricional calcularTotalesDeRegistros(List<RegistroConsumo> registros) {
        PromedioNutricional resultado = new PromedioNutricional();

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

        resultado.setPromedioCalorias(totalCalorias);
        resultado.setPromedioProteinas(totalProteinas);
        resultado.setPromedioCarbohidratos(totalCarbohidratos);
        resultado.setPromedioGrasas(totalGrasas);
        resultado.setTotalRegistros(registros.size());

        return resultado;
    }

    // =========================================================================
    // MÉTODO MAIN DE PRUEBA CON DATOS REALES DE LA BASE DE DATOS
    // =========================================================================

    /**
     * Clase interna para ejecutar el main con Spring Boot y acceder a datos reales.
     */
    @SpringBootApplication(scanBasePackages = "com.equipodinamita")
    public static class ConsumoDiarioTestRunner {

        public static void main(String[] args) {
            SpringApplication.run(ConsumoDiarioTestRunner.class, args);
        }

        @Bean
        CommandLineRunner ejecutarPrueba(RegistroConsumoRepository registroConsumoRepository) {
            return args -> {
                System.out.println();
                System.out.println("=".repeat(70));
                System.out.println("    PRUEBA DEL SERVICIO DE CONSUMO DIARIO - DATOS REALES");
                System.out.println("=".repeat(70));
                System.out.println();

                // Obtener todos los registros de la base de datos
                List<RegistroConsumo> registros = registroConsumoRepository.findAll();

                if (registros.isEmpty()) {
                    System.out.println("⚠️  No hay registros de consumo en la base de datos.");
                    System.out.println("    Registre algunos alimentos primero para ver los promedios.");
                    System.out.println("=".repeat(70));
                    return;
                }

                // Mostrar los registros encontrados
                mostrarRegistrosEncontrados(registros);

                // Calcular y mostrar promedios
                mostrarPromedioGeneral(registros);
                mostrarPromedioPorHorario(registros);
                mostrarTotalDelDia(registros);
            };
        }
    }

    /**
     * Muestra los registros de consumo encontrados en la base de datos.
     */
    private static void mostrarRegistrosEncontrados(List<RegistroConsumo> registros) {
        System.out.println(">>> REGISTROS DE CONSUMO EN LA BASE DE DATOS:");
        System.out.println("-".repeat(70));
        System.out.printf("%-5s %-25s %10s %-12s %15s%n", "ID", "Alimento", "Cantidad", "Unidad", "Horario");
        System.out.println("-".repeat(70));

        for (RegistroConsumo r : registros) {
            Alimento alimento = r.getAlimento();
            if (alimento != null) {
                System.out.printf("%-5d %-25s %10.1f %-12s %15s%n",
                        r.getId(),
                        alimento.getNombre(),
                        r.getCantidad(),
                        alimento.getUnidadMedida() != null ? alimento.getUnidadMedida() : "-",
                        r.getHorarioAlimenticio() != null ? r.getHorarioAlimenticio() : "-");
            } else {
                System.out.printf("%-5d %-25s %10.1f %-12s %15s%n",
                        r.getId(), "(Alimento no encontrado)", r.getCantidad(), "-",
                        r.getHorarioAlimenticio() != null ? r.getHorarioAlimenticio() : "-");
            }
        }
        System.out.println();
    }

    /**
     * Muestra el promedio general de todos los registros.
     */
    private static void mostrarPromedioGeneral(List<RegistroConsumo> registros) {
        PromedioNutricional promedio = calcularPromedioDeRegistrosStatic(registros);

        System.out.println(">>> PROMEDIO GENERAL DE CONSUMO (por registro):");
        System.out.println("-".repeat(70));
        System.out.printf("  Promedio de Calorías:      %10.2f kcal%n", promedio.getPromedioCalorias());
        System.out.printf("  Promedio de Proteínas:     %10.2f g%n", promedio.getPromedioProteinas());
        System.out.printf("  Promedio de Carbohidratos: %10.2f g%n", promedio.getPromedioCarbohidratos());
        System.out.printf("  Promedio de Grasas:        %10.2f g%n", promedio.getPromedioGrasas());
        System.out.printf("  Total de registros:        %10d%n", promedio.getTotalRegistros());
        System.out.println();
    }

    /**
     * Muestra el promedio por cada tipo de comida (horario alimenticio).
     */
    private static void mostrarPromedioPorHorario(List<RegistroConsumo> registros) {
        System.out.println(">>> PROMEDIO POR TIPO DE COMIDA:");
        System.out.println("-".repeat(70));

        for (HorarioAlimenticioEnum horario : HorarioAlimenticioEnum.values()) {
            List<RegistroConsumo> registrosHorario = registros.stream()
                    .filter(r -> r.getHorarioAlimenticio() == horario)
                    .toList();

            if (!registrosHorario.isEmpty()) {
                PromedioNutricional promedio = calcularPromedioDeRegistrosStatic(registrosHorario);

                System.out.printf("%n  [%s] (%d registros):%n", horario.name(), promedio.getTotalRegistros());
                System.out.printf("    Promedio Calorías:      %8.2f kcal%n", promedio.getPromedioCalorias());
                System.out.printf("    Promedio Proteínas:     %8.2f g%n", promedio.getPromedioProteinas());
                System.out.printf("    Promedio Carbohidratos: %8.2f g%n", promedio.getPromedioCarbohidratos());
                System.out.printf("    Promedio Grasas:        %8.2f g%n", promedio.getPromedioGrasas());
            }
        }
        System.out.println();
    }

    /**
     * Muestra el total del día (suma de todos los consumos).
     */
    private static void mostrarTotalDelDia(List<RegistroConsumo> registros) {
        PromedioNutricional total = calcularTotalesDeRegistrosStatic(registros);

        System.out.println(">>> TOTAL DEL DÍA (suma de todos los consumos):");
        System.out.println("-".repeat(70));
        System.out.printf("  Total de Calorías:      %10.2f kcal%n", total.getPromedioCalorias());
        System.out.printf("  Total de Proteínas:     %10.2f g%n", total.getPromedioProteinas());
        System.out.printf("  Total de Carbohidratos: %10.2f g%n", total.getPromedioCarbohidratos());
        System.out.printf("  Total de Grasas:        %10.2f g%n", total.getPromedioGrasas());
        System.out.printf("  Total de registros:     %10d%n", total.getTotalRegistros());
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println("                    FIN DE LA PRUEBA");
        System.out.println("=".repeat(70));
    }

    // =========================================================================
    // MÉTODOS ESTÁTICOS AUXILIARES PARA CÁLCULOS
    // =========================================================================

    private static float calcularValorProporcionalStatic(Float valorBase, Float porcionBase, Float cantidadConsumida) {
        if (valorBase == null || porcionBase == null || cantidadConsumida == null || porcionBase == 0) {
            return 0f;
        }
        return (valorBase / porcionBase) * cantidadConsumida;
    }

    private static PromedioNutricional calcularPromedioDeRegistrosStatic(List<RegistroConsumo> registros) {
        PromedioNutricional resultado = new PromedioNutricional();
        if (registros == null || registros.isEmpty())
            return resultado;

        float totalCalorias = 0f, totalProteinas = 0f, totalCarbohidratos = 0f, totalGrasas = 0f;

        for (RegistroConsumo registro : registros) {
            Alimento alimento = registro.getAlimento();
            Float cantidad = registro.getCantidad();
            if (alimento != null && cantidad != null) {
                Float porcionBase = alimento.getPorcionBase();
                totalCalorias += calcularValorProporcionalStatic(alimento.getCalorias(), porcionBase, cantidad);
                totalProteinas += calcularValorProporcionalStatic(alimento.getProteinas(), porcionBase, cantidad);
                totalCarbohidratos += calcularValorProporcionalStatic(alimento.getCarbohidratos(), porcionBase,
                        cantidad);
                totalGrasas += calcularValorProporcionalStatic(alimento.getGrasas(), porcionBase, cantidad);
            }
        }

        int total = registros.size();
        resultado.setPromedioCalorias(totalCalorias / total);
        resultado.setPromedioProteinas(totalProteinas / total);
        resultado.setPromedioCarbohidratos(totalCarbohidratos / total);
        resultado.setPromedioGrasas(totalGrasas / total);
        resultado.setTotalRegistros(total);
        return resultado;
    }

    private static PromedioNutricional calcularTotalesDeRegistrosStatic(List<RegistroConsumo> registros) {
        PromedioNutricional resultado = new PromedioNutricional();
        if (registros == null || registros.isEmpty())
            return resultado;

        float totalCalorias = 0f, totalProteinas = 0f, totalCarbohidratos = 0f, totalGrasas = 0f;

        for (RegistroConsumo registro : registros) {
            Alimento alimento = registro.getAlimento();
            Float cantidad = registro.getCantidad();
            if (alimento != null && cantidad != null) {
                Float porcionBase = alimento.getPorcionBase();
                totalCalorias += calcularValorProporcionalStatic(alimento.getCalorias(), porcionBase, cantidad);
                totalProteinas += calcularValorProporcionalStatic(alimento.getProteinas(), porcionBase, cantidad);
                totalCarbohidratos += calcularValorProporcionalStatic(alimento.getCarbohidratos(), porcionBase,
                        cantidad);
                totalGrasas += calcularValorProporcionalStatic(alimento.getGrasas(), porcionBase, cantidad);
            }
        }

        resultado.setPromedioCalorias(totalCalorias);
        resultado.setPromedioProteinas(totalProteinas);
        resultado.setPromedioCarbohidratos(totalCarbohidratos);
        resultado.setPromedioGrasas(totalGrasas);
        resultado.setTotalRegistros(registros.size());
        return resultado;
    }
}
