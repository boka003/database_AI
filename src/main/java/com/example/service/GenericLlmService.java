package com.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.StringUtils;

@Service("genericLlmService")
public class GenericLlmService {

    private final RestTemplate rest = new RestTemplate();

    @Value("${llm.generic.endpoint:}")
    private String endpoint;

    @Value("${llm.generic.apiKey:}")
    private String apiKey;

    @Value("${llm.generic.apiKeyHeader:Authorization}")
    private String apiKeyHeader;

    @Value("${llm.generic.model:}")
    private String model;

    @Value("${llm.generic.requestTemplate:}")
    private String requestTemplate;

    public String generate(String prompt) {
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalStateException("Generic LLM endpoint not configured");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(apiKey)) {
            headers.set(apiKeyHeader, apiKey.startsWith("Bearer ") ? apiKey : apiKey);
        }

        String body;
        if (StringUtils.hasText(requestTemplate)) {
            body = requestTemplate
                .replace("${prompt}", escapeJson(prompt))
                .replace("${model}", model == null ? "" : model);
        } else {
            body = "{\"model\":\"" + (model == null ? "" : model) + "\",\"input\":\"" + escapeJson(prompt) + "\"}";
        }

        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> resp = rest.postForEntity(endpoint, httpEntity, String.class);

        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("LLM returned: " + resp.getStatusCode() + " - " + resp.getBody());
        }

        return resp.getBody();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
