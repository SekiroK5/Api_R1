package com.proyecto.usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.proyecto.usuarios.dto.request.UsuarioCreateRequest;
import com.proyecto.usuarios.dto.request.UsuarioEliminacionRequest;
import com.proyecto.usuarios.dto.request.UsuarioUpdateRequest;
import com.proyecto.usuarios.dto.response.UsuarioResponse;
import com.proyecto.usuarios.entity.Usuario;
import com.proyecto.usuarios.enums.Catalogos.EstadoCivil;
import com.proyecto.usuarios.enums.Catalogos.Nacionalidad;
import com.proyecto.usuarios.enums.Catalogos.Sexo;
import com.proyecto.usuarios.exception.GlobalExceptionHandler.CorreoDuplicadoException;
import com.proyecto.usuarios.exception.GlobalExceptionHandler.NegocioException;
import com.proyecto.usuarios.exception.GlobalExceptionHandler.UsuarioNoEncontradoException;
import com.proyecto.usuarios.repository.UsuarioRepository;

/**
 * Pruebas unitarias de las reglas de negocio de UsuarioServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    // 1. Registro normaliza correo, CURP y RFC
    @Test
    void crearNormalizaLosDatos() {
        when(usuarioRepository.existsByCorreo("juan.perez@correo.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        UsuarioResponse respuesta = usuarioService.crear(solicitudCreacion(" Juan.Perez@Correo.com "));

        assertThat(respuesta.getCorreo()).isEqualTo("juan.perez@correo.com");
        assertThat(respuesta.getCurp()).isEqualTo("PEGJ900101HGTRRN09");
        assertThat(respuesta.getRfc()).isEqualTo("PEGJ900101AB1");
    }

    // 2. Registro con correo duplicado no llega a guardar
    @Test
    void crearConCorreoDuplicadoLanzaExcepcion() {
        when(usuarioRepository.existsByCorreo("juan.perez@correo.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.crear(solicitudCreacion("juan.perez@correo.com")))
                .isInstanceOf(CorreoDuplicadoException.class);
        verify(usuarioRepository, never()).save(any());
    }

    // 3. Segundo nombre omitido: no se modifica
    @Test
    void actualizarSinSegundoNombreLoConserva() {
        Usuario usuario = usuarioExistente();
        when(usuarioRepository.findByNombreAndCorreo("Juan", "juan.perez@correo.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        UsuarioUpdateRequest request = llave();
        request.setTelefono("4779876543");
        UsuarioResponse respuesta = usuarioService.actualizar(request);

        assertThat(respuesta.getSegundoNombre()).isEqualTo("Carlos");
        assertThat(respuesta.getTelefono()).isEqualTo("4779876543");
    }

    // 4. Segundo nombre enviado con null: se borra
    @Test
    void actualizarConSegundoNombreNullLoBorra() {
        Usuario usuario = usuarioExistente();
        when(usuarioRepository.findByNombreAndCorreo("Juan", "juan.perez@correo.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        UsuarioUpdateRequest request = llave();
        request.setSegundoNombre(null);
        UsuarioResponse respuesta = usuarioService.actualizar(request);

        assertThat(respuesta.getSegundoNombre()).isNull();
    }

    // 5. Nuevo correo que pertenece a otro usuario
    @Test
    void actualizarConCorreoDeOtroUsuarioLanzaExcepcion() {
        Usuario usuario = usuarioExistente();
        when(usuarioRepository.findByNombreAndCorreo("Juan", "juan.perez@correo.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByCorreoAndIdNot("maria.lopez@correo.com", 1L)).thenReturn(true);

        UsuarioUpdateRequest request = llave();
        request.setNuevoCorreo("maria.lopez@correo.com");

        assertThatThrownBy(() -> usuarioService.actualizar(request)).isInstanceOf(CorreoDuplicadoException.class);
        verify(usuarioRepository, never()).save(any());
    }

    // 6. Eliminar sin coincidencia: excepcion de negocio con 404 (polimorfismo)
    @Test
    void eliminarSinCoincidenciaLanzaExcepcion() {
        when(usuarioRepository.findByNombreAndCorreo("Juan", "juan.perez@correo.com")).thenReturn(Optional.empty());

        UsuarioEliminacionRequest request = UsuarioEliminacionRequest.builder()
                .nombre(" Juan ").correo("JUAN.PEREZ@correo.com").build();

        assertThatThrownBy(() -> usuarioService.eliminar(request))
                .isInstanceOf(UsuarioNoEncontradoException.class)
                .satisfies(ex -> {
                    NegocioException negocio = (NegocioException) ex;
                    assertThat(negocio.getCodigo()).isEqualTo("USUARIO_NO_ENCONTRADO");
                    assertThat(negocio.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                });
        verify(usuarioRepository, never()).delete(any(Usuario.class));
    }

    private static UsuarioCreateRequest solicitudCreacion(String correo) {
        return UsuarioCreateRequest.builder()
                .nombre("Juan").apellidoPaterno("Perez").apellidoMaterno("Gomez")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .curp("pegj900101hgtrrn09").rfc("pegj900101ab1")
                .sexo(Sexo.HOMBRE).nacionalidad(Nacionalidad.MEXICANA).estadoCivil(EstadoCivil.SOLTERO)
                .correo(correo).telefono("4771234567")
                .build();
    }

    private static UsuarioUpdateRequest llave() {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setNombre("Juan");
        request.setCorreo("juan.perez@correo.com");
        return request;
    }

    private static Usuario usuarioExistente() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan");
        usuario.setSegundoNombre("Carlos");
        usuario.setApellidoPaterno("Perez");
        usuario.setApellidoMaterno("Gomez");
        usuario.setCorreo("juan.perez@correo.com");
        usuario.setTelefono("4771234567");
        usuario.setActivo(true);
        return usuario;
    }

}
