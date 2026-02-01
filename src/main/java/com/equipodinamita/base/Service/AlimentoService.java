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
        if (unidadMedida == null || categoria == null) {
        throw new IllegalArgumentException(
            "La unidad de medida y la categoría son obligatorias"
        );
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
    return alimentoRepository.save(alimento);
    }


    @Transactional(readOnly = true)
    public List<Alimento> list(Pageable pageable) {
        return alimentoRepository.findAllBy(pageable).toList();
    }


    public List<Alimento> findAll() {
    return alimentoRepository.findAll();
    }

}
