package com.equipodinamita.examplefeature.ui;

import com.equipodinamita.base.models.Cuenta;

import com.equipodinamita.base.models.Persona;
import com.equipodinamita.base.models.RolEnum;
import com.equipodinamita.controller.services.CuentaServices;
import com.vaadin.copilot.shaded.checkerframework.checker.units.qual.N;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route("login")
public class LoginView extends Composite<LoginOverlay> {
    
     private final CuentaServices cuentaServices;

    public LoginView(CuentaServices cuentaServices) {
        this.cuentaServices = cuentaServices;
        LoginOverlay loginOverlay = getContent();
        loginOverlay.setTitle("DataAliment");
        loginOverlay.setDescription("Inicia sesión con tu correo electrónico y contraseña");
        loginOverlay.setOpened(true);
        Persona persona = null;


        loginOverlay.addLoginListener( event -> {
            /*String email = event.getUsername();
            String password = event.getPassword();
            if (cuentaServices.autenticar(email, password)) {
                Notification.show("Login successful");
                loginOverlay.close();
                UI.getCurrent().navigate("");
            }else if (cuentaServices.existsByEmail(email)) {
                Notification.show("Login failed: Invalid password");
            }else{
                Notification.show("Login failed: Invalid credentials");
            }*/
           Cuenta cuenta = this.cuentaServices.autenticar(event.getUsername(), event.getPassword());
           
           if (cuenta != null) {
            VaadinSession.getCurrent().setAttribute(Cuenta.class, cuenta);
            Notification.show("Login successful");
            loginOverlay.close();
            RolEnum rolDelUsuario = persona.getRol();
            if (rolDelUsuario == RolEnum.ADMIN) {
                UI.getCurrent().navigate("");
                Notification.show("Bienvenido Administrador");
            } else if (rolDelUsuario == RolEnum.CLIENTE) {
                 UI.getCurrent().navigate("");
                Notification.show("Bienvenido Cliente");
            }
           
        } else {
            loginOverlay.setError(true);
           }
        });

}
}
