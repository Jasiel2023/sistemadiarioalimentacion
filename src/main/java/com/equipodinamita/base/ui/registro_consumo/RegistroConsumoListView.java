package com.equipodinamita.base.ui.registro_consumo;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.equipodinamita.base.Service.AlimentoService;
import com.equipodinamita.base.Service.ConsumoDiarioService;
import com.equipodinamita.base.Service.RegistroConsumoService;
import com.equipodinamita.base.models.Alimento;
import com.equipodinamita.base.models.ConsumoDiario;
import com.equipodinamita.base.models.Cuenta;
import com.equipodinamita.base.models.HorarioAlimenticioEnum;
import com.equipodinamita.base.models.RegistroConsumo;
import com.equipodinamita.base.ui.ViewToolbar;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("registro-consumo")
@PageTitle("Registro de Consumo")
// @Menu(order = 1, icon = "vaadin:clipboard-text", title = "Registro de
// Consumo")
public class RegistroConsumoListView extends VerticalLayout implements BeforeEnterObserver {

    private static final Locale LOCALE_ES = new Locale("es", "ES");

    private final RegistroConsumoService registroService;
    private final AlimentoService alimentoService;
    private final ConsumoDiarioService consumoDiarioService;

    private final Grid<RegistroConsumo> grid;
    private final Button crearBtn;
    private VerticalLayout itemsContainer;
    private VerticalLayout itemsContainerAlmuerzo;
    private VerticalLayout itemsContainerCena;
    private VerticalLayout itemsContainerEntretiempo;

    // Fecha seleccionada para el registro (null = hoy)
    private LocalDate fechaSeleccionada;
    private Span tituloFechaSpan;

    // ConsumoDiario actual para la fecha seleccionada
    private ConsumoDiario consumoDiarioActual;

    // Método helper para obtener la cuenta del usuario actual
    private Cuenta getCuentaActual() {
        return VaadinSession.getCurrent().getAttribute(Cuenta.class);
    }

    // Método helper para obtener la fecha actual (seleccionada o hoy)
    private LocalDate getFechaActual() {
        return fechaSeleccionada != null ? fechaSeleccionada : LocalDate.now();
    }

    // Método helper para obtener o crear el ConsumoDiario de la fecha actual
    // IMPORTANTE: Siempre obtiene el ConsumoDiario basándose en la fecha actual,
    // sin usar cache para evitar datos obsoletos al cambiar de fecha
    private ConsumoDiario getConsumoDiarioActual() {
        Cuenta cuenta = getCuentaActual();
        if (cuenta != null) {
            LocalDate fecha = getFechaActual();
            // Siempre obtener/crear el ConsumoDiario para la fecha correcta
            consumoDiarioActual = consumoDiarioService.obtenerOCrearConsumoDiario(cuenta, fecha);
        }
        return consumoDiarioActual;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Obtener el parámetro de fecha de la URL
        List<String> fechaParams = event.getLocation().getQueryParameters().getParameters().get("fecha");

        // Resetear el consumoDiario cuando se navega
        consumoDiarioActual = null;

        if (fechaParams != null && !fechaParams.isEmpty()) {
            try {
                fechaSeleccionada = LocalDate.parse(fechaParams.get(0));
            } catch (Exception e) {
                fechaSeleccionada = null; // Si hay error, usar fecha de hoy
            }
        } else {
            fechaSeleccionada = null; // Sin parámetro = hoy
        }

        // Actualizar título y cargar registros
        actualizarTituloFecha();
        refrescarTodosLosRegistros();
    }

    // Método para refrescar todos los registros de todas las tarjetas
    private void refrescarTodosLosRegistros() {
        if (itemsContainer != null) {
            refreshBreakfastItems();
        }
        if (itemsContainerAlmuerzo != null) {
            refreshAlmuerzoItems();
        }
        if (itemsContainerCena != null) {
            refreshCenaItems();
        }
        if (itemsContainerEntretiempo != null) {
            refreshEntretiempoItems();
        }
    }

    /**
     * Valida si el consumo del día actual requiere ser guardado antes de navegar al
     * calendario.
     * Solo muestra el diálogo si hay alimentos registrados sin guardar.
     */
    private void validarYNavegarACalendario() {
        Cuenta cuenta = getCuentaActual();
        if (cuenta == null) {
            Notification.show("Error: No hay usuario autenticado", 3000, Notification.Position.MIDDLE);
            return;
        }

        // Obtener el ConsumoDiario actual y verificar directamente si tiene registros
        // sin guardar
        ConsumoDiario consumoDiario = getConsumoDiarioActual();

        // Verificar si hay registros en cualquier horario
        boolean tieneRegistros = false;
        if (consumoDiario != null) {
            tieneRegistros = !registroService
                    .findByConsumoDiarioAndHorario(consumoDiario, HorarioAlimenticioEnum.DESAYUNO).isEmpty() ||
                    !registroService.findByConsumoDiarioAndHorario(consumoDiario, HorarioAlimenticioEnum.ALMUERZO)
                            .isEmpty()
                    ||
                    !registroService.findByConsumoDiarioAndHorario(consumoDiario, HorarioAlimenticioEnum.CENA).isEmpty()
                    ||
                    !registroService.findByConsumoDiarioAndHorario(consumoDiario, HorarioAlimenticioEnum.ENTRETIEMPOS)
                            .isEmpty();
        }

        boolean yaGuardado = consumoDiario != null && consumoDiario.isGuardado();
        boolean requiereGuardar = tieneRegistros && !yaGuardado;

        if (requiereGuardar) {
            // Mostrar diálogo obligatorio
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("⚠️ Debes guardar tu consumo primero");

            LocalDate fechaActual = getFechaActual();
            String fechaTexto = fechaActual.equals(LocalDate.now()) ? "de hoy"
                    : "del " + fechaActual.getDayOfMonth() + " de " +
                            fechaActual.getMonth().getDisplayName(TextStyle.FULL, LOCALE_ES);

            dialog.setText("Tienes alimentos registrados " + fechaTexto + " que aún no has guardado. " +
                    "¿Qué deseas hacer?");

            dialog.setConfirmText("Guardar Consumo");
            dialog.setConfirmButtonTheme("primary success");
            dialog.addConfirmListener(event -> {
                // Guardar el consumo automáticamente
                LocalDate fecha = getFechaActual();
                consumoDiarioService.guardarConsumoDiario(cuenta, fecha);
                Notification.show("✅ Consumo guardado correctamente", 3000, Notification.Position.BOTTOM_CENTER);
                // Navegar al calendario después de guardar
                UI.getCurrent().navigate("calendario-registro");
            });

            // Botón de "No guardar" - descarta cambios y navega al calendario
            dialog.setRejectable(true);
            dialog.setRejectText("No guardar");
            dialog.addRejectListener(event -> {
                // Navegar al calendario sin guardar
                UI.getCurrent().navigate("calendario-registro");
            });

            // Botón de cancelar para cerrar el diálogo y quedarse
            dialog.setCancelable(true);
            dialog.setCancelText("Cancelar");
            dialog.setCloseOnEsc(true);

            dialog.open();
        } else {
            // No hay registros pendientes de guardar, permitir navegar
            UI.getCurrent().navigate("calendario-registro");
        }
    }

    private void actualizarTituloFecha() {
        if (tituloFechaSpan != null && fechaSeleccionada != null) {
            String nombreDia = fechaSeleccionada.getDayOfWeek().getDisplayName(TextStyle.FULL, LOCALE_ES);
            String nombreMes = fechaSeleccionada.getMonth().getDisplayName(TextStyle.FULL, LOCALE_ES);
            tituloFechaSpan.setText(String.format("📅 %s, %d de %s de %d",
                    nombreDia.substring(0, 1).toUpperCase() + nombreDia.substring(1),
                    fechaSeleccionada.getDayOfMonth(),
                    nombreMes,
                    fechaSeleccionada.getYear()));
            tituloFechaSpan.setVisible(true);
        } else if (tituloFechaSpan != null) {
            tituloFechaSpan.setVisible(false);
        }
    }

    public RegistroConsumoListView(
            RegistroConsumoService registroService,
            AlimentoService alimentoService,
            ConsumoDiarioService consumoDiarioService) {

        this.registroService = registroService;
        this.alimentoService = alimentoService;
        this.consumoDiarioService = consumoDiarioService;

        crearBtn = new Button("Crear registro", e -> openCreateDialog());
        crearBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        grid = new Grid<>();
        grid.setItems(query -> registroService.list(getCuentaActual(), toSpringPageRequest(query)).stream());

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("overflow-y", "auto"); // Permitir scroll vertical

        // Contenedor vertical para organizar filas de cuadros
        VerticalLayout cardsWrapper = new VerticalLayout();
        cardsWrapper.setWidthFull();
        cardsWrapper.setPadding(true);
        cardsWrapper.setSpacing(true);

        // Primera fila: dos cuadros
        HorizontalLayout firstRow = new HorizontalLayout();
        firstRow.setWidthFull();
        firstRow.setSpacing(true);
        firstRow.add(createBreakfastCard(), createMeriendaCard());

        // Segunda fila: dos cuadros
        HorizontalLayout secondRow = new HorizontalLayout();
        secondRow.setWidthFull();
        secondRow.setSpacing(true);
        secondRow.add(createAlmuerzoCard(), createEntretiempoCard());

        cardsWrapper.add(firstRow, secondRow);

        // --- BOTÓN Consultar Consumo en la parte inferior ---
        HorizontalLayout bottomButtonContainer = new HorizontalLayout();
        bottomButtonContainer.setWidthFull();
        bottomButtonContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        bottomButtonContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        bottomButtonContainer.getStyle().set("margin-top", "auto").set("padding", "20px");

        // Contenedor de los tres botones (inicialmente oculto)
        HorizontalLayout botonesOpciones = new HorizontalLayout();
        botonesOpciones.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        botonesOpciones.setAlignItems(FlexComponent.Alignment.CENTER);
        botonesOpciones.setSpacing(true);
        botonesOpciones.setVisible(false);

        Button btnDiario = new Button("Resumen de hoy", new Icon(VaadinIcon.SUN_DOWN), e -> {
            openConsumoDiarioDialog();
        });
        btnDiario.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnDiario.getStyle()
                .set("background-color", "#4CAF50")
                .set("color", "#ffffff")
                .set("border-radius", "8px")
                .set("padding", "10px 20px");

        Button btnCalendario = new Button("Calendario", new Icon(VaadinIcon.CALENDAR), e -> {
            validarYNavegarACalendario();
        });
        btnCalendario.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnCalendario.getStyle()
                .set("background-color", "#2196F3")
                .set("color", "#ffffff")
                .set("border-radius", "8px")
                .set("padding", "10px 20px");

        Button btnAlimentos = new Button("Lista de alimentos", new Icon(VaadinIcon.LIST), e -> {
            UI.getCurrent().navigate("alimentos_usuario");
        });
        btnAlimentos.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAlimentos.getStyle()
                .set("background-color", "#e00fe3")
                .set("color", "#ffffff")
                .set("border-radius", "8px")
                .set("padding", "10px 20px");

        botonesOpciones.add(btnDiario, btnCalendario, btnAlimentos);

        // Botón principal "Consultar consumo"
        Button btnConsultarConsumo = new Button("Consultar consumo", new Icon(VaadinIcon.SEARCH), e -> {
            botonesOpciones.setVisible(!botonesOpciones.isVisible());
        });
        btnConsultarConsumo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnConsultarConsumo.getStyle()
                .set("background-color", "#6a419d")
                .set("color", "#ffffff")
                .set("border-radius", "8px")
                .set("padding", "12px 24px")
                .set("font-size", "16px");

        VerticalLayout buttonSection = new VerticalLayout();
        buttonSection.setWidthFull();
        buttonSection.setAlignItems(FlexComponent.Alignment.CENTER);
        buttonSection.setSpacing(true);
        buttonSection.add(btnConsultarConsumo, botonesOpciones);

        bottomButtonContainer.add(buttonSection);

        // Span para mostrar la fecha seleccionada (si es diferente a hoy)
        tituloFechaSpan = new Span();
        tituloFechaSpan.getStyle()
                .set("font-size", "16px")
                .set("font-weight", "bold")
                .set("color", "#6a419d")
                .set("background-color", "#f0e6ff")
                .set("padding", "8px 16px")
                .set("border-radius", "8px");
        tituloFechaSpan.setVisible(false); // Inicialmente oculto, se muestra si hay fecha seleccionada

        // Botón "Guardar Consumo" para la barra superior
        Button btnGuardarConsumo = new Button("Guardar consumo", new Icon(VaadinIcon.CHECK), e -> {
            guardarConsumoDiario();
        });
        btnGuardarConsumo.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnGuardarConsumo.getStyle()
                .set("background-color", "#4CAF50")
                .set("color", "#ffffff");

        // Contenedor para la fecha seleccionada debajo del toolbar
        HorizontalLayout fechaContainer = new HorizontalLayout(tituloFechaSpan);
        fechaContainer.setWidthFull();
        fechaContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        fechaContainer.setPadding(true);
        fechaContainer.setVisible(false);

        // Actualizar visibilidad del contenedor cuando cambie la fecha
        tituloFechaSpan.addAttachListener(e -> {
            fechaContainer.setVisible(tituloFechaSpan.isVisible());
        });

        add(
                new ViewToolbar("Registro de consumo", btnGuardarConsumo),
                fechaContainer,
                cardsWrapper,
                bottomButtonContainer);
    }

    /**
     * Guarda el consumo diario del usuario actual para la fecha seleccionada
     */
    private void guardarConsumoDiario() {
        Cuenta cuentaActual = getCuentaActual();

        if (cuentaActual == null) {
            Notification.show("Error: No hay usuario autenticado", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            // Usar la fecha actual (seleccionada o hoy)
            consumoDiarioService.guardarConsumoDiario(cuentaActual, getFechaActual());
            Notification.show("✅ Consumo diario guardado correctamente", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception ex) {
            Notification.show("❌ Error al guardar: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void openConsumoDiarioDialog() {
        Dialog dialog = new Dialog();

        // Título dinámico
        LocalDate fecha = getFechaActual();
        String tituloFecha = fecha.equals(LocalDate.now()) ? "de hoy"
                : "del " + fecha.getDayOfMonth() + " de " + fecha.getMonth().getDisplayName(TextStyle.FULL, LOCALE_ES);

        // --- CONTENEDOR DE CABECERA MODERNO ---
        HorizontalLayout headerContainer = new HorizontalLayout();
        headerContainer.setWidthFull();
        headerContainer.setPadding(true);
        headerContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        headerContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // Estilo de Graduado y Sombra Profunda
        headerContainer.getStyle()
                .set("background", "linear-gradient(135deg, #1a73e8 0%, #0d47a1 100%)") // Gradiente dinámico
                .set("border-radius", "12px 12px 0 0") // Bordes más suaves
                .set("box-shadow", "0 4px 15px rgba(0, 0, 0, 0.2)") // Sombra más realista
                .set("padding", "20px 30px")
                .set("border-bottom", "3px solid #ffca28"); // Línea de acento inferior (opcional, un toque de
                                                            // contraste)

        // Texto con tipografía limpia
        H3 title = new H3("Resumen de consumo " + tituloFecha);
        title.getStyle()
                .set("color", "white")
                .set("margin", "0")
                .set("font-weight", "600")
                .set("letter-spacing", "0.5px")
                .set("text-shadow", "1px 1px 2px rgba(0,0,0,0.2)")
                .set("align-self", "center")
                .set("display", "inline-block");

        headerContainer.add(title);

        // --- CONFIGURACIÓN DEL DIÁLOGO ---
        dialog.setWidth("900px");
        dialog.setHeight("700px");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        // --- CONTENIDO PRINCIPAL ---
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);
        content.getStyle()
                .set("background-color", "#f8f9fa") // Fondo gris muy claro para resaltar el blanco de las tarjetas
                                                    // internas
                .set("border-radius", "0 0 12px 12px");

        // Armar la estructura
        dialog.add(headerContainer, content);

        // Calcular totales SOLO de los registros de esa fecha
        ConsumoDiario totalDiario = consumoDiarioService.calcularTotalConsumoDiarioPorFecha(consumoDiarioActual);

        if (totalDiario.getTotalRegistros() == 0) {
            content.add(new H3("⚠️ No hay registros de consumo para " + tituloFecha.toLowerCase()));

            Button cerrarBtn = new Button("Cerrar", e -> dialog.close());
            cerrarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            dialog.add(content);
            dialog.getFooter().add(cerrarBtn);
            dialog.open();
            return;
        }

        // === SECCIÓN: RESUMEN TOTAL ===
        VerticalLayout resumenSection = new VerticalLayout();
        resumenSection.setPadding(true);
        resumenSection.getStyle()
                .set("background", "linear-gradient(145deg, #ffffff 0%, #f0f4f8 100%)")
                .set("border-radius", "16px")
                .set("margin-bottom", "20px")
                .set("box-shadow", "0 8px 32px rgba(0, 0, 0, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04)")
                .set("border", "1px solid rgba(255, 255, 255, 0.8)")
                .set("backdrop-filter", "blur(10px)")
                .set("transition", "transform 0.3s ease, box-shadow 0.3s ease");

        H3 tituloResumen = new H3("Resumen total " + tituloFecha);
        tituloResumen.getStyle()
                .set("margin", "0 0 15px 0")
                .set("color", "#1a1a2e")
                .set("font-weight", "700")
                .set("font-size", "1.3rem")
                .set("letter-spacing", "0.5px");

        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setWidthFull();
        statsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.AROUND);

        statsLayout.add(
                createStatCard("Calorías", String.format("%.1f kcal", totalDiario.getCalorias()), "#FF6B6B"),
                createStatCard("Proteínas", String.format("%.1f g", totalDiario.getProteinas()), "#4ECDC4"),
                createStatCard("Carbohidratos", String.format("%.1f g", totalDiario.getCarbohidratos()), "#FFE66D"),
                createStatCard("Grasas", String.format("%.1f g", totalDiario.getGrasas()), "#95E1D3"));

        Span totalAlimentos = new Span("Total de alimentos registrados: " + totalDiario.getTotalRegistros());
        totalAlimentos.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "700")
                .set("margin-top", "18px")
                .set("color", "#ffffff")
                .set("background",
                        "linear-gradient(135deg, rgba(10, 156, 167, 0.9) 0%, rgba(7, 116, 224, 0.9) 100%)")
                .set("padding", "12px 20px")
                .set("border-radius", "10px")
                .set("box-shadow", "0 2px 8px rgba(0, 0, 0, 0.08)")
                .set("border", "1px solid rgba(0, 0, 0, 0.05)")
                .set("align-self", "center")
                .set("display", "inline-block");

        resumenSection.add(tituloResumen, statsLayout, totalAlimentos);
        content.add(resumenSection);

        // === SECCIÓN: CONSUMO POR HORARIO (de la fecha actual) ===
        Map<HorarioAlimenticioEnum, ConsumoDiario> consumoPorHorario = consumoDiarioService
                .calcularConsumoPorCadaHorarioYFecha(consumoDiarioActual);

        VerticalLayout horarioSection = new VerticalLayout();
        horarioSection.setPadding(true);
        horarioSection.getStyle()
                .set("background", "linear-gradient(145deg, #e3f2fd 0%, #bbdefb 50%, #e1f5fe 100%)")
                .set("border-radius", "16px")
                .set("margin-bottom", "20px")
                .set("box-shadow", "0 8px 32px rgba(33, 150, 243, 0.15), 0 2px 8px rgba(0, 0, 0, 0.05)")
                .set("border", "1px solid rgba(255, 255, 255, 0.6)")
                .set("backdrop-filter", "blur(10px)")
                .set("transition", "transform 0.3s ease, box-shadow 0.3s ease");

        H3 tituloHorarios = new H3("Consumo por tipo de comida " + tituloFecha);
        tituloHorarios.getStyle()
                .set("margin", "0 0 15px 0")
                .set("color", "#0d47a1")
                .set("font-weight", "700")
                .set("font-size", "1.3rem")
                .set("letter-spacing", "0.5px");
        horarioSection.add(tituloHorarios);

        HorizontalLayout horariosGrid = new HorizontalLayout();
        horariosGrid.setWidthFull();
        horariosGrid.setJustifyContentMode(FlexComponent.JustifyContentMode.AROUND);
        horariosGrid.getStyle().set("flex-wrap", "wrap");

        for (HorarioAlimenticioEnum horario : HorarioAlimenticioEnum.values()) {
            ConsumoDiario consumoHorario = consumoPorHorario.get(horario);
            if (consumoHorario != null && consumoHorario.getTotalRegistros() > 0) {
                horariosGrid.add(createHorarioCard(horario, consumoHorario));
            }
        }

        horarioSection.add(horariosGrid);

        // === GRÁFICO DE PASTEL: Distribución de calorías por tipo de comida ===
        VerticalLayout pieChartSection = createPieChartSection(consumoPorHorario, totalDiario.getCalorias());
        horarioSection.add(pieChartSection);

        content.add(horarioSection);

        // === Seccion del total de calorias de un dia===
        Span totalCaloriasDia = new Span(
                "Total de calorías " + tituloFecha + ": " + String.format("%.1f kcal", totalDiario.getCalorias()));
        totalCaloriasDia.getStyle()
                .set("font-size", "18px")
                .set("font-weight", "800")
                .set("margin", "15px 0")
                .set("color", "#ffffff")
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("padding", "16px 28px")
                .set("border-radius", "14px")
                .set("box-shadow", "0 8px 25px rgba(102, 126, 234, 0.4)")
                .set("letter-spacing", "0.5px")
                .set("text-shadow", "0 2px 4px rgba(0, 0, 0, 0.15)")
                .set("display", "inline-block")
                .set("transition", "transform 0.3s ease, box-shadow 0.3s ease");
        content.add(totalCaloriasDia);

        // === SECCIÓN: DETALLE DE ALIMENTOS ===
        VerticalLayout detalleSection = new VerticalLayout();
        detalleSection.setPadding(true);
        detalleSection.getStyle()
                .set("background", "linear-gradient(145deg, #fff8e1 0%, #ffecb3 50%, #ffe0b2 100%)")
                .set("border-radius", "16px")
                .set("box-shadow", "0 8px 32px rgba(255, 152, 0, 0.15), 0 2px 8px rgba(0, 0, 0, 0.05)")
                .set("border", "1px solid rgba(255, 255, 255, 0.6)")
                .set("backdrop-filter", "blur(10px)")
                .set("transition", "transform 0.3s ease, box-shadow 0.3s ease");

        H3 tituloDetalle = new H3("📝 Detalle de alimentos consumidos");
        tituloDetalle.getStyle()
                .set("margin", "0 0 15px 0")
                .set("color", "#e65100")
                .set("font-weight", "700")
                .set("font-size", "1.3rem")
                .set("letter-spacing", "0.5px");
        detalleSection.add(tituloDetalle);

        // Grid con los alimentos detallados
        Grid<RegistroConsumo> gridDetalle = new Grid<>();
        gridDetalle.setItems(totalDiario.getRegistros());
        gridDetalle.setHeight("250px");
        gridDetalle.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);

        gridDetalle.addColumn(r -> r.getAlimento() != null ? r.getAlimento().getNombre() : "N/A")
                .setHeader("🍴 Alimento")
                .setFlexGrow(2);

        gridDetalle.addColumn(r -> r.getHorarioAlimenticio() != null ? r.getHorarioAlimenticio().name() : "N/A")
                .setHeader("⏰ Horario")
                .setFlexGrow(1);

        gridDetalle.addColumn(r -> {
            if (r.getCantidad() != null && r.getAlimento() != null && r.getAlimento().getUnidadMedida() != null) {
                return String.format("%.1f %s", r.getCantidad(), r.getAlimento().getUnidadMedida().name());
            }
            return r.getCantidad() != null ? String.format("%.1f", r.getCantidad()) : "N/A";
        }).setHeader("📏 Cantidad").setFlexGrow(1);

        gridDetalle.addColumn(r -> {
            if (r.getAlimento() != null && r.getCantidad() != null) {
                Alimento a = r.getAlimento();
                float factor = a.getPorcionBase() != null && a.getPorcionBase() > 0
                        ? r.getCantidad() / a.getPorcionBase()
                        : 0;
                float calorias = a.getCalorias() != null ? a.getCalorias() * factor : 0;
                return String.format("%.1f kcal", calorias);
            }
            return "N/A";
        }).setHeader("🔥 Calorías").setFlexGrow(1);

        gridDetalle.addColumn(r -> {
            if (r.getAlimento() != null && r.getCantidad() != null) {
                Alimento a = r.getAlimento();
                float factor = a.getPorcionBase() != null && a.getPorcionBase() > 0
                        ? r.getCantidad() / a.getPorcionBase()
                        : 0;
                float proteinas = a.getProteinas() != null ? a.getProteinas() * factor : 0;
                return String.format("%.1f g", proteinas);
            }
            return "N/A";
        }).setHeader("🥩 Proteínas").setFlexGrow(1);

        gridDetalle.addColumn(r -> {
            if (r.getAlimento() != null && r.getCantidad() != null) {
                Alimento a = r.getAlimento();
                float factor = a.getPorcionBase() != null && a.getPorcionBase() > 0
                        ? r.getCantidad() / a.getPorcionBase()
                        : 0;
                float carbos = a.getCarbohidratos() != null ? a.getCarbohidratos() * factor : 0;
                return String.format("%.1f g", carbos);
            }
            return "N/A";
        }).setHeader("🍞 Carbos").setFlexGrow(1);

        gridDetalle.addColumn(r -> {
            if (r.getAlimento() != null && r.getCantidad() != null) {
                Alimento a = r.getAlimento();
                float factor = a.getPorcionBase() != null && a.getPorcionBase() > 0
                        ? r.getCantidad() / a.getPorcionBase()
                        : 0;
                float grasas = a.getGrasas() != null ? a.getGrasas() * factor : 0;
                return String.format("%.1f g", grasas);
            }
            return "N/A";
        }).setHeader("🧈 Grasas").setFlexGrow(1);

        detalleSection.add(gridDetalle);
        content.add(detalleSection);

        // Botón cerrar
        Button cerrarBtn = new Button("Cerrar", e -> dialog.close());
        cerrarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(content);
        dialog.getFooter().add(cerrarBtn);
        dialog.open();
    }

    private VerticalLayout createStatCard(String titulo, String valor, String color) {
        VerticalLayout card = new VerticalLayout();
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.setPadding(true);

        // Crear gradiente dinámico basado en el color
        String gradientColor = createGradientFromColor(color);

        card.getStyle()
                .set("background", gradientColor)
                .set("border-radius", "16px")
                .set("min-width", "150px")
                .set("color", "#fff")
                .set("box-shadow", "0 10px 30px " + hexToRgba(color, 0.4) + ", 0 4px 12px rgba(0, 0, 0, 0.1)")
                .set("border", "1px solid rgba(255, 255, 255, 0.3)")
                .set("backdrop-filter", "blur(10px)")
                .set("transition", "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)")
                .set("cursor", "pointer")
                .set("position", "relative")
                .set("overflow", "hidden");

        Span tituloSpan = new Span(titulo);
        tituloSpan.getStyle()
                .set("font-size", "13px")
                .set("font-weight", "600")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.5px")
                .set("opacity", "0.95");

        Span valorSpan = new Span(valor);
        valorSpan.getStyle()
                .set("font-size", "22px")
                .set("font-weight", "800")
                .set("text-shadow", "0 2px 4px rgba(0, 0, 0, 0.2)")
                .set("margin-top", "8px");

        card.add(tituloSpan, valorSpan);
        return card;
    }

    // Helper method para crear gradiente desde un color
    private String createGradientFromColor(String color) {
        return "linear-gradient(135deg, " + color + " 0%, " + darkenColor(color, 20) + " 100%)";
    }

    // Helper method para oscurecer un color hex
    private String darkenColor(String hexColor, int percent) {
        try {
            int r = Integer.parseInt(hexColor.substring(1, 3), 16);
            int g = Integer.parseInt(hexColor.substring(3, 5), 16);
            int b = Integer.parseInt(hexColor.substring(5, 7), 16);

            r = Math.max(0, r - (r * percent / 100));
            g = Math.max(0, g - (g * percent / 100));
            b = Math.max(0, b - (b * percent / 100));

            return String.format("#%02x%02x%02x", r, g, b);
        } catch (Exception e) {
            return hexColor;
        }
    }

    // Helper method para convertir hex a rgba
    private String hexToRgba(String hexColor, double alpha) {
        try {
            int r = Integer.parseInt(hexColor.substring(1, 3), 16);
            int g = Integer.parseInt(hexColor.substring(3, 5), 16);
            int b = Integer.parseInt(hexColor.substring(5, 7), 16);
            return String.format("rgba(%d, %d, %d, %.2f)", r, g, b, alpha);
        } catch (Exception e) {
            return "rgba(0, 0, 0, " + alpha + ")";
        }
    }

    private VerticalLayout createHorarioCard(HorarioAlimenticioEnum horario, ConsumoDiario consumo) {
        VerticalLayout card = new VerticalLayout();
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.setPadding(true);

        // Colores dinámicos según el horario
        String gradientColor = switch (horario) {
            case DESAYUNO -> "linear-gradient(145deg, #FF9800 0%, #F57C00 100%)";
            case ALMUERZO -> "linear-gradient(145deg, #4CAF50 0%, #388E3C 100%)";
            case CENA -> "linear-gradient(145deg, #3F51B5 0%, #303F9F 100%)";
            case ENTRETIEMPOS -> "linear-gradient(145deg, #9C27B0 0%, #7B1FA2 100%)";
        };

        String shadowColor = switch (horario) {
            case DESAYUNO -> "rgba(239, 151, 20, 0.9)";
            case ALMUERZO -> "rgb(43, 167, 47)";
            case CENA -> "rgba(63, 81, 181, 0.4)";
            case ENTRETIEMPOS -> "rgba(156, 39, 176, 0.4)";
        };

        card.getStyle()
                .set("background", gradientColor)
                .set("border-radius", "16px")
                .set("box-shadow", "0 8px 24px " + shadowColor + ", 0 4px 8px rgba(0,0,0,0.1)")
                .set("min-width", "180px")
                .set("margin", "8px")
                .set("border", "1px solid rgba(255, 255, 255, 0.3)")
                .set("backdrop-filter", "blur(10px)")
                .set("transition", "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)")
                .set("cursor", "pointer")
                .set("padding", "20px");

        Span tituloSpan = new Span(horario.name());
        tituloSpan.getStyle()
                .set("font-weight", "700")
                .set("font-size", "16px")
                .set("color", "white")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "1px")
                .set("text-shadow", "0 2px 4px rgba(0, 0, 0, 0.2)");

        Span registrosSpan = new Span(consumo.getTotalRegistros() + " alimentos");
        registrosSpan.getStyle()
                .set("font-size", "13px")
                .set("color", "rgba(255, 255, 255, 0.9)")
                .set("margin-top", "8px")
                .set("background", "rgba(255, 255, 255, 0.15)")
                .set("padding", "4px 12px")
                .set("border-radius", "20px");

        Span caloriasSpan = new Span(String.format("%.1f kcal", consumo.getCalorias()));
        caloriasSpan.getStyle()
                .set("font-size", "18px")
                .set("font-weight", "800")
                .set("color", "white")
                .set("margin-top", "10px")
                .set("text-shadow", "0 2px 4px rgba(0, 0, 0, 0.3)");

        card.add(tituloSpan, registrosSpan, caloriasSpan);
        return card;
    }

    /**
     * Crea la sección del gráfico de barras con la distribución de calorías por
     * tipo de comida
     */
    private VerticalLayout createPieChartSection(Map<HorarioAlimenticioEnum, ConsumoDiario> consumoPorHorario,
            float totalCalorias) {
        VerticalLayout section = new VerticalLayout();
        section.setAlignItems(FlexComponent.Alignment.CENTER);
        section.setPadding(true);
        section.setSpacing(true);
        section.getStyle()
                .set("margin-top", "20px")
                .set("background", "linear-gradient(145deg, #ffffff 0%, #f5f7fa 100%)")
                .set("border-radius", "16px")
                .set("padding", "24px")
                .set("box-shadow", "0 4px 20px rgba(0, 0, 0, 0.08), inset 0 1px 0 rgba(255, 255, 255, 0.6)")
                .set("border", "1px solid rgba(0, 0, 0, 0.05)")
                .set("transition", "transform 0.3s ease, box-shadow 0.3s ease");

        H3 tituloPie = new H3("Distribución de calorías por tipo de comida");
        tituloPie.getStyle()
                .set("margin", "0 0 20px 0")
                .set("color", "#1a1a2e")
                .set("font-weight", "700")
                .set("font-size", "1.2rem")
                .set("letter-spacing", "0.3px")
                .set("align-self", "center")
                .set("display", "inline-block");

        // Colores para cada tipo de comida
        String colorDesayuno = "#d46408";
        String colorAlmuerzo = "#4CAF50";
        String colorCena = "#2196F3";
        String colorEntretiempos = "#9C27B0";

        // Obtener calorías de cada horario
        float calDesayuno = consumoPorHorario.containsKey(HorarioAlimenticioEnum.DESAYUNO)
                ? consumoPorHorario.get(HorarioAlimenticioEnum.DESAYUNO).getCalorias()
                : 0;
        float calAlmuerzo = consumoPorHorario.containsKey(HorarioAlimenticioEnum.ALMUERZO)
                ? consumoPorHorario.get(HorarioAlimenticioEnum.ALMUERZO).getCalorias()
                : 0;
        float calCena = consumoPorHorario.containsKey(HorarioAlimenticioEnum.CENA)
                ? consumoPorHorario.get(HorarioAlimenticioEnum.CENA).getCalorias()
                : 0;
        float calEntretiempos = consumoPorHorario.containsKey(HorarioAlimenticioEnum.ENTRETIEMPOS)
                ? consumoPorHorario.get(HorarioAlimenticioEnum.ENTRETIEMPOS).getCalorias()
                : 0;

        // Calcular porcentajes
        float porcDesayuno = totalCalorias > 0 ? (calDesayuno / totalCalorias) * 100 : 0;
        float porcAlmuerzo = totalCalorias > 0 ? (calAlmuerzo / totalCalorias) * 100 : 0;
        float porcCena = totalCalorias > 0 ? (calCena / totalCalorias) * 100 : 0;
        float porcEntretiempos = totalCalorias > 0 ? (calEntretiempos / totalCalorias) * 100 : 0;

        // Contenedor del gráfico de barras
        VerticalLayout barChartContainer = new VerticalLayout();
        barChartContainer.setWidthFull();
        barChartContainer.setSpacing(true);
        barChartContainer.setPadding(false);
        barChartContainer.getStyle().set("max-width", "500px");

        // Crear barras para cada tipo de comida
        if (calDesayuno > 0) {
            barChartContainer.add(createBarItem("Desayuno", colorDesayuno, porcDesayuno, calDesayuno));
        }
        if (calAlmuerzo > 0) {
            barChartContainer.add(createBarItem("Almuerzo", colorAlmuerzo, porcAlmuerzo, calAlmuerzo));
        }
        if (calCena > 0) {
            barChartContainer.add(createBarItem("Cena", colorCena, porcCena, calCena));
        }
        if (calEntretiempos > 0) {
            barChartContainer
                    .add(createBarItem("Entretiempos", colorEntretiempos, porcEntretiempos, calEntretiempos));
        }

        // Determinar en qué tipo de comida se consumió más calorías
        String mensajeMayorConsumo = determinarMayorConsumo(calDesayuno, calAlmuerzo, calCena, calEntretiempos);

        Span mensajeSpan = new Span(mensajeMayorConsumo);
        mensajeSpan.getStyle()
                .set("font-size", "15px")
                .set("font-weight", "700")
                .set("color", "#ffffff")
                .set("background", "linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%)")
                .set("padding", "14px 24px")
                .set("border-radius", "12px")
                .set("margin-top", "20px")
                .set("text-align", "center")
                .set("box-shadow", "0 4px 15px rgba(165, 54, 54, 0.4)")
                .set("letter-spacing", "0.3px")
                .set("transition", "transform 0.3s ease, box-shadow 0.3s ease");

        section.add(tituloPie, barChartContainer, mensajeSpan);
        return section;
    }

    /**
     * Crea una barra del gráfico de barras
     */
    private VerticalLayout createBarItem(String nombre, String color, float porcentaje, float calorias) {
        VerticalLayout barContainer = new VerticalLayout();
        barContainer.setWidthFull();
        barContainer.setSpacing(false);
        barContainer.setPadding(false);
        barContainer.getStyle()
                .set("margin-bottom", "14px")
                .set("padding", "12px 16px")
                .set("background", "linear-gradient(145deg, #ffffff 0%, #f8f9fa 100%)")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 8px rgba(0, 0, 0, 0.06)")
                .set("transition", "transform 0.2s ease, box-shadow 0.2s ease");

        // Etiqueta con nombre y valores
        HorizontalLayout labelRow = new HorizontalLayout();
        labelRow.setWidthFull();
        labelRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        labelRow.setAlignItems(FlexComponent.Alignment.CENTER);

        Span nombreSpan = new Span(nombre);
        nombreSpan.getStyle()
                .set("font-weight", "700")
                .set("font-size", "14px")
                .set("color", "#2d3436");

        Span valorSpan = new Span(String.format("%.1f%% (%.0f kcal)", porcentaje, calorias));
        valorSpan.getStyle()
                .set("font-size", "13px")
                .set("font-weight", "600")
                .set("color", color)
                .set("background", hexToRgba(color, 0.1))
                .set("padding", "4px 10px")
                .set("border-radius", "20px");

        labelRow.add(nombreSpan, valorSpan);

        // Contenedor de la barra (fondo gris moderno)
        Div barBackground = new Div();
        barBackground.getStyle()
                .set("width", "100%")
                .set("height", "28px")
                .set("background", "linear-gradient(90deg, #e8e8e8 0%, #f0f0f053 100%)")
                .set("border-radius", "14px")
                .set("overflow", "hidden")
                .set("margin-top", "10px")
                .set("box-shadow", "inset 0 2px 4px rgba(0, 0, 0, 0.06)");

        // Crear gradiente dinámico para la barra
        String barGradient = "linear-gradient(90deg, " + color + " 0%, " + darkenColor(color, 15) + " 100%)";

        // Barra de progreso con gradiente
        Div barFill = new Div();
        barFill.getStyle()
                .set("width", String.format("%.1f%%", porcentaje))
                .set("height", "100%")
                .set("background", barGradient)
                .set("border-radius", "14px")
                .set("transition", "width 0.8s cubic-bezier(0.4, 0, 0.2, 1)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("box-shadow", "0 2px 8px " + hexToRgba(color, 0.4) + ", inset 0 1px 0 rgba(255, 255, 255, 0.3)");

        // Porcentaje dentro de la barra si es suficientemente grande
        if (porcentaje > 15) {
            Span porcentajeEnBarra = new Span(String.format("%.0f%%", porcentaje));
            porcentajeEnBarra.getStyle()
                    .set("color", "#ffffff")
                    .set("font-weight", "700")
                    .set("font-size", "12px")
                    .set("text-shadow", "0 1px 2px rgba(0, 0, 0, 0.2)");
            barFill.add(porcentajeEnBarra);
        }

        barBackground.add(barFill);
        barContainer.add(labelRow, barBackground);

        return barContainer;
    }

    /**
     * Determina el mensaje indicando en qué tipo de comida se consumieron más
     * calorías
     */
    private String determinarMayorConsumo(float calDesayuno, float calAlmuerzo, float calCena, float calEntretiempos) {
        float maxCalorias = Math.max(Math.max(calDesayuno, calAlmuerzo), Math.max(calCena, calEntretiempos));

        if (maxCalorias == 0) {
            return "No hay datos de consumo registrados";
        }

        String tipoComida;

        if (maxCalorias == calDesayuno) {
            tipoComida = "DESAYUNO";
        } else if (maxCalorias == calAlmuerzo) {
            tipoComida = "ALMUERZO";
        } else if (maxCalorias == calCena) {
            tipoComida = "CENA";
        } else {
            tipoComida = "ENTRETIEMPOS";
        }

        return String.format("El mayor consumo de calorías fue en %s con %.0f kcal",
                tipoComida, maxCalorias);
    }

    private VerticalLayout createBreakfastCard() {
        itemsContainer = new VerticalLayout();
        refreshBreakfastItems();
        return createMealCard("DESAYUNO", itemsContainer, e -> openEditListDialog());
    }

    private VerticalLayout createAlmuerzoCard() {
        itemsContainerAlmuerzo = new VerticalLayout();
        refreshAlmuerzoItems();
        return createMealCard("ALMUERZO", itemsContainerAlmuerzo, e -> openEditListDialogAlmuerzo());
    }

    private VerticalLayout createMeriendaCard() {
        itemsContainerCena = new VerticalLayout();
        refreshCenaItems();
        return createMealCard("CENA", itemsContainerCena, e -> openEditListDialogMerienda());
    }

    private VerticalLayout createEntretiempoCard() {
        itemsContainerEntretiempo = new VerticalLayout();
        refreshEntretiempoItems();
        return createMealCard("ENTRETIEMPOS", itemsContainerEntretiempo, e -> openEditListDialogEntretiempo());
    }

    private VerticalLayout createMealCard(String titleText, VerticalLayout itemsLayout,
            com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<Button>> clickListener) {
        VerticalLayout card = new VerticalLayout();
        applyCardStyle(card);

        // --- ENCABEZADO ---
        HorizontalLayout header = createCardHeader(titleText);

        // --- CONTENIDO DE ALIMENTOS ---
        applyItemsContainerStyle(itemsLayout);

        // --- BOTÓN VER LISTA ---
        Button editButton = createViewListButton(clickListener);

        card.add(header, itemsLayout, editButton);
        return card;
    }

    private void applyCardStyle(VerticalLayout card) {
        card.setWidth("400px");
        card.setSpacing(false);
        card.setPadding(false);
        card.getStyle()
                .set("background-color", "#f0ebf1")
                .set("border-radius", "8px")
                .set("overflow", "hidden")
                .set("box-shadow", "0 4px 6px rgba(0,0,0,0.05)");
    }

    private HorizontalLayout createCardHeader(String title) {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        header.getStyle().set("background-color", "#e5e0eb");

        H3 titleComponent = new H3(title);
        applyTitleStyle(titleComponent);
        header.add(titleComponent);

        return header;
    }

    private void applyTitleStyle(H3 title) {
        title.getStyle()
                .set("margin", "0")
                .set("color", "#6b6375")
                .set("font-size", "14px")
                .set("font-weight", "bold");
    }

    private void applyItemsContainerStyle(VerticalLayout itemsContainer) {
        itemsContainer.setPadding(true);
        itemsContainer.setSpacing(true);
    }

    private Button createViewListButton(
            com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<Button>> clickListener) {
        Button button = new Button("Ver lista", new Icon(VaadinIcon.EYE), clickListener);
        button.getStyle()
                .set("background-color", "#6a419d")
                .set("color", "#ffffff")
                .set("align-self", "center")
                .set("margin-top", "10px")
                .set("border-radius", "8px");
        return button;
    }

    // --- REFRESH ITEMS ---
    private void refreshBreakfastItems() {
        itemsContainer.removeAll();
        ConsumoDiario consumoDiario = getConsumoDiarioActual();
        if (consumoDiario != null) {
            registroService.findByConsumoDiarioAndHorario(consumoDiario, HorarioAlimenticioEnum.DESAYUNO)
                    .forEach(registro -> {
                        if (registro.getAlimento() != null) {
                            String nombreAlimento = registro.getAlimento().getNombre();
                            String cantidad = registro.getCantidad() != null ? registro.getCantidad().toString() : "0";
                            String unidad = registro.getAlimento().getUnidadMedida() != null
                                    ? registro.getAlimento().getUnidadMedida().name()
                                    : "";
                            String macros = "Cantidad: " + cantidad + " " + unidad;
                            itemsContainer.add(createFoodRow(nombreAlimento, macros));
                        }
                    });
        }
    }

    private void refreshAlmuerzoItems() {
        itemsContainerAlmuerzo.removeAll();
        ConsumoDiario consumoDiario = getConsumoDiarioActual();
        if (consumoDiario != null) {
            registroService.findByConsumoDiarioAndHorario(consumoDiario, HorarioAlimenticioEnum.ALMUERZO)
                    .forEach(registro -> {
                        if (registro.getAlimento() != null) {
                            String nombreAlimento = registro.getAlimento().getNombre();
                            String cantidad = registro.getCantidad() != null ? registro.getCantidad().toString() : "0";
                            String unidad = registro.getAlimento().getUnidadMedida() != null
                                    ? registro.getAlimento().getUnidadMedida().name()
                                    : "";
                            String macros = "Cantidad: " + cantidad + " " + unidad;
                            itemsContainerAlmuerzo.add(createFoodRow(nombreAlimento, macros));
                        }
                    });
        }
    }

    private void refreshCenaItems() {
        itemsContainerCena.removeAll();
        ConsumoDiario consumoDiario = getConsumoDiarioActual();
        if (consumoDiario != null) {
            registroService.findByConsumoDiarioAndHorario(consumoDiario, HorarioAlimenticioEnum.CENA)
                    .forEach(registro -> {
                        if (registro.getAlimento() != null) {
                            String nombreAlimento = registro.getAlimento().getNombre();
                            String cantidad = registro.getCantidad() != null ? registro.getCantidad().toString() : "0";
                            String unidad = registro.getAlimento().getUnidadMedida() != null
                                    ? registro.getAlimento().getUnidadMedida().name()
                                    : "";
                            String macros = "Cantidad: " + cantidad + " " + unidad;
                            itemsContainerCena.add(createFoodRow(nombreAlimento, macros));
                        }
                    });
        }
    }

    private void refreshEntretiempoItems() {
        itemsContainerEntretiempo.removeAll();
        ConsumoDiario consumoDiario = getConsumoDiarioActual();
        if (consumoDiario != null) {
            registroService.findByConsumoDiarioAndHorario(consumoDiario, HorarioAlimenticioEnum.ENTRETIEMPOS)
                    .forEach(registro -> {
                        if (registro.getAlimento() != null) {
                            String nombreAlimento = registro.getAlimento().getNombre();
                            String cantidad = registro.getCantidad() != null ? registro.getCantidad().toString() : "0";
                            String unidad = registro.getAlimento().getUnidadMedida() != null
                                    ? registro.getAlimento().getUnidadMedida().name()
                                    : "";
                            String macros = "Cantidad: " + cantidad + " " + unidad;
                            itemsContainerEntretiempo.add(createFoodRow(nombreAlimento, macros));
                        }
                    });
        }
    }

    private HorizontalLayout createFoodRow(String name, String macros) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Span nameLabel = new Span(name);
        nameLabel.getStyle()
                .set("background", "white")
                .set("padding", "5px 15px")
                .set("border-radius", "10px")
                .set("border", "1px solid #dcdcdc")
                .set("min-width", "80px");

        Span macroLabel = new Span(macros);
        macroLabel.getStyle()
                .set("background", "white")
                .set("padding", "5px 15px")
                .set("border-radius", "10px")
                .set("border", "1px solid #dcdcdc")
                .set("flex-grow", "1");

        row.add(nameLabel, macroLabel);
        return row;
    }

    // ---------------- DIALOG DESAYUNO ----------------
    private void openEditListDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Editar lista de Desayuno");
        dialog.setWidth("80%");
        dialog.setHeight("80%");

        Grid<RegistroConsumo> dialogGrid = new Grid<>();
        ConsumoDiario consumoDiario = getConsumoDiarioActual();
        dialogGrid.setItems(query -> registroService
                .listByConsumoDiarioAndHorario(consumoDiario, HorarioAlimenticioEnum.DESAYUNO,
                        toSpringPageRequest(query))
                .stream());

        Button crearEnDialogoBtn = new Button("Crear alimento", new Icon(VaadinIcon.PLUS_CIRCLE), e -> {
            openCreateDialog(HorarioAlimenticioEnum.DESAYUNO, dialogGrid);
        });
        crearEnDialogoBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getHeader().add(crearEnDialogoBtn);

        dialogGrid.addColumn(rc -> rc.getAlimento() != null
                ? safeText(rc.getAlimento().getNombre())
                : "—").setHeader("Alimento");

        dialogGrid.addColumn(rc -> rc.getAlimento() != null
                ? safeEnum(rc.getAlimento().getUnidadMedida())
                : "—").setHeader("Unidad");

        dialogGrid.addColumn(rc -> rc.getCantidad() != null ? rc.getCantidad() : "—").setHeader("Cantidad");

        dialogGrid.addComponentColumn(registro -> createEditButton(registro, dialogGrid)).setHeader("Editar");

        dialogGrid.addComponentColumn(registro -> createDeleteButton(registro, dialogGrid)).setHeader("Eliminar");

        dialogGrid.setSizeFull();
        dialogGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        dialogGrid.setEmptyStateText("No hay registros de consumo");

        VerticalLayout layout = new VerticalLayout(dialogGrid);
        layout.setSizeFull();
        layout.setPadding(false);

        Button cerrarBtn = createCloseButton(dialog);

        dialog.add(layout);
        dialog.getFooter().add(cerrarBtn);
        dialog.open();
    }

    // ----------Dialog ALMUERZO ----------------
    private void openEditListDialogAlmuerzo() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Editar lista de Almuerzo");
        dialog.setWidth("80%");
        dialog.setHeight("80%");

        Grid<RegistroConsumo> dialogGrid = new Grid<>();
        ConsumoDiario consumoDiario = getConsumoDiarioActual();
        dialogGrid.setItems(query -> registroService
                .listByConsumoDiarioAndHorario(consumoDiario, HorarioAlimenticioEnum.ALMUERZO,
                        toSpringPageRequest(query))
                .stream());

        Button crearEnDialogoBtn = new Button("Crear alimento", new Icon(VaadinIcon.PLUS_CIRCLE), e -> {
            openCreateDialog(HorarioAlimenticioEnum.ALMUERZO, dialogGrid);
        });
        crearEnDialogoBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getHeader().add(crearEnDialogoBtn);

        dialogGrid.addColumn(rc -> rc.getAlimento() != null
                ? safeText(rc.getAlimento().getNombre())
                : "—").setHeader("Alimento");

        dialogGrid.addColumn(rc -> rc.getAlimento() != null
                ? safeEnum(rc.getAlimento().getUnidadMedida())
                : "—").setHeader("Unidad");

        dialogGrid.addColumn(rc -> rc.getCantidad() != null ? rc.getCantidad() : "—").setHeader("Cantidad");

        dialogGrid.addComponentColumn(registro -> createEditButton(registro, dialogGrid)).setHeader("Editar");

        dialogGrid.addComponentColumn(registro -> createDeleteButton(registro, dialogGrid)).setHeader("Eliminar");

        dialogGrid.setSizeFull();
        dialogGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        dialogGrid.setEmptyStateText("No hay registros de consumo");

        VerticalLayout layout = new VerticalLayout(dialogGrid);
        layout.setSizeFull();
        layout.setPadding(false);

        Button cerrarBtn = createCloseButton(dialog);

        dialog.add(layout);
        dialog.getFooter().add(cerrarBtn);
        dialog.open();
    }

    // ---------------- DIALOG MERIENDA ----------------
    private void openEditListDialogMerienda() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Editar lista de Cena");
        dialog.setWidth("80%");
        dialog.setHeight("80%");

        Grid<RegistroConsumo> dialogGrid = new Grid<>();
        ConsumoDiario consumoDiario = getConsumoDiarioActual();
        dialogGrid.setItems(query -> registroService
                .listByConsumoDiarioAndHorario(consumoDiario, HorarioAlimenticioEnum.CENA, toSpringPageRequest(query))
                .stream());

        Button crearEnDialogoBtn = new Button("Crear alimento", new Icon(VaadinIcon.PLUS_CIRCLE), e -> {
            openCreateDialog(HorarioAlimenticioEnum.CENA, dialogGrid);
        });
        crearEnDialogoBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getHeader().add(crearEnDialogoBtn);

        dialogGrid.addColumn(rc -> rc.getAlimento() != null
                ? safeText(rc.getAlimento().getNombre())
                : "—").setHeader("Alimento");

        dialogGrid.addColumn(rc -> rc.getAlimento() != null
                ? safeEnum(rc.getAlimento().getUnidadMedida())
                : "—").setHeader("Unidad");

        dialogGrid.addColumn(rc -> rc.getCantidad() != null ? rc.getCantidad() : "—").setHeader("Cantidad");

        dialogGrid.addComponentColumn(registro -> createEditButton(registro, dialogGrid)).setHeader("Editar");

        dialogGrid.addComponentColumn(registro -> createDeleteButton(registro, dialogGrid)).setHeader("Eliminar");

        dialogGrid.setSizeFull();
        dialogGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        dialogGrid.setEmptyStateText("No hay registros de consumo");

        VerticalLayout layout = new VerticalLayout(dialogGrid);
        layout.setSizeFull();
        layout.setPadding(false);

        Button cerrarBtn = createCloseButton(dialog);

        dialog.add(layout);
        dialog.getFooter().add(cerrarBtn);
        dialog.open();
    }

    // ---------------- DIALOG ENTRETIEMPOS ----------------
    private void openEditListDialogEntretiempo() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Editar lista de Entretiempos");
        dialog.setWidth("80%");
        dialog.setHeight("80%");

        Grid<RegistroConsumo> dialogGrid = new Grid<>();
        ConsumoDiario consumoDiario = getConsumoDiarioActual();
        dialogGrid.setItems(query -> registroService
                .listByConsumoDiarioAndHorario(consumoDiario, HorarioAlimenticioEnum.ENTRETIEMPOS,
                        toSpringPageRequest(query))
                .stream());

        Button crearEnDialogoBtn = new Button("Crear alimento", new Icon(VaadinIcon.PLUS_CIRCLE), e -> {
            openCreateDialog(HorarioAlimenticioEnum.ENTRETIEMPOS, dialogGrid);
        });
        crearEnDialogoBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getHeader().add(crearEnDialogoBtn);

        dialogGrid.addColumn(rc -> rc.getAlimento() != null
                ? safeText(rc.getAlimento().getNombre())
                : "—").setHeader("Alimento");

        dialogGrid.addColumn(rc -> rc.getAlimento() != null
                ? safeEnum(rc.getAlimento().getUnidadMedida())
                : "—").setHeader("Unidad");

        dialogGrid.addColumn(rc -> rc.getCantidad() != null ? rc.getCantidad() : "—").setHeader("Cantidad");

        dialogGrid.addComponentColumn(registro -> createEditButton(registro, dialogGrid)).setHeader("Editar");

        dialogGrid.addComponentColumn(registro -> createDeleteButton(registro, dialogGrid)).setHeader("Eliminar");

        dialogGrid.setSizeFull();
        dialogGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        dialogGrid.setEmptyStateText("No hay registros de consumo");

        VerticalLayout layout = new VerticalLayout(dialogGrid);
        layout.setSizeFull();
        layout.setPadding(false);

        Button cerrarBtn = createCloseButton(dialog);

        dialog.add(layout);
        dialog.getFooter().add(cerrarBtn);
        dialog.open();
    }

    private void openCreateDialog() {
        openCreateDialog(HorarioAlimenticioEnum.DESAYUNO, null);
    }

    private void openCreateDialog(HorarioAlimenticioEnum horarioAlimenticio) {
        openCreateDialog(horarioAlimenticio, null);
    }

    private void openCreateDialog(HorarioAlimenticioEnum horarioAlimenticio, Grid<RegistroConsumo> dialogGrid) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Nuevo Registro de Consumo");

        ComboBox<Alimento> alimentoCombo = new ComboBox<>("Alimento");
        alimentoCombo.setItems(alimentoService.findAll());
        alimentoCombo.setItemLabelGenerator(
                a -> a.getNombre() + " - " + a.getUnidadMedida().name());
        alimentoCombo.setRequired(true);
        alimentoCombo.setWidth("100%");

        NumberField cantidadField = new NumberField("Cantidad");
        cantidadField.setMin(0.1);
        cantidadField.setRequired(true);
        cantidadField.setWidth("100%");

        Button guardarBtn = new Button("Guardar", e -> {
            if (alimentoCombo.isEmpty()) {
                Notification.show("Seleccione un alimento")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            if (cantidadField.isEmpty() || cantidadField.getValue() <= 0) {
                Notification.show("Ingrese una cantidad válida")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Obtener el ConsumoDiario de la fecha actual
            ConsumoDiario consumoDiario = getConsumoDiarioActual();

            registroService.crearRegistro(
                    alimentoCombo.getValue(),
                    cantidadField.getValue().floatValue(),
                    horarioAlimenticio,
                    getCuentaActual(),
                    consumoDiario);

            // Refrescar el grid principal
            grid.getDataProvider().refreshAll();

            // Refrescar el grid del diálogo si está presente
            if (dialogGrid != null) {
                dialogGrid.getDataProvider().refreshAll();
            }

            // Refrescar las tarjetas según el horario
            if (horarioAlimenticio == HorarioAlimenticioEnum.DESAYUNO) {
                refreshBreakfastItems();
            } else if (horarioAlimenticio == HorarioAlimenticioEnum.ALMUERZO) {
                refreshAlmuerzoItems();
            } else if (horarioAlimenticio == HorarioAlimenticioEnum.CENA) {
                refreshCenaItems();
            } else if (horarioAlimenticio == HorarioAlimenticioEnum.ENTRETIEMPOS) {
                refreshEntretiempoItems();
            }

            dialog.close();

            Notification.show(
                    "Registro guardado correctamente",
                    3000,
                    Notification.Position.BOTTOM_END).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        guardarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelarBtn = new Button("Cancelar", e -> dialog.close());

        dialog.add(new VerticalLayout(
                alimentoCombo,
                cantidadField));

        dialog.getFooter().add(cancelarBtn, guardarBtn);
        dialog.open();
    }

    private void openEditDialog(RegistroConsumo registro, Grid<RegistroConsumo> dialogGrid) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Editar Registro");

        ComboBox<Alimento> alimentoCombo = new ComboBox<>("Alimento");
        alimentoCombo.setItems(alimentoService.findAll());
        alimentoCombo.setItemLabelGenerator(
                a -> a.getNombre() + " - " + a.getUnidadMedida().name());
        alimentoCombo.setValue(registro.getAlimento());

        NumberField cantidadField = new NumberField("Cantidad");
        cantidadField.setValue(registro.getCantidad().doubleValue());

        Button guardarBtn = new Button("Guardar", e -> {
            if (alimentoCombo.isEmpty() || cantidadField.isEmpty()) {
                Notification.show("Complete todos los campos")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            registro.setAlimento(alimentoCombo.getValue());
            registro.setCantidad(cantidadField.getValue().floatValue());

            registroService.actualizarRegistro(registro);

            // Refrescar el grid principal
            grid.getDataProvider().refreshAll();

            // Refrescar el grid del diálogo si está presente
            if (dialogGrid != null) {
                dialogGrid.getDataProvider().refreshAll();
            }

            // Refrescar las tarjetas según el horario
            if (registro.getHorarioAlimenticio() == HorarioAlimenticioEnum.DESAYUNO) {
                refreshBreakfastItems();
            } else if (registro.getHorarioAlimenticio() == HorarioAlimenticioEnum.ALMUERZO) {
                refreshAlmuerzoItems();
            } else if (registro.getHorarioAlimenticio() == HorarioAlimenticioEnum.CENA) {
                refreshCenaItems();
            } else if (registro.getHorarioAlimenticio() == HorarioAlimenticioEnum.ENTRETIEMPOS) {
                refreshEntretiempoItems();
            }

            dialog.close();

            Notification.show(
                    "Registro actualizado",
                    3000,
                    Notification.Position.BOTTOM_END).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        guardarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelarBtn = new Button("Cancelar", e -> dialog.close());

        dialog.add(new VerticalLayout(
                alimentoCombo,
                cantidadField));

        dialog.getFooter().add(cancelarBtn, guardarBtn);
        dialog.open();
    }

    private void openDeleteDialog(RegistroConsumo registro, Grid<RegistroConsumo> dialogGrid) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Eliminar Registro");

        dialog.add(
                "¿Seguro que deseas eliminar el consumo de "
                        + (registro.getAlimento() != null
                                ? registro.getAlimento().getNombre()
                                : "alimento desconocido")
                        + "?");

        Button eliminarBtn = new Button("Eliminar", e -> {
            HorarioAlimenticioEnum horario = registro.getHorarioAlimenticio();
            registroService.eliminarRegistro(registro.getId());

            // Refrescar el grid principal
            grid.getDataProvider().refreshAll();

            // Refrescar el grid del diálogo si está presente
            if (dialogGrid != null) {
                dialogGrid.getDataProvider().refreshAll();
            }

            // Refrescar las tarjetas según el horario
            if (horario == HorarioAlimenticioEnum.DESAYUNO) {
                refreshBreakfastItems();
            } else if (horario == HorarioAlimenticioEnum.ALMUERZO) {
                refreshAlmuerzoItems();
            } else if (horario == HorarioAlimenticioEnum.CENA) {
                refreshCenaItems();
            } else if (horario == HorarioAlimenticioEnum.ENTRETIEMPOS) {
                refreshEntretiempoItems();
            }

            dialog.close();

            Notification.show(
                    "Registro eliminado",
                    3000,
                    Notification.Position.BOTTOM_END).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        eliminarBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button cancelarBtn = new Button("Cancelar", e -> dialog.close());

        dialog.getFooter().add(cancelarBtn, eliminarBtn);
        dialog.open();
    }

    private String safeEnum(Enum<?> value) {
        return value != null ? value.name() : "—";
    }

    private String safeText(String value) {
        return value != null ? value : "—";
    }

    // --- MÉTODOS AUXILIARES PARA BOTONES REUTILIZABLES ---

    private Button createEditButton(RegistroConsumo registro, Grid<RegistroConsumo> dialogGrid) {
        Button button = new Button("Editar");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_TERTIARY);
        button.addClickListener(e -> openEditDialog(registro, dialogGrid));
        return button;
    }

    private Button createDeleteButton(RegistroConsumo registro, Grid<RegistroConsumo> dialogGrid) {
        Button button = new Button("Eliminar");
        button.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        button.addClickListener(e -> openDeleteDialog(registro, dialogGrid));
        return button;
    }

    private Button createCloseButton(Dialog dialog) {
        Button button = new Button("Cerrar", e -> dialog.close());
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return button;
    }
}
