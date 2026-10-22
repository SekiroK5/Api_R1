package com.proyecto.usuarios;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

/**
 * Pruebas de extremo a extremo de los endpoints contra un PostgreSQL real (embebido)
 * creado con db/script_bd.sql. Cada solicitud y respuesta se guarda como evidencia
 * en target/evidencias-pruebas.md.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UsuarioApiIntegrationTest {

    private static final String URL = "/api/v1/usuarios";
    private static final EmbeddedPostgres POSTGRES = iniciarPostgres();
    private static final StringBuilder EVIDENCIAS = new StringBuilder();
    private static int numeroCaso = 0;

    private static final String JUAN = """
            {
              "nombre": "Juan",
              "segundoNombre": "Carlos",
              "apellidoPaterno": "Perez",
              "apellidoMaterno": "Gomez",
              "fechaNacimiento": "1990-01-01",
              "curp": "PEGJ900101HGTRRN09",
              "rfc": "PEGJ900101AB1",
              "sexo": "HOMBRE",
              "nacionalidad": "MEXICANA",
              "estadoCivil": "SOLTERO",
              "correo": "juan.perez@correo.com",
              "telefono": "4771234567"
            }""";

    private static final String MARIA = """
            {
              "nombre": "Maria",
              "apellidoPaterno": "Lopez",
              "apellidoMaterno": "Diaz",
              "fechaNacimiento": "1995-06-15",
              "curp": "LODM950615MGTPZR01",
              "rfc": "LODM950615CD2",
              "sexo": "MUJER",
              "nacionalidad": "MEXICANA",
              "estadoCivil": "CASADO",
              "correo": "maria.lopez@correo.com",
              "telefono": "4777654321"
            }""";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configurarBaseDeDatos(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl("postgres", "postgres"));
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "");
    }

    // ===================== 1. Crear usuario =====================

    @Test
    @Order(1)
    void crearUsuarioDevuelve201() throws Exception {
        ejecutar("Crear usuario (exitoso)", post(URL), JUAN)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Usuario registrado correctamente."))
                .andExpect(jsonPath("$.datos.id").isNumber())
                .andExpect(jsonPath("$.datos.correo").value("juan.perez@correo.com"))
                .andExpect(jsonPath("$.datos.activo").value(true))
                .andExpect(jsonPath("$.datos.fechaCreacion").isNotEmpty());
    }

    @Test
    @Order(2)
    void crearSegundoUsuarioDevuelve201() throws Exception {
        ejecutar("Crear segundo usuario (sin segundo nombre)", post(URL), MARIA)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.datos.segundoNombre").value(nullValue()));
    }

    @Test
    @Order(3)
    void crearConCorreoDuplicadoDevuelve409() throws Exception {
        String body = MARIA.replace("maria.lopez@correo.com", "Juan.Perez@Correo.com")
                .replace("LODM950615MGTPZR01", "LODM950615MGTPZR02").replace("LODM950615CD2", "LODM950615CD3");
        ejecutar("Crear con correo ya registrado (aunque cambien las mayusculas)", post(URL), body)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CORREO_DUPLICADO"))
                .andExpect(jsonPath("$.mensaje").value("El correo ya se encuentra registrado. Intente con otro correo."));
    }

    @Test
    @Order(4)
    void crearConCurpDuplicadaDevuelve409() throws Exception {
        String body = MARIA.replace("maria.lopez@correo.com", "otra@correo.com")
                .replace("LODM950615MGTPZR01", "PEGJ900101HGTRRN09").replace("LODM950615CD2", "LODM950615CD3");
        ejecutar("Crear con CURP ya registrada (restriccion UNIQUE de la BD)", post(URL), body)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("INTEGRIDAD_DATOS"))
                .andExpect(jsonPath("$.mensaje").value("La CURP ya se encuentra registrada."));
    }

    @Test
    @Order(5)
    void crearConDatosInvalidosDevuelve400() throws Exception {
        String body = """
                {
                  "nombre": "   ",
                  "apellidoPaterno": "Perez",
                  "apellidoMaterno": "Gomez",
                  "fechaNacimiento": "2999-01-01",
                  "curp": "",
                  "rfc": "XXXX",
                  "sexo": "HOMBRE",
                  "nacionalidad": "MEXICANA",
                  "estadoCivil": "SOLTERO",
                  "correo": "correo-sin-arroba",
                  "telefono": "12345"
                }""";
        ejecutar("Crear con datos no validos (Bean Validation)", post(URL), body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"))
                .andExpect(jsonPath("$.errores.nombre").value("El nombre es obligatorio y no puede contener solo espacios."))
                .andExpect(jsonPath("$.errores.correo").value("El correo electrónico no tiene un formato válido."))
                .andExpect(jsonPath("$.errores.telefono").value("El teléfono debe contener exactamente 10 dígitos."))
                .andExpect(jsonPath("$.errores.fechaNacimiento").exists())
                .andExpect(jsonPath("$.errores.curp").exists())
                .andExpect(jsonPath("$.errores.rfc").exists());
    }

    @Test
    @Order(6)
    void crearIgnoraCamposControladosPorElServidor() throws Exception {
        String body = """
                {
                  "id": 999,
                  "fechaCreacion": "2000-01-01T00:00:00",
                  "activo": false,
                  "nombre": "Pedro",
                  "apellidoPaterno": "Ramirez",
                  "apellidoMaterno": "Soto",
                  "fechaNacimiento": "1988-03-10",
                  "curp": "RASP880310HGTMTD05",
                  "rfc": "RASP880310EF4",
                  "sexo": "HOMBRE",
                  "nacionalidad": "EXTRANJERA",
                  "estadoCivil": "DIVORCIADO",
                  "correo": "pedro.ramirez@correo.com",
                  "telefono": "4771112233"
                }""";
        ejecutar("Crear enviando id, fechaCreacion y activo (el servidor los ignora)", post(URL), body)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.datos.id").value(not(999)))
                .andExpect(jsonPath("$.datos.activo").value(true))
                .andExpect(jsonPath("$.datos.fechaCreacion").value(not("2000-01-01T00:00:00")));
    }

    @Test
    @Order(7)
    void crearConValorFueraDeCatalogoDevuelve400() throws Exception {
        String body = JUAN.replace("\"HOMBRE\"", "\"OTRO\"");
        ejecutar("Crear con un valor fuera del catalogo (enum Sexo)", post(URL), body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"))
                .andExpect(jsonPath("$.errores.sexo").exists());
    }

    // ===================== 2. Consultar usuarios =====================

    @Test
    @Order(10)
    void listarUsuariosDevuelvePagina() throws Exception {
        ejecutar("Listar usuarios (paginado)", get(URL), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.content[0].nombre").value("Pedro"));
    }

    // ===================== 7. Busqueda dinamica =====================

    @Test
    @Order(20)
    void buscarPorNombreParcialYActivo() throws Exception {
        String body = """
                { "nombre": "jua", "correo": null, "telefono": null, "activo": true }""";
        ejecutar("Busqueda dinamica: nombre parcial + activo", post(URL + "/busqueda"), body)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].correo").value("juan.perez@correo.com"));
    }

    @Test
    @Order(21)
    void buscarSoloPorCorreo() throws Exception {
        ejecutar("Busqueda dinamica: solo correo", post(URL + "/busqueda"), """
                { "correo": "maria.lopez@correo.com" }""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nombre").value("Maria"));
    }

    @Test
    @Order(22)
    void buscarSinCriteriosDevuelve400() throws Exception {
        ejecutar("Busqueda dinamica sin criterios", post(URL + "/busqueda"), "{ }")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"))
                .andExpect(jsonPath("$.errores", hasKey("algunCriterio")));
    }

    @Test
    @Order(23)
    void buscarConPaginacionNulaDevuelve400() throws Exception {
        ejecutar("Busqueda dinamica con pagina y tamanio nulos (@NotNull)", post(URL + "/busqueda"), """
                { "nombre": "Juan", "pagina": null, "tamanio": null }""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.pagina").value("La pagina no puede ser nula."))
                .andExpect(jsonPath("$.errores.tamanio").value("El tamaño de pagina no puede ser nulo."));
    }

    // ===================== 3. Consulta por llave compuesta =====================

    @Test
    @Order(30)
    void consultarPorLlaveCompuesta() throws Exception {
        ejecutar("Consultar por nombre + correo (exitoso)", post(URL + "/consulta"), """
                { "nombre": "Juan", "correo": "juan.perez@correo.com" }""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.telefono").value("4771234567"));
    }

    @Test
    @Order(31)
    void consultarSinCoincidenciaDevuelve404() throws Exception {
        ejecutar("Consultar cuando nombre y correo no coinciden", post(URL + "/consulta"), """
                { "nombre": "Maria", "correo": "juan.perez@correo.com" }""")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("USUARIO_NO_ENCONTRADO"))
                .andExpect(jsonPath("$.mensaje").value("No existe un usuario con el nombre y correo electrónico proporcionados."));
    }

    @Test
    @Order(32)
    void consultarConCamposVaciosDevuelve400() throws Exception {
        ejecutar("Consultar con campos vacios", post(URL + "/consulta"), """
                { "nombre": " ", "correo": "" }""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").value("El nombre es obligatorio y no puede contener solo espacios."))
                .andExpect(jsonPath("$.errores.correo").value("El correo electrónico es obligatorio."));
    }

    // ===================== 4. Actualizar usuario =====================

    @Test
    @Order(40)
    void actualizarSoloLosCamposEnviados() throws Exception {
        ejecutar("Actualizar telefono y activo (segundoNombre omitido no cambia)", patch(URL + "/actualizacion"), """
                {
                  "nombre": "Juan",
                  "correo": "juan.perez@correo.com",
                  "telefono": "4779876543",
                  "activo": false
                }""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Usuario actualizado correctamente."))
                .andExpect(jsonPath("$.datos.telefono").value("4779876543"))
                .andExpect(jsonPath("$.datos.activo").value(false))
                .andExpect(jsonPath("$.datos.segundoNombre").value("Carlos"))
                .andExpect(jsonPath("$.datos.apellidoPaterno").value("Perez"));
    }

    @Test
    @Order(41)
    void actualizarSegundoNombreConNullLoBorra() throws Exception {
        ejecutar("Actualizar segundoNombre con null explicito (se borra)", patch(URL + "/actualizacion"), """
                { "nombre": "Juan", "correo": "juan.perez@correo.com", "segundoNombre": null }""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.segundoNombre").value(nullValue()))
                .andExpect(jsonPath("$.datos.telefono").value("4779876543"));
    }

    @Test
    @Order(42)
    void actualizarConCorreoDeOtroUsuarioDevuelve409() throws Exception {
        ejecutar("Actualizar con un correo que ya usa otro usuario", patch(URL + "/actualizacion"), """
                { "nombre": "Juan", "correo": "juan.perez@correo.com", "nuevoCorreo": "maria.lopez@correo.com" }""")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CORREO_DUPLICADO"));
    }

    @Test
    @Order(43)
    void actualizarLaLlave() throws Exception {
        ejecutar("Actualizar nombre y correo (cambio de llave)", patch(URL + "/actualizacion"), """
                {
                  "nombre": "Juan",
                  "correo": "juan.perez@correo.com",
                  "nuevoNombre": "Juan Pablo",
                  "nuevoCorreo": "JuanPablo.Perez@correo.com"
                }""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.nombre").value("Juan Pablo"))
                .andExpect(jsonPath("$.datos.correo").value("juanpablo.perez@correo.com"));
    }

    @Test
    @Order(44)
    void actualizarSinCamposDevuelve400() throws Exception {
        ejecutar("Actualizar sin campos a modificar", patch(URL + "/actualizacion"), """
                { "nombre": "Juan Pablo", "correo": "juanpablo.perez@correo.com" }""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.algunCampoParaActualizar").value("Debe enviar al menos un campo para actualizar."));
    }

    @Test
    @Order(45)
    void actualizarUsuarioInexistenteDevuelve404() throws Exception {
        ejecutar("Actualizar usuario inexistente", patch(URL + "/actualizacion"), """
                { "nombre": "Juan", "correo": "juan.perez@correo.com", "telefono": "4770000000" }""")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("USUARIO_NO_ENCONTRADO"));
    }

    // ===================== 5. Eliminar usuario =====================

    @Test
    @Order(50)
    void eliminarUsuario() throws Exception {
        ejecutar("Eliminar por nombre + correo (exitoso)", delete(URL + "/eliminacion"), """
                { "nombre": "Maria", "correo": "maria.lopez@correo.com" }""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Usuario eliminado correctamente."))
                .andExpect(jsonPath("$", not(hasKey("datos"))));
    }

    @Test
    @Order(51)
    void eliminarUsuarioInexistenteDevuelve404() throws Exception {
        ejecutar("Eliminar un usuario que ya no existe", delete(URL + "/eliminacion"), """
                { "nombre": "Maria", "correo": "maria.lopez@correo.com" }""")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("USUARIO_NO_ENCONTRADO"));
    }

    // ===================== 6. Errores generales =====================

    @Test
    @Order(60)
    void jsonMalFormadoDevuelve400() throws Exception {
        ejecutar("JSON mal formado", post(URL), "{ \"nombre\": ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));
    }

    @Test
    @Order(61)
    void rutaInexistenteDevuelve404() throws Exception {
        ejecutar("Ruta inexistente", get(URL + "/1"), null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));
    }

    @Test
    @Order(62)
    void metodoNoPermitidoDevuelve405() throws Exception {
        ejecutar("Metodo HTTP no permitido", put(URL), JUAN)
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));
    }

    @Test
    @Order(63)
    void documentacionOpenApiDisponible() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/usuarios/consulta']").exists());
    }

    // ===================== Utilidades =====================

    private ResultActions ejecutar(String titulo, MockHttpServletRequestBuilder solicitud, String body) throws Exception {
        if (body != null) {
            solicitud.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        ResultActions resultado = mockMvc.perform(solicitud);
        registrarEvidencia(titulo, resultado.andReturn(), body);
        return resultado;
    }

    private void registrarEvidencia(String titulo, MvcResult resultado, String body) throws IOException {
        String respuesta = resultado.getResponse().getContentAsString(StandardCharsets.UTF_8);
        if (!respuesta.isBlank()) {
            respuesta = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(respuesta));
        }
        EVIDENCIAS.append("### ").append(++numeroCaso).append(". ").append(titulo).append("\n\n")
                .append("`").append(resultado.getRequest().getMethod()).append(" ")
                .append(resultado.getRequest().getRequestURI()).append("`, resultado: **HTTP ")
                .append(resultado.getResponse().getStatus()).append("**\n\n");
        if (body != null) {
            EVIDENCIAS.append("Solicitud:\n```json\n").append(body).append("\n```\n\n");
        }
        EVIDENCIAS.append("Respuesta:\n```json\n").append(respuesta).append("\n```\n\n");
    }

    @AfterAll
    static void guardarEvidencias() throws IOException {
        Path archivo = Path.of("target", "evidencias-pruebas.md");
        Files.createDirectories(archivo.getParent());
        Files.writeString(archivo, EVIDENCIAS.toString(), StandardCharsets.UTF_8);
    }

    private static EmbeddedPostgres iniciarPostgres() {
        try {
            EmbeddedPostgres postgres = EmbeddedPostgres.builder().start();
            String script = Files.readString(Path.of("db", "script_bd.sql"), StandardCharsets.UTF_8);
            try (Connection conexion = postgres.getPostgresDatabase().getConnection();
                 Statement sentencia = conexion.createStatement()) {
                sentencia.execute(script);
            }
            return postgres;
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo iniciar PostgreSQL embebido", ex);
        }
    }

}
