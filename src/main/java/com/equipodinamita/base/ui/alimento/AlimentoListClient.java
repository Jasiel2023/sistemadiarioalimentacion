package com.equipodinamita.base.ui.alimento;

import com.equipodinamita.base.Service.AlimentoService;
import com.equipodinamita.base.models.Alimento;
import com.equipodinamita.base.ui.MainLayout;
import com.equipodinamita.base.ui.ViewToolbar;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route(value = "alimentos_usuario", layout = MainLayout.class)
@PageTitle("Alimentos_usuario")
// @Menu(order = 1, icon = "vaadin:list", title = "Alimentos")

class AlimentoListClient extends VerticalLayout {

        private final AlimentoService alimentoService;
        private final Grid<Alimento> alimentoGrid;

        AlimentoListClient(AlimentoService alimentoService) {
                this.alimentoService = alimentoService;

                alimentoGrid = new Grid<>();
                alimentoGrid.setItems(
                                query -> alimentoService
                                                .list(toSpringPageRequest(query))
                                                .stream());

                alimentoGrid.addColumn(Alimento::getNombre).setHeader("Nombre");
                alimentoGrid.addColumn(Alimento::getCalorias).setHeader("Calorías");
                alimentoGrid.addColumn(Alimento::getProteinas).setHeader("Proteínas");
                alimentoGrid.addColumn(Alimento::getCarbohidratos).setHeader("Carbohidratos");
                alimentoGrid.addColumn(Alimento::getGrasas).setHeader("Grasas");
                alimentoGrid.addColumn(Alimento::getPorcionBase).setHeader("Porción Base");

                alimentoGrid.addColumn(a -> a.getCategoria() != null ? a.getCategoria().name() : "")
                                .setHeader("Categoría");

                alimentoGrid.addColumn(a -> a.getUnidadMedida() != null ? a.getUnidadMedida().name() : "")
                                .setHeader("Unidad de Medida");

                alimentoGrid.setEmptyStateText("No hay alimentos registrados");
                alimentoGrid.setSizeFull();
                alimentoGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

                setSizeFull();
                setPadding(false);
                setSpacing(false);
                getStyle().setOverflow(Style.Overflow.HIDDEN);

                add(
                                new ViewToolbar("Listado de Alimentos"),
                                alimentoGrid);
        }
}