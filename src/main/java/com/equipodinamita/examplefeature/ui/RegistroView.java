package com.equipodinamita.examplefeature.ui;

import java.sql.Date;

import com.equipodinamita.controller.services.CuentaServices;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("registro")
@PageTitle("Crear Cuenta")
@AnonymousAllowed
public class RegistroView extends VerticalLayout {

    private final CuentaServices cuentaServices;
    public RegistroView(CuentaServices cuentaServices) {
        this.cuentaServices = cuentaServices;
        
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Titulo
        H2 title = new H2("Registrate en DataAliment");

        TextField nombre = new TextField("Nombre");
        nombre.setWidth("300px");
        TextField apellido = new TextField("Apellido"); 
        apellido.setWidth("300px");
        TextField estaturaCm = new TextField("Estatura (cm)");
        estaturaCm.setWidth("300px");
        TextField pesoKg = new TextField("Peso (kg)");
        pesoKg.setWidth("300px");
        TextField fechaNacimiento = new TextField("Fecha de Nacimiento (YYYY-MM-DD)");
        fechaNacimiento.setWidth("300px");
        TextField telefono = new TextField("Teléfono");
        telefono.setWidth("300px");
        EmailField email = new EmailField("Correo Electronico");
        email.setWidth("300px");
        email.setErrorMessage("Por favor ingresa un correo electrónico válido.");

        PasswordField password = new PasswordField("Contraseña");
        password.setWidth("300px");
        PasswordField confirmPassword = new PasswordField("Confirmar Contraseña");
        confirmPassword.setWidth("300px");

        Button registerBtn = new Button("Crear Cuenta");
        registerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerBtn.setWidth("300px");

        Button loginlink = new Button("¿Ya tienes una cuenta? Inicia sesión", e -> {
            UI.getCurrent().navigate("login");
        });
        loginlink.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        registerBtn.addClickListener(event -> {
            if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() || password.isEmpty()|| estaturaCm.isEmpty() || pesoKg.isEmpty() || fechaNacimiento.isEmpty() || telefono.isEmpty() || password.isEmpty()) {
                Notification.show("Por favor completa todos los campos.");
                return;
            } 
             if (!password.getValue().equals(confirmPassword.getValue())) {
                Notification.show("Las contraseñas no coinciden.")
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } 

            boolean exito = cuentaServices.registrar(
                nombre.getValue(),
                apellido.getValue(),
                Float.parseFloat(estaturaCm.getValue()),
                Float.parseFloat(pesoKg.getValue()),
                Date.valueOf(fechaNacimiento.getValue()),
                telefono.getValue(),
                email.getValue(), 
                password.getValue()
            );

            if (exito) {
                Notification.show("Cuenta creada exitosamente. Ahora puedes iniciar sesión.")
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                UI.getCurrent().navigate("login");
            } else {
                Notification.show("El correo electrónico ya está en uso. Por favor utiliza otro.")
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        add(title, nombre, apellido, email, estaturaCm, pesoKg, telefono, fechaNacimiento, password, confirmPassword, registerBtn, loginlink );
    }
}
