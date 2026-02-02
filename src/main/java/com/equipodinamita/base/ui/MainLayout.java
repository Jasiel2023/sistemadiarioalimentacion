package com.equipodinamita.base.ui;

import com.equipodinamita.base.models.Cuenta;
import com.equipodinamita.base.models.RolEnum;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.model.Side;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin.Minus.Vertical;

@Layout
public final class MainLayout extends AppLayout implements AfterNavigationObserver{
    private SideNav nav;
    private VerticalLayout footer;
    MainLayout() {
        setPrimarySection(Section.DRAWER);
        nav = new SideNav();
        footer = createFooter();
        addToDrawer(createHeader(), new Scroller(nav), footer);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        String rutaActual = event.getLocation().getPath();
        if ("login".equals(rutaActual) || "".equals(rutaActual) || "registro".equals(rutaActual)) {
            actualizarVisibilidad(false);

            Cuenta cuenta = VaadinSession.getCurrent().getAttribute(Cuenta.class);
            if (cuenta != null) {
                regenerarMenu();
            }
            return;
        }
        regenerarMenu();
    }

    private void actualizarVisibilidad(boolean visible) {
        footer.setVisible(visible);
        nav.setVisible(visible);

    }
    private void regenerarMenu() {
        nav.removeAll();
        Cuenta cuenta = VaadinSession.getCurrent().getAttribute(Cuenta.class);
        if (cuenta == null || cuenta.getPersona() == null) {
            actualizarVisibilidad(false);
            System.out.println("Redirigiendo a login desde MainLayout");
            UI.getCurrent().getPage().setLocation("login");
            return;
    }
    actualizarVisibilidad(true);
    System.out.println("Generando Menú para usuario: " + cuenta.getEmail());

    RolEnum rol = cuenta.getPersona().getRol();
    if (rol == RolEnum.ADMIN) {
        nav.addItem(new SideNavItem("Gestion Alimentos", "alimentos", new Icon(VaadinIcon.DATABASE)));
       
    } else if (rol == RolEnum.CLIENTE) {
        nav.addItem(new SideNavItem("Registro de Consumo", "registro-consumo", new Icon(VaadinIcon.CHART)));
        nav.addItem(new SideNavItem("Alimentos_usuario", "alimentos_usuario", new Icon(VaadinIcon.LIST)));
    }
}
    private Component createHeader() {
        // TODO Replace with real application logo and name
        var appLogo = VaadinIcon.CUBES.create();
        appLogo.setSize("48px");
        appLogo.setColor("green");

        var appName = new Span("DataAliment");
        appName.getStyle().setFontWeight(Style.FontWeight.BOLD);

        var header = new VerticalLayout(appLogo, appName);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        return header;
    }

    

    private VerticalLayout createFooter(){
        Button logoutButton = new Button("Cerrar Sesion", new Icon(VaadinIcon.SIGN_OUT));
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        logoutButton.setWidthFull();
        logoutButton.addClickListener(event -> {
            VaadinSession.getCurrent().getSession().invalidate();
            UI.getCurrent().getPage().setLocation("login");
        });

       VerticalLayout layout = new VerticalLayout(logoutButton);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setPadding(true);
        
        return layout;
    }
}
