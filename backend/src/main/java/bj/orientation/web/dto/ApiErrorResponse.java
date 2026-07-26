package bj.orientation.web.dto;

/** Enveloppe d'erreur standard ASIN : {@code { "error": { code, message, status } }}. */
public record ApiErrorResponse(Body error) {

    public record Body(String code, String message, int status) {
    }

    public static ApiErrorResponse of(String code, String message, int status) {
        return new ApiErrorResponse(new Body(code, message, status));
    }
}
