package com.trading.historical_service.exception;

import com.influxdb.exceptions.InfluxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Gestion des erreurs InfluxDB (base indisponible, requête incorrecte)
    @ExceptionHandler(InfluxException.class)
    public ResponseEntity<String> handleInfluxException(InfluxException e) {
        log.error("Erreur InfluxDB : {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Erreur de communication avec la base de données historique : " + e.getMessage());
    }

    // Gestion des paramètres invalides (ex: date mal formatée)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Paramètre invalide : " + e.getName() + " doit être du type " + e.getRequiredType().getSimpleName());
    }

    // Gestion des exceptions génériques (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e) {
        log.error("Erreur inattendue : {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Une erreur interne est survenue");
    }
}