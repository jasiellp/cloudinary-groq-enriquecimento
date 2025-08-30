package com.enriquecimento.upload.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroqServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GroqService groqService;

    @Test
    void improveDescription_deveRetornarDescricaoMelhorada() {
        // Arrange
        String originalDescription = "Imagem de um senhor segurando um bebê";
        String expectedResponse = "Fotografia emocionante de um avô carinhoso segurando um bebê com ternura";
        
        Map<String, Object> mockResponse = Map.of(
            "choices", List.of(
                Map.of(
                    "message", Map.of(
                        "content", expectedResponse
                    )
                )
            )
        );
        
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(Map.class)
        )).thenReturn(responseEntity);

        // Act
        String result = groqService.improveDescription(originalDescription);

        // Assert
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void improveDescription_deveRetornarErroQuandoRespostaInesperada() {
        // Arrange
        String originalDescription = "Imagem de um senhor segurando um bebê";
        Map<String, Object> mockResponse = Map.of("error", "API error");
        
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(Map.class)
        )).thenReturn(responseEntity);

        // Act
        String result = groqService.improveDescription(originalDescription);

        // Assert
        assertThat(result).contains("Erro ao processar com IA");
    }

    @Test
    void improveDescription_deveRetornarErroQuandoExcecao() {
        // Arrange
        String originalDescription = "Imagem de um senhor segurando um bebê";
        
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(Map.class)
        )).thenThrow(new RuntimeException("Network error"));

        // Act
        String result = groqService.improveDescription(originalDescription);

        // Assert
        assertThat(result).contains("Erro ao processar com IA");
    }
}
