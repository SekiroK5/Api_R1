package com.proyecto.usuarios.entity;

import java.time.LocalDate;

import com.proyecto.usuarios.enums.Catalogos.EstadoCivil;
import com.proyecto.usuarios.enums.Catalogos.Nacionalidad;
import com.proyecto.usuarios.enums.Catalogos.Sexo;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity: clase base abstracta con los datos personales comunes.
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public abstract class Persona {

    @Column(name = "nombre", nullable = false, length = 60)
    protected String nombre;

    @Column(name = "segundo_nombre", length = 60)
    protected String segundoNombre;

    @Column(name = "apellido_paterno", nullable = false, length = 60)
    protected String apellidoPaterno;

    @Column(name = "apellido_materno", nullable = false, length = 60)
    protected String apellidoMaterno;

    @Column(name = "fecha_nacimiento", nullable = false)
    protected LocalDate fechaNacimiento;

    @Column(name = "curp", nullable = false, unique = true, length = 18)
    protected String curp;

    @Column(name = "rfc", nullable = false, unique = true, length = 13)
    protected String rfc;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false, length = 15)
    protected Sexo sexo;

    @Enumerated(EnumType.STRING)
    @Column(name = "nacionalidad", nullable = false, length = 15)
    protected Nacionalidad nacionalidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_civil", nullable = false, length = 15)
    protected EstadoCivil estadoCivil;

    // Cada subclase define como se arma el nombre completo (polimorfismo)
    public abstract String getNombreCompleto();

}
