package com.enriquecimento.upload.controller;

import com.enriquecimento.upload.entity.DadosImagem;
import com.enriquecimento.upload.service.FileUploadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = FileUploadController.class)
class FileUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileUploadService service;

    @Test
    void uploadFile_deveRetornarBadRequestParaArquivoNaoImagem() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "doc.txt", "text/plain", "abc".getBytes());
        MockPart nomeArquivo = new MockPart("nomeArquivo", "doc.txt".getBytes());
        MockPart descricao = new MockPart("descricao", "texto".getBytes());

        this.mockMvc.perform(multipart("/api/files/upload")
                        .file(arquivo)
                        .part(nomeArquivo)
                        .part(descricao))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("O arquivo enviado não é uma imagem válida."));
    }

    @Test
    void uploadFile_deveRetornarOkQuandoImagemValida() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "img.png", "image/png", new byte[]{1});
        MockPart nomeArquivo = new MockPart("nomeArquivo", "img.png".getBytes());
        MockPart descricao = new MockPart("descricao", "uma imagem".getBytes());
        
        DadosImagem mockImagem = DadosImagem.builder()
            .id(1L)
            .nomeArquivo("img.png")
            .descricao("uma imagem")
            .build();
        
        Mockito.when(this.service.saveFile(any(), any(), any())).thenReturn(mockImagem);

        this.mockMvc.perform(multipart("/api/files/upload")
                        .file(arquivo)
                        .part(nomeArquivo)
                        .part(descricao))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nomeArquivo").value("img.png"));
    }

    @Test
    void getImageResult_deveRetornarImagemQuandoEncontrada() throws Exception {
        // Arrange
        Long imageId = 1L;
        DadosImagem mockImagem = DadosImagem.builder()
            .id(imageId)
            .nomeArquivo("test.jpg")
            .descricao("Imagem de teste")
            .conteudo("Descrição enriquecida pela IA")
            .linkPublico("https://example.com/image.jpg")
            .build();

        Mockito.when(this.service.getFile(imageId)).thenReturn(Optional.of(mockImagem));

        // Act & Assert
        this.mockMvc.perform(get("/api/files/{id}", imageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(imageId))
                .andExpect(jsonPath("$.nomeArquivo").value("test.jpg"))
                .andExpect(jsonPath("$.descricao").value("Imagem de teste"))
                .andExpect(jsonPath("$.conteudo").value("Descrição enriquecida pela IA"))
                .andExpect(jsonPath("$.linkPublico").value("https://example.com/image.jpg"));
    }

    @Test
    void getImageResult_deveRetornarNotFoundQuandoImagemNaoEncontrada() throws Exception {
        // Arrange
        Long imageId = 999L;
        Mockito.when(this.service.getFile(imageId)).thenReturn(Optional.empty());

        // Act & Assert
        this.mockMvc.perform(get("/api/files/{id}", imageId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getImageResult_deveRetornarInternalServerErrorQuandoExcecao() throws Exception {
        // Arrange
        Long imageId = 1L;
        Mockito.when(this.service.getFile(imageId)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        this.mockMvc.perform(get("/api/files/{id}", imageId))
                .andExpect(status().isInternalServerError());
    }
}


