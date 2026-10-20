package com.proyecto.usuarios.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.proyecto.usuarios.entity.Usuario;

/**
 * Repository: acceso a la base de datos mediante Spring Data JPA, incluidas las búsquedas dinámicas.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    // 1. Llave compuesta: WHERE nombre = :nombre AND correo = :correo
    Optional<Usuario> findByNombreAndCorreo(String nombre, String correo);

    // 2. Unicidad del correo al registrar
    boolean existsByCorreo(String correo);

    // 3. Unicidad del correo al actualizar (excluye al propio usuario)
    boolean existsByCorreoAndIdNot(String correo, Long id);

}
