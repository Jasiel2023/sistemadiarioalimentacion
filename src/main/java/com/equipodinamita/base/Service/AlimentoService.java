package com.equipodinamita.base.Service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.equipodinamita.base.Repository.AlimentoRepository;
import com.equipodinamita.base.models.Alimento;
import com.equipodinamita.base.models.CategoriaEnum;
import com.equipodinamita.base.models.UnidadEnum;

@Service //Capa de Negocio
public class AlimentoService {

    private final AlimentoRepository alimentoRepository;

    AlimentoService(AlimentoRepository alimentoRepository) {
        this.alimentoRepository = alimentoRepository;
    }

    @Transactional//Abre transaccion, ejecuta metodo,ok? , realiza estos en SQL por parte de Spring
    public void createAlimento(String nombre, Float calorias, Float proteinas, Float carbohidratos, Float grasas, Float porcionBase, UnidadEnum unidadMedida, CategoriaEnum categoria) {
        validarAlimento(nombre, calorias, proteinas, carbohidratos, grasas, porcionBase, unidadMedida, categoria);
        String nombreNormalizado = nombre.trim();
        //NO MOFIQUES ESTO OE >:(
        if (alimentoRepository.existsByNombreIgnoreCase(nombreNormalizado)) {
            throw new IllegalArgumentException("Ya existe un alimento con ese nombre");
        }
            var alimento = new Alimento(nombre, calorias, proteinas, carbohidratos, grasas, porcionBase, unidadMedida, categoria);
            alimentoRepository.saveAndFlush(alimento);//Guardado ek la base de datos
    }

    public void deleteAlimento(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("El id del alimento no puede ser nulo");
        }
        alimentoRepository.deleteById(id);
    }

    public Alimento updateAlimento(Alimento alimento) {
    if (alimento.getId() == null) {
        throw new IllegalArgumentException("El alimento debe tener ID para editarse");
    }
    
    validarAlimento(
        alimento.getNombre(),
        alimento.getCalorias(),
        alimento.getProteinas(),
        alimento.getCarbohidratos(),
        alimento.getGrasas(),
        alimento.getPorcionBase(),
        alimento.getUnidadMedida(),
        alimento.getCategoria()
    );
    return alimentoRepository.save(alimento);
    }


    @Transactional(readOnly = true)
    public List<Alimento> list(Pageable pageable) {
        return alimentoRepository.findAllBy(pageable).toList();
    }


    public List<Alimento> findAll() {
    return alimentoRepository.findAll();
    }

    private void validarAlimento(
        String nombre,
        Float calorias,
        Float proteinas,
        Float carbohidratos,
        Float grasas,
        Float porcionBase,
        UnidadEnum unidadMedida,
        CategoriaEnum categoria
) {
    if (nombre == null || nombre.trim().isEmpty()) {
        throw new IllegalArgumentException("El nombre del alimento es obligatorio");
    }

    if (calorias == null || calorias < 0) {
        throw new IllegalArgumentException("Las calorías son obligatorias y deben ser >= 0");
    }

    if (proteinas == null || proteinas < 0) {
        throw new IllegalArgumentException("Las proteínas son obligatorias y deben ser >= 0");
    }

    if (carbohidratos == null || carbohidratos < 0) {
        throw new IllegalArgumentException("Los carbohidratos son obligatorios y deben ser >= 0");
    }

    if (grasas == null || grasas < 0) {
        throw new IllegalArgumentException("Las grasas son obligatorias y deben ser >= 0");
    }

    if (porcionBase == null || porcionBase <= 0) {
        throw new IllegalArgumentException("La porción base es obligatoria y debe ser mayor que 0");
    }

    if (unidadMedida == null) {
        throw new IllegalArgumentException("La unidad de medida es obligatoria");
    }

    if (categoria == null) {
        throw new IllegalArgumentException("La categoría es obligatoria");
    }
}


}
