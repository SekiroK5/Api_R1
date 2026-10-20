package com.proyecto.usuarios.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * DTO de salida con el formato centralizado de los errores.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    private String codigo;
    private String mensaje;
    private Map<String, String> errores;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Map<String, String> getErrores() {
        return errores;
    }

    public void setErrores(Map<String, String> errores) {
        this.errores = errores;
    }

}
