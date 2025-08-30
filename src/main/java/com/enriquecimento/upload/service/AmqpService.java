package com.enriquecimento.upload.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.enriquecimento.upload.repository.DadosImagemRepository;

import java.util.Optional;
import com.enriquecimento.upload.entity.DadosImagem;
import java.util.Map;

@Log
@Service
@RequiredArgsConstructor
public class AmqpService {

    private final DadosImagemRepository dadosImagemRepository;
    private final RabbitTemplate rabbitTemplate;
    private final GroqService groqService;

    @Value("${amqp.exchange.name:image-processing-exchange}")
    private String exchangeName;

    @Value("${amqp.routing.key:image-processing}")
    private String routingKey;

    private void sendMessage(Object message) {
        try {
            this.rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
            log.info("Mensagem enviada para a fila AMQP: " + message);
        } catch (Exception e) {
            log.severe("Erro ao enviar mensagem para AMQP: " + e.getMessage());
            throw new RuntimeException("Falha ao enviar mensagem para fila", e);
        }
    }

    public void sendImageProcessingMessage(String imageId, String imageUrl, String description) {
        Map<String, Object> message = Map.of(
                "imageId", imageId,
                "imageUrl", imageUrl,
                "description", description,
                "timestamp", System.currentTimeMillis()
        );
        this.sendMessage(message);
    }

    @RabbitListener(queues = "${amqp.queue.name:image-processing-queue}")
    public void receiveImageProcessingMessage(Map<String, Object> message) {
        try {
            log.info("Mensagem recebida da fila AMQP: " + message);
            
            String imageId =  message.get("imageId").toString();
            var imagem = this.getFile(Long.valueOf(imageId));
            String imageUrl =  message.get("imageUrl").toString();
            String description = message.get("description").toString();
            Long timestamp = (Long) message.get("timestamp");
            
            log.info("Processando imagem - ID: " + imageId + ", URL: " + imageUrl);
            
            // Processa a imagem com IA via Groq
            String enrichedDescription = this.groqService.improveDescription(description);
            
            // Atualiza a descrição enriquecida no banco
            if (imagem.isPresent()) {
                DadosImagem dadosImagem = imagem.get();
                dadosImagem.setConteudo(enrichedDescription);
                this.dadosImagemRepository.save(dadosImagem);
                log.info("Descrição enriquecida salva para imagem " + imageId);
            }
            
        } catch (Exception e) {
            log.severe("Erro ao processar mensagem da fila: " + e.getMessage());
        }
    }

    private Optional<DadosImagem> getFile(Long id) {
        return this.dadosImagemRepository.findById(id);
    }
}
