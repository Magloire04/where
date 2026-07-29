package bj.orientation.web;

import bj.orientation.ocr.OcrIndisponibleException;
import bj.orientation.web.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/** Traduit les erreurs en enveloppe d'erreur standard ASIN. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiErrorResponse onValidation(MethodArgumentNotValidException exception) {
    return ApiErrorResponse.of(
        "VALIDATION_ERROR",
        "Requête invalide : série requise et notes valides (0 à 20) obligatoires.",
        HttpStatus.BAD_REQUEST.value());
  }

  @ExceptionHandler(OcrIndisponibleException.class)
  @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
  public ApiErrorResponse onOcrIndisponible(OcrIndisponibleException exception) {
    return ApiErrorResponse.of(
        "OCR_INDISPONIBLE", exception.getMessage(), HttpStatus.SERVICE_UNAVAILABLE.value());
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
  public ApiErrorResponse onFichierTropGros(MaxUploadSizeExceededException exception) {
    return ApiErrorResponse.of(
        "FICHIER_TROP_GROS",
        "Fichier trop volumineux (max 15 Mo). Réduis la taille de la photo ou saisis tes notes à la main.",
        HttpStatus.PAYLOAD_TOO_LARGE.value());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiErrorResponse onArgumentInvalide(IllegalArgumentException exception) {
    return ApiErrorResponse.of(
        "SERIE_INVALIDE", "Série inconnue.", HttpStatus.BAD_REQUEST.value());
  }
}
