package com.proyecto.usuarios.exception;

import com.proyecto.usuarios.dto.response.ApiErrorResponse;
import com.proyecto.usuarios.enums.Catalogos.CodigoError;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * En caso de que alguna validacion no se cumpla para algun DTO se tira MethodArgumentNotValidException
     * este metodo la cacha y regresa los mensajes de error para el cliente, con los message de cada campo.
     * @param ex
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> manejarValidacion(MethodArgumentNotValidException ex) {
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
        Map<String, String> listError = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            listError.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        apiErrorResponse.setCodigo(CodigoError.DATOS_INVALIDOS.name());
        apiErrorResponse.setMensaje(CodigoError.DATOS_INVALIDOS.getMensaje());
        apiErrorResponse.setErrores(listError);
        return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
    }

}
