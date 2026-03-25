package com.github.pramodkuth.openpolicy.client;

import jakarta.annotation.Nullable;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Implementation of {@link ClientHttpResponse} to wrap the fallback response in case the main OPA client call fails.
 * @author pramodkuth
 */
public class OpaFallbackClientHttpResponse implements ClientHttpResponse {

    /**
     * The response body as bytes.
     */
    private final byte[] body;

    /**
     * The HTTP status code of the response.
     */
    private final HttpStatusCode status;

    /**
     * The HTTP headers of the response.
     */
    private final HttpHeaders headers;

    /**
     * Creates a new instance of {@link OpaFallbackClientHttpResponse} with the given body and status.
     *
     * @param body  the response body as bytes
     * @param status the HTTP status code
     */
    public OpaFallbackClientHttpResponse(@Nullable byte[] body, HttpStatusCode status) {
        this.body = body != null ? body : new byte[0];
        this.status = status;
        this.headers = new HttpHeaders();
        this.headers.setContentType(MediaType.APPLICATION_JSON);
    }

    /**
     * Returns the HTTP status code of the response.
     *
     * @return the HTTP status code
     */
    @Override
    public HttpStatusCode getStatusCode() {
        return this.status;
    }

    /**
     * Returns the status text corresponding to the HTTP status code.
     *
     * @return the status text
     */
    @Override
    public String getStatusText() {
        return this.status.toString();
    }

    /**
     * Returns an {@link InputStream} containing the response body as bytes.
     *
     * @return an InputStream containing the response body
     */
    @Override
    public InputStream getBody() {
        return new ByteArrayInputStream(this.body);
    }

    /**
     * Returns the HTTP headers of the response.
     *
     * @return the HTTP headers
     */
    @Override
    public HttpHeaders getHeaders() {
        return this.headers;
    }

    /**
     * Closes any resources associated with this response.
     */
    @Override
    public void close() {
        // No-op, no resources to release
    }
}
