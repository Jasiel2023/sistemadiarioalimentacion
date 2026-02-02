package com.equipodinamita.base.ui.alimento;

import com.equipodinamita.base.Service.AlimentoService;
import com.equipodinamita.base.models.Alimento;
import com.equipodinamita.base.models.CategoriaEnum;
import com.equipodinamita.base.models.UnidadEnum;
import com.equipodinamita.base.ui.MainLayout;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;
import com.equipodinamita.base.ui.MainLayout;
@Route(value = "alimentos", layout = MainLayout.class)
@PageTitle("Alimentos")

//@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Alimento List Admin")
class AlimentoListView extends VerticalLayout {

    private final AlimentoService alimentoService;

    final Button createBtn;
    final Grid<Alimento> alimentoGrid;

    AlimentoListView(AlimentoService alimentoService) {
        this.alimentoService = alimentoService;

        createBtn = new Button("Crear Alimento", event -> openCreateDialog());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        alimentoGrid = new Grid<>();
        alimentoGrid.setItems(query -> alimentoService.list(toSpringPageRequest(query)).stream());
        alimentoGrid.addColumn(Alimento::getNombre).setHeader("Nombre");
        alimentoGrid.addColumn(Alimento::getCalorias).setHeader("Calorias");
        alimentoGrid.addColumn(Alimento::getProteinas).setHeader("Proteinas");
        alimentoGrid.addColumn(Alimento::getCarbohidratos).setHeader("Carbohidratos");
        alimentoGrid.addColumn(Alimento::getGrasas).setHeader("Grasas");
        alimentoGrid.addColumn(Alimento::getPorcionBase).setHeader("Porcion Base");
        alimentoGrid.addColumn(a ->
            a.getCategoria() != null ? a.getCategoria().name() : "—"
        ).setHeader("Categoría");

        alimentoGrid.addColumn(a ->
            a.getUnidadMedida() != null ? a.getUnidadMedida().name() : "—"
        ).setHeader("Unidad de Medida");

        alimentoGrid.addComponentColumn(alimento -> {Button deleteBtn = new Button("Eliminar");
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        deleteBtn.addClickListener(event -> deleteAlimento(alimento));
        return deleteBtn;
        }).setHeader("ELiminar");

        alimentoGrid.addComponentColumn(alimento -> {
        Button editBtn = new Button("Editar");
        editBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_TERTIARY);
        editBtn.addClickListener(e -> openEditDialog(alimento));
        return editBtn;
        }).setHeader("Editar");



        alimentoGrid.setEmptyStateText("You have no Alimentos to complete");
        alimentoGrid.setSizeFull();
        alimentoGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().setOverflow(Style.Overflow.HIDDEN);

    add(new ViewToolbar("Alimento List",ViewToolbar.group(createBtn)),alimentoGrid);
    }

    private Float toFloat(Double value) {
    return value != null ? value.floatValue() : null;
    }

    private void openCreateDialog() {
    Dialog dialog = new Dialog();
    dialog.setHeaderTitle("Nuevo Alimento");

    TextField nombre = new TextField("Nombre");

    NumberField calorias = new NumberField("Calorías");
    NumberField proteinas = new NumberField("Proteínas");
    NumberField carbohidratos = new NumberField("Carbohidratos");
    NumberField grasas = new NumberField("Grasas");
    NumberField porcionBase = new NumberField("Porción Base");

    ComboBox<UnidadEnum> unidadMedida = new ComboBox<>("Unidad de Medida");
    unidadMedida.setItems(UnidadEnum.values());
    unidadMedida.setRequired(true);

    ComboBox<CategoriaEnum> categoria = new ComboBox<>("Categoría");
    categoria.setItems(CategoriaEnum.values());
    categoria.setRequired(true);

    Button saveBtn = new Button("Guardar", e -> {
        if (nombre.isEmpty()
        || unidadMedida.getValue() == null
        || categoria.getValue() == null) {

        Notification.show(
            "Nombre, unidad y categoría son obligatorios",
            3000,
            Notification.Position.MIDDLE
        ).addThemeVariants(NotificationVariant.LUMO_ERROR);

        return;
    }
        alimentoService.createAlimento(
            nombre.getValue(),
            toFloat(calorias.getValue()),
            toFloat(proteinas.getValue()),
            toFloat(carbohidratos.getValue()),
            toFloat(grasas.getValue()),
            toFloat(porcionBase.getValue()),
            unidadMedida.getValue(),
            categoria.getValue()
        );

        alimentoGrid.getDataProvider().refreshAll();
        dialog.close();

        Notification.show("Alimento creado", 3000,
                Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    });

    saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    Button cancelBtn = new Button("Cancelar", e -> dialog.close());

    dialog.add(new VerticalLayout(
        nombre,
        calorias,
        proteinas,
        carbohidratos,
        grasas,
        porcionBase,
        unidadMedida,
        categoria
    ));

    dialog.getFooter().add(cancelBtn, saveBtn);
    dialog.open();
}

    private void deleteAlimento(Alimento alimento) {
    Dialog dialog = new Dialog();
    dialog.setHeaderTitle("Confirmar eliminación");

    dialog.add("¿Estás seguro de eliminar el alimento: " + alimento.getNombre() + "?");

    Button confirm = new Button("Eliminar", e -> {
        alimentoService.deleteAlimento(alimento.getId());
        alimentoGrid.getDataProvider().refreshAll();
        dialog.close();

        Notification.show("Alimento eliminado", 3000,
                Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    });

    confirm.addThemeVariants(ButtonVariant.LUMO_ERROR);

    Button cancel = new Button("Cancelar", e -> dialog.close());

    dialog.getFooter().add(cancel, confirm);
    dialog.open();
    }

    private void openEditDialog(Alimento alimento) {
    Dialog dialog = new Dialog();
    dialog.setHeaderTitle("Editar Alimento");

    TextField nombreEdit = new TextField("Nombre");
    nombreEdit.setValue(alimento.getNombre());

    NumberField caloriasEdit = new NumberField("Calorías");
    caloriasEdit.setValue(alimento.getCalorias() != null ? alimento.getCalorias().doubleValue() : null);

    NumberField proteinasEdit = new NumberField("Proteínas");
    proteinasEdit.setValue(alimento.getProteinas() != null ? alimento.getProteinas().doubleValue() : null);

    NumberField carboEdit = new NumberField("Carbohidratos");
    carboEdit.setValue(alimento.getCarbohidratos() != null ? alimento.getCarbohidratos().doubleValue() : null);

    NumberField grasasEdit = new NumberField("Grasas");
    grasasEdit.setValue(alimento.getGrasas() != null ? alimento.getGrasas().doubleValue() : null);

    NumberField porcionEdit = new NumberField("Porción Base");
    porcionEdit.setValue(alimento.getPorcionBase() != null ? alimento.getPorcionBase().doubleValue() : null);

    ComboBox<UnidadEnum> unidadEdit = new ComboBox<>("Unidad Medida");
    unidadEdit.setItems(UnidadEnum.values());
    unidadEdit.setValue(alimento.getUnidadMedida());

    ComboBox<CategoriaEnum> categoriaEdit = new ComboBox<>("Categoría");
    categoriaEdit.setItems(CategoriaEnum.values());
    categoriaEdit.setValue(alimento.getCategoria());

    Button saveBtn = new Button("Guardar", e -> {
        alimento.setNombre(nombreEdit.getValue());
        alimento.setCalorias(toFloat(caloriasEdit.getValue()));
        alimento.setProteinas(toFloat(proteinasEdit.getValue()));
        alimento.setCarbohidratos(toFloat(carboEdit.getValue()));
        alimento.setGrasas(toFloat(grasasEdit.getValue()));
        alimento.setPorcionBase(toFloat(porcionEdit.getValue()));
        alimento.setUnidadMedida(unidadEdit.getValue());
        alimento.setCategoria(categoriaEdit.getValue());

        alimentoService.updateAlimento(alimento);
        alimentoGrid.getDataProvider().refreshAll();
        dialog.close();

        Notification.show("Alimento actualizado", 3000,
                Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    });

    saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    Button cancelBtn = new Button("Cancelar", e -> dialog.close());

    dialog.add(
        new VerticalLayout(
            nombreEdit,
            caloriasEdit,
            proteinasEdit,
            carboEdit,
            grasasEdit,
            porcionEdit,
            unidadEdit,
            categoriaEdit
        )
    );

    dialog.getFooter().add(cancelBtn, saveBtn);
    dialog.open();
}

}
