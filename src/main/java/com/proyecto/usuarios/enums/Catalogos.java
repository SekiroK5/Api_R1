package com.proyecto.usuarios.enums;

/**
 * Catálogos de valores fijos del proyecto.
 */
public final class Catalogos {

    private Catalogos() {
    }

    /**
     * Sexo de la persona. La clave corresponde a la letra que se usa en la CURP.
     */
    public enum Sexo {
        HOMBRE("H", "Hombre"),
        MUJER("M", "Mujer"),
        NO_BINARIO("X", "No binario");

        private final String clave;
        private final String descripcion;

        Sexo(String clave, String descripcion) {
            this.clave = clave;
            this.descripcion = descripcion;
        }

        public String getClave() {
            return clave;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    /**
     * Estado civil de la persona.
     */
    public enum EstadoCivil {
        SOLTERO("Soltero(a)"),
        CASADO("Casado(a)"),
        DIVORCIADO("Divorciado(a)"),
        VIUDO("Viudo(a)"),
        UNION_LIBRE("Unión libre");

        private final String descripcion;

        EstadoCivil(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    /**
     * Nacionalidad de la persona.
     */
    public enum Nacionalidad {
        MEXICANA("Mexicana"),
        EXTRANJERA("Extranjera");

        private final String descripcion;

        Nacionalidad(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    /**
     * Códigos de error que devuelve la API en ApiErrorResponse.
     */
    public enum CodigoError {
        DATOS_INVALIDOS("Verifique la información capturada e intente nuevamente."),
        SOLICITUD_INVALIDA("El cuerpo de la solicitud no tiene un formato válido."),
        USUARIO_NO_ENCONTRADO("No existe un usuario con el nombre y correo electrónico proporcionados."),
        CORREO_DUPLICADO("El correo ya se encuentra registrado. Intente con otro correo."),
        INTEGRIDAD_DATOS("La operación no se pudo completar porque viola una regla de integridad de los datos."),
        ERROR_INTERNO("Ocurrió un error inesperado. Intente más tarde.");

        private final String mensaje;

        CodigoError(String mensaje) {
            this.mensaje = mensaje;
        }

        public String getMensaje() {
            return mensaje;
        }
    }

}
