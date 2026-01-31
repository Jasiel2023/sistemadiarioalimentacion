package com.equipodinamita.base.ui.registro_consumo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import com.equipodinamita.base.ui.ViewToolbar;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("calendario-registro")
@PageTitle("Calendario Registro de Consumo")
@Menu(order = 2, icon = "vaadin:calendar", title = "Calendario")
public class ConsultaConsumoView extends VerticalLayout {

    private static final Locale LOCALE_ES = new Locale("es", "ES");
    private static final String[] DIAS_SEMANA = { "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom" };
    private static final String[] COLORES_SEMANA = {
            "#FFE0E0", "#E0FFE0", "#E0E0FF", "#FFFFE0", "#FFE0FF", "#E0FFFF"
    };

    private YearMonth mesActual;
    private VerticalLayout calendarioContainer;
    private H2 tituloMes;
    private Map<Integer, String> coloresSemanas;
    private Random random;

    public ConsultaConsumoView() {
        this.mesActual = YearMonth.now();
        this.random = new Random();
        this.coloresSemanas = new HashMap<>();

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        // Toolbar superior
        add(new ViewToolbar("Calendario de Consumo"));

        // Contenedor principal del calendario
        VerticalLayout mainContainer = new VerticalLayout();
        mainContainer.setSizeFull();
        mainContainer.setPadding(true);
        mainContainer.setSpacing(true);
        mainContainer.setAlignItems(FlexComponent.Alignment.CENTER);

        // Navegación del mes
        HorizontalLayout navegacion = crearNavegacionMes();
        mainContainer.add(navegacion);

        // Contenedor del calendario
        calendarioContainer = new VerticalLayout();
        calendarioContainer.setWidthFull();
        calendarioContainer.setMaxWidth("900px");
        calendarioContainer.setPadding(false);
        calendarioContainer.setSpacing(false);
        calendarioContainer.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.15)")
                .set("overflow", "hidden");

        construirCalendario();

        mainContainer.add(calendarioContainer);
        add(mainContainer);
    }

    private HorizontalLayout crearNavegacionMes() {
        HorizontalLayout nav = new HorizontalLayout();
        nav.setWidthFull();
        nav.setMaxWidth("900px");
        nav.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        nav.setAlignItems(FlexComponent.Alignment.CENTER);
        nav.setPadding(true);

        Button btnAnterior = new Button(new Icon(VaadinIcon.CHEVRON_LEFT), e -> {
            mesActual = mesActual.minusMonths(1);
            generarColoresSemanas();
            construirCalendario();
        });
        btnAnterior.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnAnterior.getStyle()
                .set("background-color", "#6a419d")
                .set("color", "white")
                .set("border-radius", "50%");

        tituloMes = new H2(obtenerTituloMes());
        tituloMes.getStyle()
                .set("margin", "0")
                .set("color", "#333")
                .set("text-transform", "capitalize");

        Button btnSiguiente = new Button(new Icon(VaadinIcon.CHEVRON_RIGHT), e -> {
            mesActual = mesActual.plusMonths(1);
            generarColoresSemanas();
            construirCalendario();
        });
        btnSiguiente.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnSiguiente.getStyle()
                .set("background-color", "#6a419d")
                .set("color", "white")
                .set("border-radius", "50%");

        Button btnHoy = new Button("Hoy", e -> {
            mesActual = YearMonth.now();
            generarColoresSemanas();
            construirCalendario();
        });
        btnHoy.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnHoy.getStyle()
                .set("background-color", "#4CAF50")
                .set("border-radius", "8px");

        HorizontalLayout botonesNav = new HorizontalLayout(btnAnterior, btnHoy, btnSiguiente);
        botonesNav.setSpacing(true);

        nav.add(tituloMes, botonesNav);
        return nav;
    }

    private String obtenerTituloMes() {
        String nombreMes = mesActual.getMonth().getDisplayName(TextStyle.FULL, LOCALE_ES);
        return nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1) + " " + mesActual.getYear();
    }

    private void generarColoresSemanas() {
        coloresSemanas.clear();
        LocalDate primerDia = mesActual.atDay(1);
        LocalDate ultimoDia = mesActual.atEndOfMonth();

        int semanaActual = 0;
        LocalDate fecha = primerDia;

        while (!fecha.isAfter(ultimoDia)) {
            if (fecha.getDayOfWeek() == DayOfWeek.MONDAY || fecha.equals(primerDia)) {
                coloresSemanas.put(semanaActual, COLORES_SEMANA[random.nextInt(COLORES_SEMANA.length)]);
                semanaActual++;
            }
            fecha = fecha.plusDays(1);
        }
    }

    private void construirCalendario() {
        calendarioContainer.removeAll();
        tituloMes.setText(obtenerTituloMes());

        if (coloresSemanas.isEmpty()) {
            generarColoresSemanas();
        }

        // Encabezado con días de la semana
        HorizontalLayout headerDias = new HorizontalLayout();
        headerDias.setWidthFull();
        headerDias.setSpacing(false);
        headerDias.setPadding(true);
        headerDias.getStyle()
                .set("background-color", "#6a419d")
                .set("padding", "15px 10px");

        for (String dia : DIAS_SEMANA) {
            Div diaHeader = new Div();
            diaHeader.setText(dia);
            diaHeader.getStyle()
                    .set("flex", "1")
                    .set("text-align", "center")
                    .set("font-weight", "bold")
                    .set("color", "white")
                    .set("font-size", "14px");
            headerDias.add(diaHeader);
        }

        calendarioContainer.add(headerDias);

        // Contenedor de semanas
        VerticalLayout semanasContainer = new VerticalLayout();
        semanasContainer.setWidthFull();
        semanasContainer.setPadding(false);
        semanasContainer.setSpacing(false);

        LocalDate primerDiaMes = mesActual.atDay(1);
        LocalDate ultimoDiaMes = mesActual.atEndOfMonth();

        // Ajustar al lunes de la primera semana
        int diasHastaLunes = primerDiaMes.getDayOfWeek().getValue() - 1;
        LocalDate inicioSemana = primerDiaMes.minusDays(diasHastaLunes);

        int numeroSemana = 0;
        LocalDate hoy = LocalDate.now();

        while (inicioSemana.isBefore(ultimoDiaMes) || inicioSemana.equals(ultimoDiaMes)) {
            HorizontalLayout semanaRow = new HorizontalLayout();
            semanaRow.setWidthFull();
            semanaRow.setSpacing(false);
            semanaRow.setPadding(false);

            String colorSemana = coloresSemanas.getOrDefault(numeroSemana, COLORES_SEMANA[0]);

            for (int i = 0; i < 7; i++) {
                LocalDate diaActual = inicioSemana.plusDays(i);
                Div diaCell = crearCeldaDia(diaActual, primerDiaMes, ultimoDiaMes, hoy, colorSemana);
                semanaRow.add(diaCell);
            }

            // Indicador de número de semana
            Span numeroSemanaLabel = new Span("S" + (numeroSemana + 1));
            numeroSemanaLabel.getStyle()
                    .set("position", "absolute")
                    .set("left", "5px")
                    .set("top", "50%")
                    .set("transform", "translateY(-50%)")
                    .set("font-size", "10px")
                    .set("color", "#999")
                    .set("font-weight", "bold");

            Div semanaWrapper = new Div();
            semanaWrapper.getStyle()
                    .set("position", "relative")
                    .set("width", "100%")
                    .set("background-color", colorSemana)
                    .set("border-bottom", "1px solid #e0e0e0");
            semanaWrapper.add(semanaRow);

            semanasContainer.add(semanaWrapper);

            inicioSemana = inicioSemana.plusWeeks(1);
            numeroSemana++;

            // Salir si ya pasamos el mes
            if (inicioSemana.getMonthValue() != mesActual.getMonthValue() &&
                    inicioSemana.isAfter(ultimoDiaMes)) {
                break;
            }
        }

        calendarioContainer.add(semanasContainer);

        // Leyenda de colores
        HorizontalLayout leyenda = crearLeyenda();
        calendarioContainer.add(leyenda);
    }

    private Div crearCeldaDia(LocalDate dia, LocalDate primerDia, LocalDate ultimoDia,
            LocalDate hoy, String colorSemana) {
        Div cell = new Div();
        cell.getStyle()
                .set("flex", "1")
                .set("min-height", "80px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("justify-content", "flex-start")
                .set("padding", "8px")
                .set("border-right", "1px solid #e0e0e0")
                .set("cursor", "pointer")
                .set("transition", "all 0.2s ease");

        Span numeroSpan = new Span(String.valueOf(dia.getDayOfMonth()));
        numeroSpan.getStyle()
                .set("font-size", "18px")
                .set("font-weight", "bold");

        // Día fuera del mes actual
        if (dia.isBefore(primerDia) || dia.isAfter(ultimoDia)) {
            cell.getStyle()
                    .set("background-color", "#f5f5f5")
                    .set("opacity", "0.5");
            numeroSpan.getStyle().set("color", "#999");
        }
        // Día actual (hoy)
        else if (dia.equals(hoy)) {
            cell.getStyle()
                    .set("background-color", "#6a419d");
            numeroSpan.getStyle()
                    .set("color", "white")
                    .set("background-color", "#4CAF50")
                    .set("border-radius", "50%")
                    .set("width", "35px")
                    .set("height", "35px")
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("justify-content", "center");
            
            // Navegar a Registro de Consumo al hacer clic en "Hoy"
            cell.addClickListener(e -> {
                UI.getCurrent().navigate(RegistroConsumoListView.class);
            });
            cell.getStyle().set("cursor", "pointer");
            cell.setTitle("Clic para ir a Registro de Consumo");
        }
        // Fin de semana
        else if (dia.getDayOfWeek() == DayOfWeek.SATURDAY || dia.getDayOfWeek() == DayOfWeek.SUNDAY) {
            numeroSpan.getStyle().set("color", "#e74c3c");
        }
        // Día normal
        else {
            numeroSpan.getStyle().set("color", "#333");
        }

        // Nombre del día de la semana (abreviado)
        Span nombreDia = new Span(dia.getDayOfWeek().getDisplayName(TextStyle.SHORT, LOCALE_ES));
        nombreDia.getStyle()
                .set("font-size", "10px")
                .set("color", "#666")
                .set("margin-top", "4px");

        cell.add(numeroSpan, nombreDia);

        // Efecto hover
        cell.getElement().addEventListener("mouseover", e -> {
            cell.getStyle().set("background-color", "#e8e0f0");
        });
        cell.getElement().addEventListener("mouseout", e -> {
            if (!dia.equals(hoy)) {
                if (dia.isBefore(primerDia) || dia.isAfter(ultimoDia)) {
                    cell.getStyle().set("background-color", "#f5f5f5");
                } else {
                    cell.getStyle().remove("background-color");
                }
            } else {
                cell.getStyle().set("background-color", "#6a419d");
            }
        });

        return cell;
    }

    private HorizontalLayout crearLeyenda() {
        HorizontalLayout leyenda = new HorizontalLayout();
        leyenda.setWidthFull();
        leyenda.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        leyenda.setAlignItems(FlexComponent.Alignment.CENTER);
        leyenda.setPadding(true);
        leyenda.setSpacing(true);
        leyenda.getStyle()
                .set("background-color", "#f9f9f9")
                .set("border-top", "1px solid #e0e0e0");

        // Leyenda: Hoy
        Div hoyIndicador = new Div();
        hoyIndicador.getStyle()
                .set("width", "20px")
                .set("height", "20px")
                .set("background-color", "#4CAF50")
                .set("border-radius", "50%");
        Span hoyLabel = new Span("Hoy");
        hoyLabel.getStyle().set("margin-left", "5px").set("font-size", "12px");
        HorizontalLayout hoyLeyenda = new HorizontalLayout(hoyIndicador, hoyLabel);
        hoyLeyenda.setAlignItems(FlexComponent.Alignment.CENTER);

        // Leyenda: Fin de semana
        Span finSemanaIndicador = new Span("15");
        finSemanaIndicador.getStyle()
                .set("color", "#e74c3c")
                .set("font-weight", "bold")
                .set("font-size", "14px");
        Span finSemanaLabel = new Span("Fin de semana");
        finSemanaLabel.getStyle().set("margin-left", "5px").set("font-size", "12px");
        HorizontalLayout finSemanaLeyenda = new HorizontalLayout(finSemanaIndicador, finSemanaLabel);
        finSemanaLeyenda.setAlignItems(FlexComponent.Alignment.CENTER);

        // Leyenda: Colores de semana
        Span coloresLabel = new Span("🎨 Cada semana tiene un color diferente");
        coloresLabel.getStyle().set("font-size", "12px").set("color", "#666");

        leyenda.add(hoyLeyenda, finSemanaLeyenda, coloresLabel);
        return leyenda;
    }
}
