package com.equipodinamita.base.ui.alimento;

import java.util.Comparator;
import java.util.List;

import com.equipodinamita.base.Service.AlimentoService;
import com.equipodinamita.base.models.Alimento;
import com.equipodinamita.base.models.CategoriaEnum;
import com.equipodinamita.base.models.UnidadEnum;
import com.equipodinamita.base.ui.MainLayout;
import com.equipodinamita.base.ui.ViewToolbar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route(value = "alimentos", layout = MainLayout.class)
@PageTitle("Alimentos")

// @Menu(order = 0, icon = "vaadin:clipboard-check", title = "Alimento List
// Admin")
class AlimentoListView extends VerticalLayout {

    private final AlimentoService alimentoService;

    final Button createBtn;
    final Grid<Alimento> alimentoGrid;

    private ComboBox<CategoriaEnum> categoriaFiltro;
    private ComboBox<String> ordenCombo;

    AlimentoListView(AlimentoService alimentoService) {
        this.alimentoService = alimentoService;

        createBtn = new Button("Crear Alimento", event -> openCreateDialog());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        categoriaFiltro = new ComboBox<>("Filtrar por categoría");
        categoriaFiltro.setItems(CategoriaEnum.values());
        categoriaFiltro.setClearButtonVisible(true);
        categoriaFiltro.setWidth("220px");
        categoriaFiltro.addThemeVariants(ComboBoxVariant.LUMO_SMALL);

        ordenCombo = new ComboBox<>("Ordenar por");
        ordenCombo.setItems("Nombre A-Z", "Nombre Z-A");
        ordenCombo.setClearButtonVisible(true);
        ordenCombo.setWidth("160px");
        ordenCombo.addThemeVariants(ComboBoxVariant.LUMO_SMALL);

        // Listeners
        categoriaFiltro.addValueChangeListener(e -> refrescarLista());
        ordenCombo.addValueChangeListener(e -> refrescarLista());

        alimentoGrid = new Grid<>();
        alimentoGrid.setItems(query -> alimentoService.list(toSpringPageRequest(query)).stream());
        alimentoGrid.addColumn(Alimento::getNombre).setHeader("Nombre").setAutoWidth(true);
        alimentoGrid.addColumn(Alimento::getCalorias).setHeader("Calorias");
        alimentoGrid.addColumn(Alimento::getProteinas).setHeader("Proteinas");
        alimentoGrid.addColumn(Alimento::getCarbohidratos).setHeader("Carbohidratos");
        alimentoGrid.addColumn(Alimento::getGrasas).setHeader("Grasas");
        alimentoGrid.addColumn(Alimento::getPorcionBase).setHeader("Porcion Base");
        alimentoGrid.addColumn(a -> a.getCategoria() != null ? a.getCategoria().name() : "—").setHeader("Categoría");

        alimentoGrid.addColumn(a -> a.getUnidadMedida() != null ? a.getUnidadMedida().name() : "—")
                .setHeader("Unidad de Medida");

        alimentoGrid.addComponentColumn(alimento -> {
            Button deleteBtn = new Button("Eliminar");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

            deleteBtn.addClickListener(event -> deleteAlimento(alimento));
            return deleteBtn;
        }).setHeader("ELiminar");

        alimentoGrid.addComponentColumn(alimento -> {
            Button editBtn = new Button("Editar");
            editBtn.addThemeVariants(
                    ButtonVariant.LUMO_PRIMARY,
                    ButtonVariant.LUMO_SMALL);
            editBtn.addClickListener(e -> openEditDialog(alimento));
            return editBtn;
        }).setHeader("Editar");

        alimentoGrid.setEmptyStateText("You have no Alimentos to complete");
        alimentoGrid.setSizeFull();
        alimentoGrid.getColumnByKey("Nombre"); // si usas keys, o directamente:

        alimentoGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().setOverflow(Style.Overflow.HIDDEN);

        add(
                new ViewToolbar("Alimento List", ViewToolbar.group(createBtn, ordenCombo, categoriaFiltro)),
                alimentoGrid);

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
            if (!validarFormulario(nombre, calorias, proteinas, carbohidratos, grasas, porcionBase, unidadMedida,
                    categoria)) {
                Notification.show(
                        "Por favor completa los campos marcados en rojo",
                        3000,
                        Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            try {
                alimentoService.createAlimento(
                        nombre.getValue(),
                        toFloat(calorias.getValue()),
                        toFloat(proteinas.getValue()),
                        toFloat(carbohidratos.getValue()),
                        toFloat(grasas.getValue()),
                        toFloat(porcionBase.getValue()),
                        unidadMedida.getValue(),
                        categoria.getValue());

                alimentoGrid.getDataProvider().refreshAll();
                dialog.close();

                Notification.show("Alimento creado correctamente", 3000,
                        Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
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
                categoria));

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

            if (!validarFormulario(
                    nombreEdit,
                    caloriasEdit,
                    proteinasEdit,
                    carboEdit,
                    grasasEdit,
                    porcionEdit,
                    unidadEdit,
                    categoriaEdit)) {
                Notification.show(
                        "Por favor completa los campos marcados en rojo",
                        3000,
                        Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
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

                Notification.show("Alimento actualizado correctamente", 3000,
                        Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
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
                        categoriaEdit));

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private boolean validarFormulario(
            TextField nombre,
            NumberField calorias,
            NumberField proteinas,
            NumberField carbohidratos,
            NumberField grasas,
            NumberField porcionBase,
            ComboBox<UnidadEnum> unidadMedida,
            ComboBox<CategoriaEnum> categoria) {
        boolean valido = true;

        // Resetear estados de error
        nombre.setInvalid(false);
        calorias.setInvalid(false);
        proteinas.setInvalid(false);
        carbohidratos.setInvalid(false);
        grasas.setInvalid(false);
        porcionBase.setInvalid(false);
        unidadMedida.setInvalid(false);
        categoria.setInvalid(false);

        // Nombre
        if (nombre.isEmpty()) {
            nombre.setErrorMessage("El nombre es obligatorio");
            nombre.setInvalid(true);
            valido = false;
        }

        // Calorías
        if (calorias.getValue() == null) {
            calorias.setErrorMessage("Las calorías son obligatorias");
            calorias.setInvalid(true);
            valido = false;
        }

        // Proteínas
        if (proteinas.getValue() == null) {
            proteinas.setErrorMessage("Las proteínas son obligatorias");
            proteinas.setInvalid(true);
            valido = false;
        }

        // Carbohidratos
        if (carbohidratos.getValue() == null) {
            carbohidratos.setErrorMessage("Los carbohidratos son obligatorios");
            carbohidratos.setInvalid(true);
            valido = false;
        }

        // Grasas
        if (grasas.getValue() == null) {
            grasas.setErrorMessage("Las grasas son obligatorias");
            grasas.setInvalid(true);
            valido = false;
        }

        // Porción base
        if (porcionBase.getValue() == null || porcionBase.getValue() <= 0) {
            porcionBase.setErrorMessage("La porción base es obligatoria y debe ser mayor a 0");
            porcionBase.setInvalid(true);
            valido = false;
        }

        // Unidad
        if (unidadMedida.getValue() == null) {
            unidadMedida.setErrorMessage("Seleccione una unidad de medida");
            unidadMedida.setInvalid(true);
            valido = false;
        }

        // Categoría
        if (categoria.getValue() == null) {
            categoria.setErrorMessage("Seleccione una categoría");
            categoria.setInvalid(true);
            valido = false;
        }

        return valido;
    }

    private void refrescarLista() {
        CategoriaEnum categoria = categoriaFiltro.getValue();
        String orden = ordenCombo.getValue();

        List<Alimento> lista = alimentoService.findAll();

        // Filtrar por categoría
        if (categoria != null) {
            lista = lista.stream()
                    .filter(a -> a.getCategoria() == categoria)
                    .toList();
        }

        // Ordenar
        if ("Nombre A-Z".equals(orden)) {
            lista = lista.stream()
                    .sorted(Comparator.comparing(Alimento::getNombre))
                    .toList();
        } else if ("Nombre Z-A".equals(orden)) {
            lista = lista.stream()
                    .sorted(Comparator.comparing(Alimento::getNombre).reversed())
                    .toList();
        }

        alimentoGrid.setItems(lista);
    }

}