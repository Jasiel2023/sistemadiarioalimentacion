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

@Layout
public final class MainLayout extends AppLayout implements AfterNavigationObserver{
    private SideNav nav;
    MainLayout() {
        setPrimarySection(Section.DRAWER);
        nav = new SideNav();
        addToDrawer(createHeader(), new Scroller(nav), createFooter());
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        String rutaActual = event.getLocation().getPath();
        if ("login".equals(rutaActual)) {
            return;
        }
        regenerarMenu();
    }

    private void regenerarMenu() {
        nav.removeAll();
        Cuenta cuenta = VaadinSession.getCurrent().getAttribute(Cuenta.class);
        if (cuenta == null || cuenta.getPersona() == null) {
            System.out.println("Redirigiendo a login desde MainLayout");
            UI.getCurrent().getPage().setLocation("login");
            return;
    }

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

    /*private SideNav createSideNav() {
        var nav = new SideNav();
        nav.addClassNames(LumoUtility.Margin.Horizontal.MEDIUM);

        Cuenta cuenta = VaadinSession.getCurrent().getAttribute(Cuenta.class);
        
        // --- DEBUG: MIRA ESTO EN LA CONSOLA DE TU IDE ---
        System.out.println("--- DEBUG MENU ---");
        if (cuenta == null) {
            System.out.println("ERROR: La cuenta es NULL. La sesión no se guardó.");
        } else {
            System.out.println("Usuario logueado: " + cuenta.getEmail());
            if (cuenta.getPersona() == null) {
                 System.out.println("ERROR: La cuenta tiene PERSONA NULL.");
            } else {
                 System.out.println("Rol detectado: " + cuenta.getPersona().getRol());
            }
        }
        if (cuenta == null || cuenta.getPersona() == null) {
            return nav;
        }

        RolEnum rol = cuenta.getPersona().getRol();
        if (rol == RolEnum.ADMIN) {
            nav.addItem(new SideNavItem("Gestion Alimentos", "alimentos", new Icon(VaadinIcon.DATABASE)));
           
        } else if (rol == RolEnum.CLIENTE) {
            nav.addItem(new SideNavItem("Registro de Consumo", "registro-consumo", new Icon(VaadinIcon.CHART)));
            nav.addItem(new SideNavItem("Alimentos_usuario", "alimentos_usuario", new Icon(VaadinIcon.LIST)));

        }
        MenuConfiguration.getMenuEntries().forEach(entry -> nav.addItem(createSideNavItem(entry)));
        return nav;
    }

    private SideNavItem createSideNavItem(MenuEntry menuEntry) {
        if (menuEntry.icon() != null) {
            return new SideNavItem(menuEntry.title(), menuEntry.path(), new Icon(menuEntry.icon()));
        } else {
            return new SideNavItem(menuEntry.title(), menuEntry.path());
        }
    }*/

    private Component createFooter(){

        Button logoutButton = new Button("Cerrar Sesion", new Icon(VaadinIcon.SIGN_OUT));
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        logoutButton.setWidthFull();
        logoutButton.addClickListener(event -> {
            VaadinSession.getCurrent().getSession().invalidate();
            UI.getCurrent().getPage().setLocation("login");
        });

        var footer = new VerticalLayout(logoutButton);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        return footer;
    }
}
