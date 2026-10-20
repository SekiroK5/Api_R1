package com.proyecto.usuarios.entity;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity: representa la tabla usuarios.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario extends Persona {

    // Identificador generado por la base de datos (GENERATED ALWAYS AS IDENTITY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "correo", nullable = false, unique = true, length = 120)
    private String correo;

    @Column(name = "telefono", nullable = false, length = 10)
    private String telefono;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    // Controlado por el servidor: no se inserta desde el cliente ni se actualiza despues
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Override
    public String getNombreCompleto() {
        return Stream.of(nombre, segundoNombre, apellidoPaterno, apellidoMaterno)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
    }

    // Valores que asigna el servidor antes de insertar el registro
    @PrePersist
    protected void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.activo == null) {
            this.activo = Boolean.TRUE;
        }
    }

}
