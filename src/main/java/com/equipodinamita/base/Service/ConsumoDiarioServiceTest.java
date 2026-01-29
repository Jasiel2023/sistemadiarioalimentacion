package com.equipodinamita.base.Service;

import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.equipodinamita.base.Repository.RegistroConsumoRepository;
import com.equipodinamita.base.models.Alimento;
import com.equipodinamita.base.models.ConsumoDiario;
import com.equipodinamita.base.models.HorarioAlimenticioEnum;
import com.equipodinamita.base.models.RegistroConsumo;

/**
 * Clase de prueba para el servicio de Consumo Diario.
 * Ejecuta pruebas con datos reales de la base de datos.
 * 
 * Para ejecutar: Run as Java Application
 */
@SpringBootApplication(scanBasePackages = "com.equipodinamita")
public class ConsumoDiarioServiceTest {

    public static void main(String[] args) {
        SpringApplication.run(ConsumoDiarioServiceTest.class, args);
    }

    @Bean
    CommandLineRunner ejecutarPrueba(ConsumoDiarioService consumoDiarioService, 
                                      RegistroConsumoRepository registroConsumoRepository) {
        return args -> {
            System.out.println();
            System.out.println("=".repeat(70));
            System.out.println("    PRUEBA DEL SERVICIO DE CONSUMO DIARIO - DATOS REALES");
            System.out.println("=".repeat(70));
            System.out.println();

            // Obtener todos los registros para mostrar
            List<RegistroConsumo> registros = registroConsumoRepository.findAll();

            if (registros.isEmpty()) {
                System.out.println("⚠️  No hay registros de consumo en la base de datos.");
                System.out.println("    Registre algunos alimentos primero para ver los promedios.");
                System.out.println("=".repeat(70));
                return;
            }

            // Mostrar los registros encontrados
            mostrarRegistrosEncontrados(registros);

            // Usar el servicio para calcular y mostrar promedios
            mostrarPromedioGeneral(consumoDiarioService);
            mostrarPromedioPorHorario(consumoDiarioService);
            mostrarTotalDelDia(consumoDiarioService);
        };
    }

    /**
     * Muestra los registros de consumo encontrados en la base de datos.
     */
    private void mostrarRegistrosEncontrados(List<RegistroConsumo> registros) {
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
     * Muestra el promedio general de todos los registros usando el servicio.
     */
    private void mostrarPromedioGeneral(ConsumoDiarioService service) {
        ConsumoDiario promedio = service.calcularPromedioConsumoGeneral();

        System.out.println(">>> PROMEDIO GENERAL DE CONSUMO (por registro):");
        System.out.println("-".repeat(70));
        System.out.printf("  Promedio de Calorías:      %10.2f kcal%n", promedio.getCalorias());
        System.out.printf("  Promedio de Proteínas:     %10.2f g%n", promedio.getProteinas());
        System.out.printf("  Promedio de Carbohidratos: %10.2f g%n", promedio.getCarbohidratos());
        System.out.printf("  Promedio de Grasas:        %10.2f g%n", promedio.getGrasas());
        System.out.printf("  Total de registros:        %10d%n", promedio.getTotalRegistros());
        System.out.println();
    }

    /**
     * Muestra el promedio por cada tipo de comida (horario alimenticio).
     */
    private void mostrarPromedioPorHorario(ConsumoDiarioService service) {
        System.out.println(">>> PROMEDIO POR TIPO DE COMIDA:");
        System.out.println("-".repeat(70));

        Map<HorarioAlimenticioEnum, ConsumoDiario> promedios = service.calcularPromedioPorCadaHorario();

        for (HorarioAlimenticioEnum horario : HorarioAlimenticioEnum.values()) {
            ConsumoDiario promedio = promedios.get(horario);
            
            if (promedio != null && promedio.getTotalRegistros() > 0) {
                System.out.printf("%n  [%s] (%d registros):%n", horario.name(), promedio.getTotalRegistros());
                System.out.printf("    Promedio Calorías:      %8.2f kcal%n", promedio.getCalorias());
                System.out.printf("    Promedio Proteínas:     %8.2f g%n", promedio.getProteinas());
                System.out.printf("    Promedio Carbohidratos: %8.2f g%n", promedio.getCarbohidratos());
                System.out.printf("    Promedio Grasas:        %8.2f g%n", promedio.getGrasas());
            }
        }
        System.out.println();
    }

    /**
     * Muestra el total del día (suma de todos los consumos).
     */
    private void mostrarTotalDelDia(ConsumoDiarioService service) {
        ConsumoDiario total = service.calcularTotalConsumoDiario();

        System.out.println(">>> TOTAL DEL DÍA (suma de todos los consumos):");
        System.out.println("-".repeat(70));
        System.out.printf("  Total de Calorías:      %10.2f kcal%n", total.getCalorias());
        System.out.printf("  Total de Proteínas:     %10.2f g%n", total.getProteinas());
        System.out.printf("  Total de Carbohidratos: %10.2f g%n", total.getCarbohidratos());
        System.out.printf("  Total de Grasas:        %10.2f g%n", total.getGrasas());
        System.out.printf("  Total de registros:     %10d%n", total.getTotalRegistros());
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println("                    FIN DE LA PRUEBA");
        System.out.println("=".repeat(70));
    }
}
