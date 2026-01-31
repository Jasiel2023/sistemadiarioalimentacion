package com.equipodinamita.base.ui.alimento;

import com.equipodinamita.base.Service.AlimentoService;
import com.equipodinamita.base.models.Alimento;
import com.equipodinamita.base.ui.ViewToolbar;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("alimentos_usuario")
@PageTitle("Alimentos_usuario")
@Menu(order = 1, icon = "vaadin:list", title = "Alimentos")
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

                // Botón Ver Consumo
                Button btnVerConsumo = new Button("Consultar Consumo", new Icon(VaadinIcon.CLIPBOARD_TEXT));
                btnVerConsumo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                btnVerConsumo.getStyle()
                                .set("background-color", "#6a419d")
                                .set("color", "#ffffff")
                                .set("border-radius", "8px")
                                .set("padding", "10px 20px");
                btnVerConsumo.addClickListener(e -> UI.getCurrent().navigate("calendario-registro"));

                HorizontalLayout buttonContainer = new HorizontalLayout(btnVerConsumo);
                buttonContainer.setWidthFull();
                buttonContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
                buttonContainer.getStyle().set("padding", "20px");

                add(
                                new ViewToolbar("Listado de Alimentos"),
                                alimentoGrid,
                                buttonContainer);
        }
}