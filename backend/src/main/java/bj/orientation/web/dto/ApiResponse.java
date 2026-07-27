package bj.orientation.web.dto;

/** Enveloppe de succès standard ASIN : le payload est toujours sous la clé {@code data}. */
public record ApiResponse<T>(T data) {
}
