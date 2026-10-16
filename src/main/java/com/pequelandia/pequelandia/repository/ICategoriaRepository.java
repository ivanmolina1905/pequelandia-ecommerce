package com.pequelandia.pequelandia.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pequelandia.pequelandia.model.Categoria;

public interface ICategoriaRepository extends JpaRepository<Categoria, Long> {

    // ✔ Buscar categoría por nombre (evita duplicados)
    Optional<Categoria> findByNombreCategoriaIgnoreCase(String nombre_categoria);

    // ✔ Ver si ya existe nombre (útil cuando creás desde el panel)
    boolean existsByNombreCategoriaIgnoreCase(String nombre_categoria);

    // ✔ Listar ordenadas alfabéticamente (para el panel)
    @Query("SELECT c FROM Categoria c ORDER BY c.nombreCategoria ASC")
    List<Categoria> findAllOrdenadas();
}
