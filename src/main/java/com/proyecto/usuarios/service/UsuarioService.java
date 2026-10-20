package com.proyecto.usuarios.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.proyecto.usuarios.dto.request.UsuarioBusquedaRequest;
import com.proyecto.usuarios.dto.request.UsuarioConsultaRequest;
import com.proyecto.usuarios.dto.request.UsuarioCreateRequest;
import com.proyecto.usuarios.dto.request.UsuarioEliminacionRequest;
import com.proyecto.usuarios.dto.request.UsuarioUpdateRequest;
import com.proyecto.usuarios.dto.response.UsuarioResponse;

/**
 * Service: contrato de las operaciones de negocio sobre usuarios.
 */
public interface UsuarioService {

    // 1. Registro
    UsuarioResponse crear(UsuarioCreateRequest request);

    // 2. Consultas
    Page<UsuarioResponse> listar(Pageable pageable);

    Page<UsuarioResponse> buscar(UsuarioBusquedaRequest request);

    UsuarioResponse consultar(UsuarioConsultaRequest request);

    // 3. Actualizacion parcial
    UsuarioResponse actualizar(UsuarioUpdateRequest request);

    // 4. Eliminacion
    void eliminar(UsuarioEliminacionRequest request);

}
