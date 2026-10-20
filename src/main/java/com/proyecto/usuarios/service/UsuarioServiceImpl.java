package com.proyecto.usuarios.service;

import java.util.Locale;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.usuarios.dto.request.UsuarioBusquedaRequest;
import com.proyecto.usuarios.dto.request.UsuarioConsultaRequest;
import com.proyecto.usuarios.dto.request.UsuarioCreateRequest;
import com.proyecto.usuarios.dto.request.UsuarioEliminacionRequest;
import com.proyecto.usuarios.dto.request.UsuarioUpdateRequest;
import com.proyecto.usuarios.dto.response.UsuarioResponse;
import com.proyecto.usuarios.entity.Usuario;
import com.proyecto.usuarios.exception.GlobalExceptionHandler.CorreoDuplicadoException;
import com.proyecto.usuarios.exception.GlobalExceptionHandler.UsuarioNoEncontradoException;
import com.proyecto.usuarios.repository.UsuarioRepository;
import com.proyecto.usuarios.specification.UsuarioSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service: implementa las reglas de negocio (existencia del usuario, unicidad del correo y coordinación del CRUD).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private static final Sort ORDEN_POR_DEFECTO = Sort.by(Sort.Direction.DESC, "fechaCreacion")
            .and(Sort.by(Sort.Direction.DESC, "id"));
    private static final int PAGINA_POR_DEFECTO = 0;
    private static final int TAMANIO_POR_DEFECTO = 10;

    private final UsuarioRepository usuarioRepository;

    // 1. Registro: valida unicidad del correo y deja al servidor los campos controlados
    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioCreateRequest request) {
        String correo = normalizarCorreo(request.getCorreo());
        validarCorreoDisponible(correo);

        Usuario usuario = new Usuario();
        usuario.setNombre(limpiar(request.getNombre()));
        usuario.setSegundoNombre(limpiar(request.getSegundoNombre()));
        usuario.setApellidoPaterno(limpiar(request.getApellidoPaterno()));
        usuario.setApellidoMaterno(limpiar(request.getApellidoMaterno()));
        usuario.setFechaNacimiento(request.getFechaNacimiento());
        usuario.setCurp(mayusculas(request.getCurp()));
        usuario.setRfc(mayusculas(request.getRfc()));
        usuario.setSexo(request.getSexo());
        usuario.setNacionalidad(request.getNacionalidad());
        usuario.setEstadoCivil(request.getEstadoCivil());
        usuario.setCorreo(correo);
        usuario.setTelefono(limpiar(request.getTelefono()));

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("[Usuario] Registrado con id {}", guardado.getId());
        return toResponse(guardado);
    }

    // 2. Listado general paginado (el orden lo define el servidor)
    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(Pageable pageable) {
        Pageable pagina = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), ORDEN_POR_DEFECTO);
        return usuarioRepository.findAll(pagina).map(this::toResponse);
    }

    // 3. Busqueda dinamica: la Specification arma la consulta y la BD filtra y pagina
    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResponse> buscar(UsuarioBusquedaRequest request) {
        int pagina = Objects.requireNonNullElse(request.getPagina(), PAGINA_POR_DEFECTO);
        int tamanio = Objects.requireNonNullElse(request.getTamanio(), TAMANIO_POR_DEFECTO);
        Pageable pageable = PageRequest.of(pagina, tamanio, ORDEN_POR_DEFECTO);

        return usuarioRepository.findAll(UsuarioSpecification.conFiltros(request), pageable)
                .map(this::toResponse);
    }

    // 4. Consulta por llave compuesta
    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse consultar(UsuarioConsultaRequest request) {
        return toResponse(obtenerPorLlave(request.getNombre(), request.getCorreo()));
    }

    // 5. Actualizacion parcial: localiza por la llave actual y solo cambia lo enviado
    @Override
    @Transactional
    public UsuarioResponse actualizar(UsuarioUpdateRequest request) {
        Usuario usuario = obtenerPorLlave(request.getNombre(), request.getCorreo());
        aplicarCambios(usuario, request);

        Usuario actualizado = usuarioRepository.save(usuario);
        log.info("[Usuario] Actualizado id {}", actualizado.getId());
        return toResponse(actualizado);
    }

    // 6. Eliminacion fisica por llave compuesta
    @Override
    @Transactional
    public void eliminar(UsuarioEliminacionRequest request) {
        Usuario usuario = obtenerPorLlave(request.getNombre(), request.getCorreo());
        usuarioRepository.delete(usuario);
        log.info("[Usuario] Eliminado id {}", usuario.getId());
    }

    private Usuario obtenerPorLlave(String nombre, String correo) {
        return usuarioRepository.findByNombreAndCorreo(limpiar(nombre), normalizarCorreo(correo))
                .orElseThrow(UsuarioNoEncontradoException::new);
    }

    private void validarCorreoDisponible(String correo) {
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new CorreoDuplicadoException();
        }
    }

    // Los valores null no sobrescriben datos, salvo segundoNombre enviado explicitamente con null
    private void aplicarCambios(Usuario usuario, UsuarioUpdateRequest request) {
        if (request.getNuevoCorreo() != null) {
            String nuevoCorreo = normalizarCorreo(request.getNuevoCorreo());
            if (!nuevoCorreo.equals(usuario.getCorreo())
                    && usuarioRepository.existsByCorreoAndIdNot(nuevoCorreo, usuario.getId())) {
                throw new CorreoDuplicadoException();
            }
            usuario.setCorreo(nuevoCorreo);
        }
        if (request.getNuevoNombre() != null) {
            usuario.setNombre(limpiar(request.getNuevoNombre()));
        }
        if (request.isSegundoNombreEnviado()) {
            usuario.setSegundoNombre(limpiar(request.getSegundoNombre()));
        }
        if (request.getApellidoPaterno() != null) {
            usuario.setApellidoPaterno(limpiar(request.getApellidoPaterno()));
        }
        if (request.getApellidoMaterno() != null) {
            usuario.setApellidoMaterno(limpiar(request.getApellidoMaterno()));
        }
        if (request.getFechaNacimiento() != null) {
            usuario.setFechaNacimiento(request.getFechaNacimiento());
        }
        if (request.getCurp() != null) {
            usuario.setCurp(mayusculas(request.getCurp()));
        }
        if (request.getRfc() != null) {
            usuario.setRfc(mayusculas(request.getRfc()));
        }
        if (request.getSexo() != null) {
            usuario.setSexo(request.getSexo());
        }
        if (request.getNacionalidad() != null) {
            usuario.setNacionalidad(request.getNacionalidad());
        }
        if (request.getEstadoCivil() != null) {
            usuario.setEstadoCivil(request.getEstadoCivil());
        }
        if (request.getTelefono() != null) {
            usuario.setTelefono(limpiar(request.getTelefono()));
        }
        if (request.getActivo() != null) {
            usuario.setActivo(request.getActivo());
        }
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .segundoNombre(usuario.getSegundoNombre())
                .apellidoPaterno(usuario.getApellidoPaterno())
                .apellidoMaterno(usuario.getApellidoMaterno())
                .fechaNacimiento(usuario.getFechaNacimiento())
                .curp(usuario.getCurp())
                .rfc(usuario.getRfc())
                .sexo(usuario.getSexo())
                .nacionalidad(usuario.getNacionalidad())
                .estadoCivil(usuario.getEstadoCivil())
                .correo(usuario.getCorreo())
                .telefono(usuario.getTelefono())
                .activo(usuario.getActivo())
                .fechaCreacion(usuario.getFechaCreacion())
                .build();
    }

    private static String limpiar(String valor) {
        return valor == null ? null : valor.trim();
    }

    private static String mayusculas(String valor) {
        return valor == null ? null : valor.trim().toUpperCase(Locale.ROOT);
    }

    // El correo se guarda y se compara siempre en minusculas
    private static String normalizarCorreo(String correo) {
        return correo == null ? null : correo.trim().toLowerCase(Locale.ROOT);
    }

}
