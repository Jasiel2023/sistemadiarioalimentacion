package com.equipodinamita.base.ui.registro_consumo;

import com.equipodinamita.base.Service.AlimentoService;
import com.equipodinamita.base.Service.RegistroConsumoService;
import com.equipodinamita.base.models.Alimento;
import com.equipodinamita.base.models.RegistroConsumo;
import com.equipodinamita.base.ui.ViewToolbar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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

    public RegistroConsumoListView(
            RegistroConsumoService registroService,
            AlimentoService alimentoService) {

        this.registroService = registroService;
        this.alimentoService = alimentoService;

        crearBtn = new Button("Crear registro", e -> openCreateDialog());
        crearBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        grid = new Grid<>();
        grid.setItems(query -> registroService.list(toSpringPageRequest(query)).stream());

        grid.addColumn(rc ->
            rc.getAlimento() != null
                ? safeText(rc.getAlimento().getNombre())
                : "—"
        ).setHeader("Alimento");

        grid.addColumn(rc ->
            rc.getAlimento() != null
                ? safeEnum(rc.getAlimento().getUnidadMedida())
                : "—"
        ).setHeader("Unidad");

        grid.addColumn(rc ->
            rc.getCantidad() != null ? rc.getCantidad() : "—"
        ).setHeader("Cantidad");

        grid.addComponentColumn(registro -> {
            Button editBtn = new Button("Editar");
            editBtn.addThemeVariants(
                ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_TERTIARY
            );
            editBtn.addClickListener(e -> openEditDialog(registro));
            return editBtn;
        }).setHeader("Editar");

        grid.addComponentColumn(registro -> {
            Button deleteBtn = new Button("Eliminar");
            deleteBtn.addThemeVariants(
                ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_TERTIARY
            );
            deleteBtn.addClickListener(e -> openDeleteDialog(registro));
            return deleteBtn;
        }).setHeader("Eliminar");

        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setEmptyStateText("No hay registros de consumo");

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().setOverflow(Style.Overflow.HIDDEN);

        add(
            new ViewToolbar(
                "Registro de Consumo",
                ViewToolbar.group(crearBtn)
            ),
            grid
        );
    }

    // ---------------- DIALOG ----------------

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Nuevo Registro de Consumo");

        ComboBox<Alimento> alimentoCombo = new ComboBox<>("Alimento");
        alimentoCombo.setItems(alimentoService.findAll());
        alimentoCombo.setItemLabelGenerator(
            a -> a.getNombre() + " - " + a.getUnidadMedida().name()
        );
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
                cantidadField.getValue().floatValue()
            );

            grid.getDataProvider().refreshAll();
            dialog.close();

            Notification.show(
                "Registro guardado correctamente",
                3000,
                Notification.Position.BOTTOM_END
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        guardarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelarBtn = new Button("Cancelar", e -> dialog.close());

        dialog.add(new VerticalLayout(
            alimentoCombo,
            cantidadField
        ));

        dialog.getFooter().add(cancelarBtn, guardarBtn);
        dialog.open();
    }
    
    private void openEditDialog(RegistroConsumo registro) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Editar Registro");

        ComboBox<Alimento> alimentoCombo = new ComboBox<>("Alimento");
        alimentoCombo.setItems(alimentoService.findAll());
        alimentoCombo.setItemLabelGenerator(
            a -> a.getNombre() + " - " + a.getUnidadMedida().name()
        );
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
                Notification.Position.BOTTOM_END
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        guardarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelarBtn = new Button("Cancelar", e -> dialog.close());

        dialog.add(new VerticalLayout(
            alimentoCombo,
            cantidadField
        ));

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
        + "?"
    );


    Button eliminarBtn = new Button("Eliminar", e -> {
        registroService.eliminarRegistro(registro.getId());
        grid.getDataProvider().refreshAll();
        dialog.close();

        Notification.show(
            "Registro eliminado",
            3000,
            Notification.Position.BOTTOM_END
        ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
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
}
