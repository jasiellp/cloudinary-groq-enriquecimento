package com.enriquecimento.upload.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Log
@Service
@RequiredArgsConstructor
public class GroqService {

    private final RestTemplate restTemplate;

    @Value("${groq.api.key}")
    private String groqApiKey;
    
    @Value("${groq.api.url}")
    private String groqApiUrl;
    public String improveDescription(String description) {
        try {
            
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(this.groqApiKey);
            
            Map<String, Object> requestBody = Map.of(
                "model", "openai/gpt-oss-20b",
                "messages", List.of(
                    Map.of(
                        "role", "user",
                        "content", "Melhore esse texto '" + description + "'"
                    )
                )
            );
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                this.groqApiUrl, 
                HttpMethod.POST, 
                request, 
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                    String content = (String) message.get("content");
                    
                    log.info("Resposta do Groq: " + content);
                    return content;
                }
            }
            
            log.warning("Resposta inesperada da API Groq: " + response.getBody());
            return "Erro ao processar com IA: " + description;
            
        } catch (Exception e) {
            log.severe("Erro ao chamar API Groq: " + e.getMessage());
            return "Erro ao processar com IA: " + description;
        }
    }
}
