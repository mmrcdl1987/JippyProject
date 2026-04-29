package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Generic API response envelope used by all REST endpoints.
 *
 * <p>Why a common envelope: wrapping every response in a consistent structure
 * ({@code success}, {@code message}, {@code data}, {@code timestamp}) makes
 * client-side error handling uniform — the frontend always knows where to look
 * for the status flag and error message regardless of endpoint.</p>
 *
 * <p>The {@code @JsonInclude(NON_NULL)} annotation means null fields
 * (e.g. {@code errors} on success responses) are omitted from the JSON,
 * keeping the payload clean.</p>
 *
 * @param <T> the type of the {@code data} payload
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FmApiResponse<T> {

    /**
     * Whether the operation succeeded. Always present in the response.
     * {@code true} for 2xx responses, {@code false} for error responses.
     */
    private boolean success;

    /** Human-readable summary of the outcome, e.g. "Merchant registered successfully". */
    private String message;

    /** The actual response payload. Null on error responses. */
    private T data;

    /**
     * List of validation or business error messages.
     * Present only on error responses where multiple messages apply
     * (e.g. constraint violation lists from bulk upload).
     */
    private List<String> errors;

    /**
     * The server timestamp when this response was generated.
     * Serialised as ISO-8601 string (e.g. "2024-03-15T14:30:00").
     */
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * Static factory for a successful response with both a message and a data payload.
     *
     * <p>Why a static factory instead of calling the constructor: the factory
     * name makes call sites self-documenting — {@code ApiResponse.success(...)}
     * clearly signals intent compared to {@code new ApiResponse(true, ..., null, now())}.</p>
     *
     * @param message human-readable success message
     * @param data    the response payload
     * @param <T>     the payload type
     * @return a success-flagged {@link FmApiResponse} with timestamp set to now
     */
    public static <T> FmApiResponse<T> success(String message, T data) {
        FmApiResponse<T> resp = new FmApiResponse<>();
        resp.setSuccess(true);
        resp.setMessage(message);
        resp.setData(data);
        resp.setTimestamp(LocalDateTime.now());
        return resp;
    }

    /**
     * Static factory for an error response with both a message and an error list.
     *
     * <p>Why include an errors list: some operations (e.g. bulk validation) produce
     * multiple error messages at once. A list lets the client display all of them
     * rather than showing only the first.</p>
     *
     * @param message the top-level error summary
     * @param errors  the list of specific error messages
     * @param <T>     unused (data will be null)
     * @return a failure-flagged {@link FmApiResponse} with no data
     */
    public static <T> FmApiResponse<T> error(String message, List<String> errors) {
        FmApiResponse<T> resp = new FmApiResponse<>();
        resp.setSuccess(false);
        resp.setMessage(message);
        resp.setErrors(errors);
        resp.setTimestamp(LocalDateTime.now());
        return resp;
    }

    /**
     * Static factory for a simple error response with just a message.
     *
     * <p>Why a convenience overload without the errors list: most single-operation
     * endpoints only have one error to report. This overload saves callers from
     * wrapping a single string in {@code List.of(...)} every time.</p>
     *
     * @param message the error message
     * @param <T>     unused (data will be null)
     * @return a failure-flagged {@link FmApiResponse} with no data or error list
     */
    public static <T> FmApiResponse<T> error(String message) {
        FmApiResponse<T> resp = new FmApiResponse<>();
        resp.setSuccess(false);
        resp.setMessage(message);
        resp.setTimestamp(LocalDateTime.now());
        return resp;
    }
}
