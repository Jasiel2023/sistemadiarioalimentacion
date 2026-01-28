package com.equipodinamita.base.ui.registro_consumo;

import com.equipodinamita.base.Service.AlimentoService;
import com.equipodinamita.base.Service.RegistroConsumoService;
import com.equipodinamita.base.models.Alimento;
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
@Menu(order = 1, icon = "vaadin:clipboard-text", title = "Registro de Consumo")
public class RegistroConsumoListView extends VerticalLayout {

    private final RegistroConsumoService registroService;
    private final AlimentoService alimentoService;

    private final Grid<RegistroConsumo> grid;
    private final Button crearBtn;
    private VerticalLayout itemsContainer;
    private VerticalLayout itemsContainerMerienda;
    private VerticalLayout itemsContainerAlmuerzo;

    public RegistroConsumoListView(
            RegistroConsumoService registroService,
            AlimentoService alimentoService) {

        this.registroService = registroService;
        this.alimentoService = alimentoService;

        crearBtn = new Button("Crear registro", e -> openCreateDialog());
        crearBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        grid = new Grid<>();
        grid.setItems(query -> registroService.list(toSpringPageRequest(query)).stream());

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().setOverflow(Style.Overflow.HIDDEN);

        add(
                new ViewToolbar(
                        "Registro de Consumo"),
                createBreakfastCard(),
                createMeriendaCard(),
                createAlmuerzoCard());
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
        itemsContainerMerienda = new VerticalLayout();
        refreshMeriendaItems();
        return createMealCard("MERIENDA", itemsContainerMerienda, e -> openEditListDialogMerienda());
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
        Button button = new Button("Ver Lista", clickListener);
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

    private void refreshMeriendaItems() {
        itemsContainerMerienda
                .removeAll();
        registroService.listByHorarioAlimenticio(HorarioAlimenticioEnum.CENA,
                org.springframework.data.domain.Pageable.unpaged()).forEach(registro -> {
                    if (registro.getAlimento() != null) {
                        String nombreAlimento = registro.getAlimento().getNombre();
                        String cantidad = registro.getCantidad() != null ? registro.getCantidad().toString() : "0";
                        String unidad = registro.getAlimento().getUnidadMedida() != null
                                ? registro.getAlimento().getUnidadMedida().name()
                                : "";
                        String macros = "Cantidad: " + cantidad + " " + unidad;
                        itemsContainerMerienda
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
        dialog.setHeaderTitle("Editar Lista de Desayuno");
        dialog.setWidth("80%");
        dialog.setHeight("80%");

        Button crearEnDialogoBtn = new Button("Crear Nuevo Consumo", e -> {
            openCreateDialog(HorarioAlimenticioEnum.DESAYUNO);
            dialog.close();
        });
        crearEnDialogoBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getHeader().add(crearEnDialogoBtn);

        Grid<RegistroConsumo> dialogGrid = new Grid<>();
        dialogGrid.setItems(query -> registroService
                .listByHorarioAlimenticio(HorarioAlimenticioEnum.DESAYUNO, toSpringPageRequest(query)).stream());

        dialogGrid.addColumn(rc -> rc.getAlimento() != null
                ? safeText(rc.getAlimento().getNombre())
                : "—").setHeader("Alimento");

        dialogGrid.addColumn(rc -> rc.getAlimento() != null
                ? safeEnum(rc.getAlimento().getUnidadMedida())
                : "—").setHeader("Unidad");

        dialogGrid.addColumn(rc -> rc.getCantidad() != null ? rc.getCantidad() : "—").setHeader("Cantidad");

        dialogGrid.addComponentColumn(this::createEditButton).setHeader("Editar");

        dialogGrid.addComponentColumn(this::createDeleteButton).setHeader("Eliminar");

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
        dialog.setHeaderTitle("Editar Lista de Almuerzo");
        dialog.setWidth("80%");
        dialog.setHeight("80%");

        Button crearEnDialogoBtn = new Button("Crear Nuevo Consumo", e -> {
            openCreateDialog(HorarioAlimenticioEnum.ALMUERZO);
            dialog.close();
        });
        crearEnDialogoBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getHeader().add(crearEnDialogoBtn);

        Grid<RegistroConsumo> dialogGrid = new Grid<>();
        dialogGrid.setItems(query -> registroService
                .listByHorarioAlimenticio(HorarioAlimenticioEnum.ALMUERZO, toSpringPageRequest(query)).stream());

        dialogGrid.addColumn(rc -> rc.getAlimento() != null
                ? safeText(rc.getAlimento().getNombre())
                : "—").setHeader("Alimento");

        dialogGrid.addColumn(rc -> rc.getAlimento() != null
                ? safeEnum(rc.getAlimento().getUnidadMedida())
                : "—").setHeader("Unidad");

        dialogGrid.addColumn(rc -> rc.getCantidad() != null ? rc.getCantidad() : "—").setHeader("Cantidad");

        dialogGrid.addComponentColumn(this::createEditButton).setHeader("Editar");

        dialogGrid.addComponentColumn(this::createDeleteButton).setHeader("Eliminar");

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
        dialog.setHeaderTitle("Editar Lista");
        dialog.setWidth("80%");
        dialog.setHeight("80%");

        Button crearEnDialogoBtn = new Button("Crear Nuevo Consumo (Merienda)", e -> {
            openCreateDialog(HorarioAlimenticioEnum.ENTRETIEMPOS);
            dialog.close();
        });
        crearEnDialogoBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getHeader().add(crearEnDialogoBtn);

        Grid<RegistroConsumo> dialogGrid = new Grid<>();
        dialogGrid.setItems(query -> registroService
                .listByHorarioAlimenticio(HorarioAlimenticioEnum.ENTRETIEMPOS, toSpringPageRequest(query)).stream());

        dialogGrid.addColumn(rc -> rc.getAlimento() != null
                ? safeText(rc.getAlimento().getNombre())
                : "—").setHeader("Alimento");

        dialogGrid.addColumn(rc -> rc.getAlimento() != null
                ? safeEnum(rc.getAlimento().getUnidadMedida())
                : "—").setHeader("Unidad");

        dialogGrid.addColumn(rc -> rc.getCantidad() != null ? rc.getCantidad() : "—").setHeader("Cantidad");

        dialogGrid.addComponentColumn(this::createEditButton).setHeader("Editar");

        dialogGrid.addComponentColumn(this::createDeleteButton).setHeader("Eliminar");

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
        openCreateDialog(HorarioAlimenticioEnum.DESAYUNO);
    }

    private void openCreateDialog(HorarioAlimenticioEnum horarioAlimenticio) {
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

            if (horarioAlimenticio == HorarioAlimenticioEnum.DESAYUNO) {
                refreshBreakfastItems();
            } else if (horarioAlimenticio == HorarioAlimenticioEnum.ENTRETIEMPOS) {
                refreshMeriendaItems();
            }
            grid.getDataProvider().refreshAll();
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

    private void openEditDialog(RegistroConsumo registro) {
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
            grid.getDataProvider().refreshAll();
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

    private void openDeleteDialog(RegistroConsumo registro) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Eliminar Registro");

        dialog.add(
                "¿Seguro que deseas eliminar el consumo de "
                        + (registro.getAlimento() != null
                                ? registro.getAlimento().getNombre()
                                : "alimento desconocido")
                        + "?");

        Button eliminarBtn = new Button("Eliminar", e -> {
            registroService.eliminarRegistro(registro.getId());
            grid.getDataProvider().refreshAll();
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

    private Button createEditButton(RegistroConsumo registro) {
        Button button = new Button("Editar");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_TERTIARY);
        button.addClickListener(e -> openEditDialog(registro));
        return button;
    }

    private Button createDeleteButton(RegistroConsumo registro) {
        Button button = new Button("Eliminar");
        button.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        button.addClickListener(e -> openDeleteDialog(registro));
        return button;
    }

    private Button createCloseButton(Dialog dialog) {
        Button button = new Button("Cerrar", e -> dialog.close());
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return button;
    }
}
