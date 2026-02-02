package com.equipodinamita.base.ui.registro_consumo;

import java.util.Map;

import com.equipodinamita.base.Service.AlimentoService;
import com.equipodinamita.base.Service.ConsumoDiarioService;
import com.equipodinamita.base.Service.RegistroConsumoService;
import com.equipodinamita.base.models.Alimento;
import com.equipodinamita.base.models.ConsumoDiario;
import com.equipodinamita.base.models.HorarioAlimenticioEnum;
import com.equipodinamita.base.models.RegistroConsumo;
import com.equipodinamita.base.ui.ViewToolbar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("registro-consumo")
@PageTitle("Registro de Consumo")
//@Menu(order = 1, icon = "vaadin:clipboard-text", title = "Registro de Consumo")
public class RegistroConsumoListView extends VerticalLayout {

    private final RegistroConsumoService registroService;
    private final AlimentoService alimentoService;
    private final ConsumoDiarioService consumoDiarioService;

    private final Grid<RegistroConsumo> grid;
    private final Button crearBtn;
    private VerticalLayout itemsContainer;
    private VerticalLayout itemsContainerAlmuerzo;
    private VerticalLayout itemsContainerCena;
    private VerticalLayout itemsContainerEntretiempo;

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
        grid.setItems(query -> registroService.list(toSpringPageRequest(query)).stream());

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().setOverflow(Style.Overflow.HIDDEN);

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

        // --- BOTÓN VER CONSUMO EN LA PARTE INFERIOR ---
        HorizontalLayout bottomButtonContainer = createVerConsumoButton();

        add(
                new ViewToolbar(
                        "Registro de Consumo"),
                cardsWrapper,
                bottomButtonContainer);
    }

    private HorizontalLayout createVerConsumoButton() {
        HorizontalLayout container = new HorizontalLayout();
        container.setWidthFull();
        container.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        container.setAlignItems(FlexComponent.Alignment.CENTER);
        container.getStyle().set("margin-top", "auto").set("padding", "20px");

        // Contenedor para los botones desplegables
        HorizontalLayout botonesOpciones = new HorizontalLayout();
        botonesOpciones.setSpacing(true);
        botonesOpciones.setVisible(false);

        Button btnDiario = new Button("Diario", new Icon(VaadinIcon.CALENDAR), e -> {
            openConsumoDiarioDialog();
        });
        btnDiario.getStyle()
                .set("background-color", "#4CAF50")
                .set("color", "#ffffff")
                .set("border-radius", "8px");

        Button btnSemanal = new Button("Semanal", new Icon(VaadinIcon.CALENDAR_CLOCK), e -> {
            Notification.show("Vista Semanal", 2000, Notification.Position.BOTTOM_CENTER);
            // Aquí puedes agregar la lógica para mostrar consumo semanal
        });
        btnSemanal.getStyle()
                .set("background-color", "#2196F3")
                .set("color", "#ffffff")
                .set("border-radius", "8px");

        botonesOpciones.add(btnDiario, btnSemanal);

        // Botón principal "Ver Consumo"
        Button btnVerConsumo = new Button("Ver Consumo", new Icon(VaadinIcon.CHART), e -> {
            botonesOpciones.setVisible(!botonesOpciones.isVisible());
        });
        btnVerConsumo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnVerConsumo.getStyle()
                .set("background-color", "#6a419d")
                .set("color", "#ffffff")
                .set("border-radius", "8px")
                .set("padding", "10px 20px");

        container.add(btnVerConsumo, botonesOpciones);
        return container;
    }

    private void openConsumoDiarioDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("📊 Resumen de Consumo Diario");
        dialog.setWidth("900px");
        dialog.setHeight("700px");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);
        content.getStyle().set("overflow-y", "auto");

        // Obtener el total de consumo diario
        ConsumoDiario totalDiario = consumoDiarioService.calcularTotalConsumoDiario();

        if (totalDiario.getTotalRegistros() == 0) {
            content.add(new H3("⚠️ No hay registros de consumo para hoy"));
            dialog.add(content);
            dialog.open();
            return;
        }

        // === SECCIÓN: RESUMEN TOTAL ===
        VerticalLayout resumenSection = new VerticalLayout();
        resumenSection.setPadding(true);
        resumenSection.getStyle()
                .set("background-color", "#f5f5f5")
                .set("border-radius", "12px")
                .set("margin-bottom", "20px");

        H3 tituloResumen = new H3("🍽️ Resumen Total del Día");
        tituloResumen.getStyle().set("margin", "0 0 15px 0").set("color", "#333");

        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setWidthFull();
        statsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.AROUND);

        statsLayout.add(
                createStatCard("🔥 Calorías", String.format("%.1f kcal", totalDiario.getCalorias()), "#FF6B6B"),
                createStatCard("🥩 Proteínas", String.format("%.1f g", totalDiario.getProteinas()), "#4ECDC4"),
                createStatCard("🍞 Carbohidratos", String.format("%.1f g", totalDiario.getCarbohidratos()), "#FFE66D"),
                createStatCard("🧈 Grasas", String.format("%.1f g", totalDiario.getGrasas()), "#95E1D3"));

        Span totalAlimentos = new Span("📋 Total de alimentos registrados: " + totalDiario.getTotalRegistros());
        totalAlimentos.getStyle()
                .set("font-size", "16px")
                .set("font-weight", "bold")
                .set("margin-top", "15px");

        resumenSection.add(tituloResumen, statsLayout, totalAlimentos);
        content.add(resumenSection);

        // === SECCIÓN: PROMEDIO POR HORARIO ===
        Map<HorarioAlimenticioEnum, ConsumoDiario> promediosPorHorario = consumoDiarioService
                .calcularPromedioPorCadaHorario();

        VerticalLayout horarioSection = new VerticalLayout();
        horarioSection.setPadding(true);
        horarioSection.getStyle()
                .set("background-color", "#e8f4fd")
                .set("border-radius", "12px")
                .set("margin-bottom", "20px");

        H3 tituloHorarios = new H3("⏰ Consumo por Tipo de Comida");
        tituloHorarios.getStyle().set("margin", "0 0 15px 0").set("color", "#333");
        horarioSection.add(tituloHorarios);

        HorizontalLayout horariosGrid = new HorizontalLayout();
        horariosGrid.setWidthFull();
        horariosGrid.setJustifyContentMode(FlexComponent.JustifyContentMode.AROUND);
        horariosGrid.getStyle().set("flex-wrap", "wrap");

        for (HorarioAlimenticioEnum horario : HorarioAlimenticioEnum.values()) {
            ConsumoDiario consumoHorario = promediosPorHorario.get(horario);
            if (consumoHorario != null && consumoHorario.getTotalRegistros() > 0) {
                horariosGrid.add(createHorarioCard(horario, consumoHorario));
            }
        }

        horarioSection.add(horariosGrid);
        content.add(horarioSection);

        // === Seccion del total de calorias de un dia===
        Span totalCaloriasDia = new Span(
                "🔥 Total de Calorías del Día: " + String.format("%.1f kcal", totalDiario.getCalorias()));
        totalCaloriasDia.getStyle()
                .set("font-size", "18px")
                .set("font-weight", "bold")
                .set("margin", "10px 0");
        content.add(totalCaloriasDia);

        // === SECCIÓN: DETALLE DE ALIMENTOS ===
        VerticalLayout detalleSection = new VerticalLayout();
        detalleSection.setPadding(true);
        detalleSection.getStyle()
                .set("background-color", "#fff3e0")
                .set("border-radius", "12px");

        H3 tituloDetalle = new H3("📝 Detalle de Alimentos Consumidos");
        tituloDetalle.getStyle().set("margin", "0 0 15px 0").set("color", "#333");
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
        card.getStyle()
                .set("background-color", color)
                .set("border-radius", "10px")
                .set("min-width", "140px")
                .set("color", "#fff");

        Span tituloSpan = new Span(titulo);
        tituloSpan.getStyle().set("font-size", "12px").set("font-weight", "bold");

        Span valorSpan = new Span(valor);
        valorSpan.getStyle().set("font-size", "18px").set("font-weight", "bold");

        card.add(tituloSpan, valorSpan);
        return card;
    }

    private VerticalLayout createHorarioCard(HorarioAlimenticioEnum horario, ConsumoDiario consumo) {
        VerticalLayout card = new VerticalLayout();
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.setPadding(true);
        card.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "10px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                .set("min-width", "180px")
                .set("margin", "5px");

        String emoji = switch (horario) {
            case DESAYUNO -> "🌅";
            case ALMUERZO -> "☀️";
            case CENA -> "🌙";
            case ENTRETIEMPOS -> "🍿";
        };

        Span tituloSpan = new Span(emoji + " " + horario.name());
        tituloSpan.getStyle().set("font-weight", "bold").set("font-size", "14px");

        Span registrosSpan = new Span("Alimentos: " + consumo.getTotalRegistros());
        registrosSpan.getStyle().set("font-size", "12px").set("color", "#666");

        Span caloriasSpan = new Span(String.format("%.1f kcal", consumo.getCalorias()));
        caloriasSpan.getStyle().set("font-size", "16px").set("font-weight", "bold").set("color", "#FF6B6B");

        card.add(tituloSpan, registrosSpan, caloriasSpan);
        return card;
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
        registroService.listByHorarioAlimenticio(HorarioAlimenticioEnum.DESAYUNO,
                org.springframework.data.domain.Pageable.unpaged()).forEach(registro -> {
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

    private void refreshAlmuerzoItems() {
        itemsContainerAlmuerzo.removeAll();
        registroService.listByHorarioAlimenticio(HorarioAlimenticioEnum.ALMUERZO,
                org.springframework.data.domain.Pageable.unpaged()).forEach(registro -> {
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

    private void refreshCenaItems() {
        itemsContainerCena.removeAll();
        registroService.listByHorarioAlimenticio(HorarioAlimenticioEnum.CENA,
                org.springframework.data.domain.Pageable.unpaged()).forEach(registro -> {
                    if (registro.getAlimento() != null) {
                        String nombreAlimento = registro.getAlimento().getNombre();
                        String cantidad = registro.getCantidad() != null ? registro.getCantidad().toString() : "0";
                        String unidad = registro.getAlimento().getUnidadMedida() != null
                                ? registro.getAlimento().getUnidadMedida().name()
                                : "";
                        String macros = "Cantidad: " + cantidad + " " + unidad;
                        itemsContainerCena
                                .add(createFoodRow(nombreAlimento, macros));
                    }
                });
    }

    private void refreshEntretiempoItems() {
        itemsContainerEntretiempo.removeAll();
        registroService.listByHorarioAlimenticio(HorarioAlimenticioEnum.ENTRETIEMPOS,
                org.springframework.data.domain.Pageable.unpaged()).forEach(registro -> {
                    if (registro.getAlimento() != null) {
                        String nombreAlimento = registro.getAlimento().getNombre();
                        String cantidad = registro.getCantidad() != null ? registro.getCantidad().toString() : "0";
                        String unidad = registro.getAlimento().getUnidadMedida() != null
                                ? registro.getAlimento().getUnidadMedida().name()
                                : "";
                        String macros = "Cantidad: " + cantidad + " " + unidad;
                        itemsContainerEntretiempo
                                .add(createFoodRow(nombreAlimento, macros));
                    }
                });
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
        dialogGrid.setItems(query -> registroService
                .listByHorarioAlimenticio(HorarioAlimenticioEnum.DESAYUNO, toSpringPageRequest(query)).stream());

        Button crearEnDialogoBtn = new Button("Crear consumo", new Icon(VaadinIcon.PLUS_CIRCLE), e -> {
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
        dialogGrid.setItems(query -> registroService
                .listByHorarioAlimenticio(HorarioAlimenticioEnum.ALMUERZO, toSpringPageRequest(query)).stream());

        Button crearEnDialogoBtn = new Button("Crear consumo", new Icon(VaadinIcon.PLUS_CIRCLE), e -> {
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
        dialogGrid.setItems(query -> registroService
                .listByHorarioAlimenticio(HorarioAlimenticioEnum.CENA, toSpringPageRequest(query)).stream());

        Button crearEnDialogoBtn = new Button("Crear consumo", new Icon(VaadinIcon.PLUS_CIRCLE), e -> {
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
        dialogGrid.setItems(query -> registroService
                .listByHorarioAlimenticio(HorarioAlimenticioEnum.ENTRETIEMPOS, toSpringPageRequest(query)).stream());

        Button crearEnDialogoBtn = new Button("Crear consumo", new Icon(VaadinIcon.PLUS_CIRCLE), e -> {
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
            registroService.crearRegistro(
                    alimentoCombo.getValue(),
                    cantidadField.getValue().floatValue(),
                    horarioAlimenticio);

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
