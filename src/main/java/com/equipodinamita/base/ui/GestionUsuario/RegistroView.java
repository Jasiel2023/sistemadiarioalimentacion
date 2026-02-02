package com.equipodinamita.base.ui.GestionUsuario;

import java.sql.Date;
import java.time.LocalDate;

import com.equipodinamita.base.Service.CuentaServices;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("registro")
@PageTitle("Crear Cuenta")
//@Menu(order = 1, icon = "vaadin:list", title = "Registro")
@AnonymousAllowed
public class RegistroView extends VerticalLayout {

    private final CuentaServices cuentaServices;
    
    // Usamos un Binder genérico para manejar la validación del formulario completo
    private final Binder<Void> binder = new Binder<>();

    public RegistroView(CuentaServices cuentaServices) {
        this.cuentaServices = cuentaServices;

        // --- CONFIGURACIÓN VISUAL ---
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background-color", "#f5f5f5");

        VerticalLayout card = new VerticalLayout();
        card.setWidth("100%");
        card.setMaxWidth("800px");
        card.setPadding(true);
        card.setSpacing(true);
        card.setAlignItems(Alignment.STRETCH);
        card.getStyle().set("background-color", "#ffffff");
        card.getStyle().set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)");
        card.getStyle().set("border-radius", "12px");
        card.getStyle().set("padding", "40px");

        H2 title = new H2("Regístrate en DataAliment");
        title.getStyle().set("text-align", "center");
        title.getStyle().set("color", "#2c3e50");

        // --- DEFINICIÓN DE CAMPOS CON RESTRICCIONES ---

        TextField nombre = new TextField("Nombre");
        TextField apellido = new TextField("Apellido");

        EmailField email = new EmailField("Correo Electrónico");
        email.setPlaceholder("ejemplo@correo.com");
        // ValueChangeMode.EAGER valida mientras escribes, no solo al salir del campo
        email.setValueChangeMode(ValueChangeMode.EAGER);

        TextField telefono = new TextField("Teléfono");
        telefono.setPlaceholder("Solo números");
        // PREVENCIÓN: Solo permite escribir dígitos (0-9). El usuario no podrá escribir letras.
        telefono.setAllowedCharPattern("[0-9]"); 
        telefono.setMaxLength(10); // Límite de caracteres (ej. para celular estándar)

        DatePicker fechaNacimiento = new DatePicker("Fecha de Nacimiento");
        fechaNacimiento.setMax(LocalDate.now().minusYears(1)); // Debe tener al menos 1 año
        fechaNacimiento.setHelperText("Debes seleccionar una fecha válida");

        NumberField estaturaCm = new NumberField("Estatura");
        estaturaCm.setSuffixComponent(new Div(new Text("cm")));
        // PREVENCIÓN: Límites lógicos (Nadie mide 0 o 3 metros)
        estaturaCm.setMin(30); 
        estaturaCm.setMax(250);
        estaturaCm.setStepButtonsVisible(true); // Botones +/- para ajuste fino

        NumberField pesoKg = new NumberField("Peso");
        pesoKg.setSuffixComponent(new Div(new Text("kg")));
        // PREVENCIÓN: Límites lógicos
        pesoKg.setMin(2);
        pesoKg.setMax(300);
        pesoKg.setStepButtonsVisible(true);

        PasswordField password = new PasswordField("Contraseña");
        password.setValueChangeMode(ValueChangeMode.EAGER);
        
        PasswordField confirmPassword = new PasswordField("Confirmar Contraseña");
        confirmPassword.setValueChangeMode(ValueChangeMode.EAGER);

        // --- CONFIGURACIÓN DEL BINDER (VALIDACIÓN LÓGICA) ---
        
        // Nombre: Obligatorio
        binder.forField(nombre)
            .asRequired("El nombre es obligatorio")
            .bind(v -> null, (v, k) -> {});

        // Apellido: Obligatorio
        binder.forField(apellido)
            .asRequired("El apellido es obligatorio")
            .bind(v -> null, (v, k) -> {});

        // Email: Debe tener formato de correo
        binder.forField(email)
            .asRequired("El correo es obligatorio")
            .withValidator(new EmailValidator("Formato de correo incorrecto"))
            .bind(v -> null, (v, k) -> {});

        // Teléfono: Debe tener longitud mínima (ej. 7) y solo números
        binder.forField(telefono)
            .asRequired("El teléfono es obligatorio")
            .withValidator(t -> t.length() >= 7, "El teléfono debe tener al menos 7 dígitos")
            .bind(v -> null, (v, k) -> {});

        // Fecha: Obligatoria y no nula
        binder.forField(fechaNacimiento)
            .asRequired("La fecha es obligatoria")
            .bind(v -> null, (v, k) -> {});

        // Estatura: Obligatoria y rango (doble chequeo)
        binder.forField(estaturaCm)
            .asRequired("La estatura es requerida")
            .withValidator(val -> val != null && val >= 30 && val <= 250, "Altura inválida (30-250cm)")
            .bind(v -> null, (v, k) -> {});

        // Peso: Obligatorio
        binder.forField(pesoKg)
            .asRequired("El peso es requerido")
            .withValidator(val -> val != null && val > 0, "Peso inválido")
            .bind(v -> null, (v, k) -> {});

        // Password: Mínimo 4 caracteres
        binder.forField(password)
            .asRequired("La contraseña es obligatoria")
            .withValidator(new StringLengthValidator("Mínimo 4 caracteres", 4, 20))
            .bind(v -> null, (v, k) -> {});

        // Confirm Password: Debe coincidir con Password
        binder.forField(confirmPassword)
            .asRequired("Confirma tu contraseña")
            .withValidator((val, context) -> {
                if (val != null && val.equals(password.getValue())) {
                    return ValidationResult.ok();
                }
                return ValidationResult.error("Las contraseñas no coinciden");
            })
            .bind(v -> null, (v, k) -> {});

        // Truco: Cuando cambie password, revalidar confirmPassword
        password.addValueChangeListener(e -> binder.validate());


        // --- BOTONES ---
        
        Button registerBtn = new Button("Crear Cuenta");
        registerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerBtn.setWidthFull();
        registerBtn.setEnabled(false); // [IMPORTANTE] Deshabilitado por defecto

        // Listener: Cada vez que el binder cambia de estado, revisamos si todo es válido
        binder.addStatusChangeListener(event -> {
            // Habilita el botón SOLO si no hay errores y todos los campos requeridos están llenos
            boolean isValid = binder.isValid();
            registerBtn.setEnabled(isValid); 
        });

        Button loginLink = new Button("¿Ya tienes cuenta? Inicia sesión", e -> {
            UI.getCurrent().navigate("login");
        });
        loginLink.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        loginLink.setWidthFull();

        // --- LAYOUT ---
        FormLayout formLayout = new FormLayout();
        formLayout.add(nombre, apellido, email, telefono, fechaNacimiento, estaturaCm, pesoKg);
        formLayout.add(password, confirmPassword);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        // --- ACCIÓN DEL BOTÓN ---
        registerBtn.addClickListener(event -> {
            // Doble seguridad: Si por alguna razón el botón se activó pero hay errores
            if (!binder.isValid()) { 
                Notification.show("Corrige los errores antes de continuar");
                return;
            }

            try {
                boolean exito = cuentaServices.registrar(
                    nombre.getValue(),
                    apellido.getValue(),
                    estaturaCm.getValue().floatValue(),
                    pesoKg.getValue().floatValue(),
                    Date.valueOf(fechaNacimiento.getValue()),
                    telefono.getValue(),
                    email.getValue(), 
                    password.getValue()
                );
    
                if (exito) {
                    Notification.show("Cuenta creada exitosamente. Inicia sesión.")
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    UI.getCurrent().navigate("login");
                } else {
                    Notification.show("El correo ya está registrado.")
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } catch (Exception ex) {
                Notification.show("Error interno: " + ex.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        card.add(title, formLayout, new Div(), registerBtn, loginLink);
        add(card);
    }
}