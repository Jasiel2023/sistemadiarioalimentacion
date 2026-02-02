package com.equipodinamita.base.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

// IMPORTANTE: Asegúrate de que aquí NO diga "layout = MainLayout.class"
@Route("") 
@PageTitle("Bienvenido | DataAliment")
public class HomeView extends VerticalLayout {

    public HomeView() {
        // --- 1. CONFIGURACIÓN DEL FONDO (Toda la pantalla) ---
        setSizeFull(); 
        setAlignItems(Alignment.CENTER);       // Centra la tarjeta horizontalmente
        setJustifyContentMode(JustifyContentMode.CENTER); // Centra la tarjeta verticalmente
        addClassName(LumoUtility.Background.CONTRAST_5);  // Color de fondo gris suave

        // --- 2. CREAR LA TARJETA (El rectángulo blanco) ---
        VerticalLayout card = new VerticalLayout();
        card.setWidth("100%");
        card.setMaxWidth("450px");  // Un ancho un poco más estrecho para que se vea elegante
        card.setPadding(true);
        card.setSpacing(true);
        card.setAlignItems(Alignment.CENTER); // Centra el texto y botones DENTRO de la tarjeta

        // Estilos visuales: Sombra, fondo blanco y bordes redondeados
        card.addClassNames(
            LumoUtility.Background.BASE,
            LumoUtility.BoxShadow.LARGE,
            LumoUtility.BorderRadius.LARGE,
            LumoUtility.Padding.XLARGE
        );

        // --- 3. CONTENIDO DE TEXTO ---
        H1 titulo = new H1("DataAliment");
        titulo.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.FontSize.XXLARGE);
        titulo.getStyle().set("margin-bottom", "0"); // Quitar margen extra

        H2 subtitulo = new H2("Tu control diario");
        subtitulo.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.TextColor.SECONDARY);
        subtitulo.getStyle().set("margin-top", "5px");

        Paragraph descripcion = new Paragraph(
            "Sistema inteligente para el seguimiento de tu alimentación y control calórico."
        );
        descripcion.addClassName(LumoUtility.TextAlignment.CENTER);
        descripcion.setWidthFull();

        // --- 4. BOTONES ARREGLADOS ---
        // Usamos VerticalLayout para los botones. Así nunca se salen del ancho.
        
        Button btnLogin = new Button("Iniciar Sesión");
        btnLogin.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnLogin.setWidthFull(); // Ocupa todo el ancho disponible de la tarjeta
        btnLogin.addClickListener(e -> UI.getCurrent().navigate("login"));

        Button btnRegistro = new Button("Registrarse");
        btnRegistro.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnRegistro.setWidthFull(); // Ocupa todo el ancho disponible
        btnRegistro.addClickListener(e -> UI.getCurrent().navigate("registro"));

        // Contenedor de botones (Vertical para que queden uno sobre otro, limpio y ordenado)
        VerticalLayout botonesArea = new VerticalLayout(btnLogin, btnRegistro);
        botonesArea.setWidthFull();
        botonesArea.setPadding(false); // Quitamos padding extra interno
        botonesArea.setSpacing(true);  // Espacio entre botones

        // --- 5. AGREGAR TODO A LA TARJETA ---
        card.add(titulo, subtitulo, descripcion, botonesArea);
        
        // Agregar tarjeta a la vista
        add(card);
    }
}