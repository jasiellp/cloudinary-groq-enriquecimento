package com.enriquecimento.upload.service;

import com.enriquecimento.upload.entity.DadosImagem;
import com.enriquecimento.upload.repository.DadosImagemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AmqpServiceTest {

    @Mock
    private DadosImagemRepository dadosImagemRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private GroqService groqService;

    @InjectMocks
    private AmqpService amqpService;

    @Test
    void sendImageProcessingMessage_deveEnviarMensagemParaFila() {
        // Arrange
        String imageId = "123";
        String imageUrl = "https://example.com/image.jpg";
        String description = "Imagem de teste";

        // Act
        amqpService.sendImageProcessingMessage(imageId, imageUrl, description);

        // Assert
        ArgumentCaptor<Map<String, Object>> messageCaptor = ArgumentCaptor.forClass(Map.class);
        verify(rabbitTemplate).convertAndSend(
            eq("image-processing-exchange"), 
            eq("image-processing"), 
            messageCaptor.capture()
        );

        Map<String, Object> sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.get("imageId")).isEqualTo(imageId);
        assertThat(sentMessage.get("imageUrl")).isEqualTo(imageUrl);
        assertThat(sentMessage.get("description")).isEqualTo(description);
        assertThat(sentMessage.get("timestamp")).isNotNull();
    }

    @Test
    void receiveImageProcessingMessage_deveProcessarMensagemEAtualizarBanco() {
        // Arrange
        String imageId = "123";
        String imageUrl = "https://example.com/image.jpg";
        String description = "Imagem de teste";
        String enrichedDescription = "Imagem processada com IA: Imagem de teste [Enriquecido]";

        Map<String, Object> message = Map.of(
            "imageId", imageId,
            "imageUrl", imageUrl,
            "description", description,
            "timestamp", System.currentTimeMillis()
        );

        DadosImagem dadosImagem = DadosImagem.builder()
            .id(123L)
            .nomeArquivo("test.jpg")
            .descricao(description)
            .build();

        when(dadosImagemRepository.findById(123L)).thenReturn(Optional.of(dadosImagem));
        when(groqService.improveDescription(description)).thenReturn(enrichedDescription);
        when(dadosImagemRepository.save(any(DadosImagem.class))).thenReturn(dadosImagem);

        // Act
        amqpService.receiveImageProcessingMessage(message);

        // Assert
        verify(groqService).improveDescription(description);
        verify(dadosImagemRepository).save(dadosImagem);
        assertThat(dadosImagem.getConteudo()).isEqualTo(enrichedDescription);
    }

    @Test
    void receiveImageProcessingMessage_deveTratarErroQuandoImagemNaoEncontrada() {
        // Arrange
        String imageId = "999";
        Map<String, Object> message = Map.of(
            "imageId", imageId,
            "imageUrl", "https://example.com/image.jpg",
            "description", "Imagem não encontrada",
            "timestamp", System.currentTimeMillis()
        );

        when(dadosImagemRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        amqpService.receiveImageProcessingMessage(message);

        // Assert
        verify(groqService, never()).improveDescription(any());
        verify(dadosImagemRepository, never()).save(any());
    }

    @Test
    void receiveImageProcessingMessage_deveTratarErroQuandoExcecao() {
        // Arrange
        Map<String, Object> message = Map.of(
            "imageId", "123",
            "imageUrl", "https://example.com/image.jpg",
            "description", "Imagem de teste",
            "timestamp", System.currentTimeMillis()
        );

        when(dadosImagemRepository.findById(123L)).thenThrow(new RuntimeException("Database error"));

        // Act
        amqpService.receiveImageProcessingMessage(message);

        // Assert
        verify(groqService, never()).improveDescription(any());
        verify(dadosImagemRepository, never()).save(any());
    }
}
