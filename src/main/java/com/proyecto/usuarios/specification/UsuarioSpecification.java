package com.proyecto.usuarios.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.proyecto.usuarios.dto.request.UsuarioBusquedaRequest;
import com.proyecto.usuarios.entity.Usuario;

/**
 * Specification: construye dinámicamente las condiciones de búsqueda.
 */
public final class UsuarioSpecification {

    private static final char ESCAPE = '\\';

    private UsuarioSpecification() {
    }

    // 1. Combina con AND unicamente los criterios que el cliente envio
    public static Specification<Usuario> conFiltros(UsuarioBusquedaRequest request) {
        List<Specification<Usuario>> condiciones = new ArrayList<>();

        if (tieneTexto(request.getNombre())) {
            condiciones.add(nombreContiene(request.getNombre()));
        }
        if (tieneTexto(request.getCorreo())) {
            condiciones.add(correoIgual(request.getCorreo()));
        }
        if (tieneTexto(request.getTelefono())) {
            condiciones.add(telefonoIgual(request.getTelefono()));
        }
        if (request.getActivo() != null) {
            condiciones.add(activoIgual(request.getActivo()));
        }

        return Specification.allOf(condiciones);
    }

    // 2. Coincidencia parcial sin distinguir mayusculas: lower(nombre) LIKE %texto%
    private static Specification<Usuario> nombreContiene(String nombre) {
        String patron = "%" + escaparLike(nombre.trim().toLowerCase(Locale.ROOT)) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("nombre")), patron, ESCAPE);
    }

    // 3. Coincidencia exacta (el correo se guarda en minusculas)
    private static Specification<Usuario> correoIgual(String correo) {
        String valor = correo.trim().toLowerCase(Locale.ROOT);
        return (root, query, cb) -> cb.equal(root.get("correo"), valor);
    }

    // 4. Coincidencia exacta
    private static Specification<Usuario> telefonoIgual(String telefono) {
        String valor = telefono.trim();
        return (root, query, cb) -> cb.equal(root.get("telefono"), valor);
    }

    // 5. Coincidencia exacta
    private static Specification<Usuario> activoIgual(Boolean activo) {
        return (root, query, cb) -> cb.equal(root.get("activo"), activo);
    }

    private static boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    // Evita que % y _ enviados por el cliente funcionen como comodines
    private static String escaparLike(String valor) {
        return valor.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

}
