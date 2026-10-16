package com.pequelandia.pequelandia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pequelandia.pequelandia.model.Localidad;

public interface ILocalidadRepository extends JpaRepository<Localidad, Long> {

    // 🔹 Listar todas ordenadas (para selects del panel)
    List<Localidad> findAllByOrderByNombreLocalidadAsc();

    // 🔹 Buscar por nombre (para autocompletado)
    List<Localidad> findByNombreLocalidadContainingIgnoreCase(String nombre);

    // 🔹 Buscar por código postal
    List<Localidad> findByCodigoPostal(int codigoPostal);

    // 🔹 Evitar duplicados en carga de datos
    boolean existsByNombreLocalidadIgnoreCase(String nombre);

    // 🔹 Si querés traer solo nombres (útil para optimizar)
    @Query("SELECT l.nombreLocalidad FROM Localidad l ORDER BY l.nombreLocalidad ASC")
    List<String> listarNombres();
}
