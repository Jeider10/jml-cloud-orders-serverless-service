package com.cloud.jml.config.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.util.Collections;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest =
                new ContentCachingRequestWrapper(request, 1024 * 1024);

        filterChain.doFilter(wrappedRequest, response);

        StringBuilder logMessage = new StringBuilder();

        logMessage.append("\n➡️ Received [")
                .append(wrappedRequest.getMethod())
                .append(" ")
                .append(wrappedRequest.getRequestURI())
                .append(" ")
                .append(wrappedRequest.getProtocol())
                .append("\n");

        // 🔹 Headers
        Collections.list(wrappedRequest.getHeaderNames()).forEach(headerName -> {
            logMessage.append(headerName)
                    .append(": ")
                    .append(wrappedRequest.getHeader(headerName))
                    .append("\n");
        });

        // 🔹 Body
        byte[] content = wrappedRequest.getContentAsByteArray();
        if (content.length > 0) {
            String body = new String(content, wrappedRequest.getCharacterEncoding());

            try {
                Object json = mapper.readValue(body, Object.class);
                String prettyJson = mapper.writeValueAsString(json);
                logMessage.append("\n").append(prettyJson);
            } catch (Exception e) {
                // Si no es JSON válido
                logMessage.append("\n").append(body);
            }
        }

        logMessage.append("\n]");

//        log.info(logMessage.toString());
        log.trace("🔥 NUEVO FILTRO ACTIVO 🔥 \n {} ", logMessage);
    }
}