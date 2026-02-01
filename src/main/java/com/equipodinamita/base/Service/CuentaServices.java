package com.equipodinamita.base.Service;


import java.sql.Date;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.equipodinamita.base.models.Cuenta;
import com.equipodinamita.base.models.Persona;
import com.equipodinamita.base.models.RolEnum;
import com.equipodinamita.base.Repository.CuentaRepository;
import com.equipodinamita.base.Repository.PersonaRepository;



@Service
public class CuentaServices {
    
    private final CuentaRepository cuentaRepository;
        private final PersonaRepository personaRepository;

    public CuentaServices(CuentaRepository cuentaRepository, PersonaRepository personaRepository) {
        this.cuentaRepository = cuentaRepository;
        this.personaRepository = personaRepository;
    }


      //Login

    public Cuenta autenticar(String email, String password ){
        Optional<Cuenta> usuarioEncontrado = cuentaRepository.findByEmail(email);
        
        if (usuarioEncontrado.isPresent()) {
            Cuenta cuenta = usuarioEncontrado.get();
            if (cuenta.getPassword().equals(password)) {
                return cuenta;
            }
        }
        return null; 
    }

   
    @Transactional
    public boolean registrar(String nombre, String apellido, Float estaturaCm, Float pesoKg, Date fechaNacimiento, String telefono, String email, String password) {
        if (cuentaRepository.findByEmail(email).isPresent()) {
            return false; 
        }
        Persona nuevaPersona = new Persona();
        nuevaPersona.setNombre(nombre);
        nuevaPersona.setApellido(apellido);
        nuevaPersona.setEstaturaCm(estaturaCm);
        nuevaPersona.setPesoKg(pesoKg);
        nuevaPersona.setFechaNacimiento(fechaNacimiento);
        nuevaPersona.setTelefono(telefono);
        nuevaPersona.setRol(RolEnum.CLIENTE);

        personaRepository.save(nuevaPersona);

        Cuenta nuevaCuenta = new Cuenta();
        nuevaCuenta.setEmail(email);
        nuevaCuenta.setPassword(password);
        nuevaCuenta.setPersona(nuevaPersona);
        System.out.println("GUARDANDO CUENTA...");
   
        nuevaCuenta.setEstado(true);
         System.out.println("Email: " + nuevaCuenta.getEmail());
    System.out.println("Estado antes de guardar: " + nuevaCuenta.getEstado());
        cuentaRepository.save(nuevaCuenta);

        return true;
    }
}
