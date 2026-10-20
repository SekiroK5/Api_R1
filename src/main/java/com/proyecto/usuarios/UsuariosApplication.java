package com.proyecto.usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

/**
 * Clase principal de la aplicación.
 */
@SpringBootApplication
// Serializa las paginas con una estructura JSON estable: { content, page }
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@OpenAPIDefinition(info = @Info(
        title = "API REST de administracion de usuarios",
        version = "1.0.0",
        description = "Spring Boot 3, Spring Data JPA y PostgreSQL. Sin @PathVariable ni @RequestParam: "
                + "los datos se reciben en Request DTOs con @RequestBody y @Valid."))
public class UsuariosApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsuariosApplication.class, args);
    }

}
