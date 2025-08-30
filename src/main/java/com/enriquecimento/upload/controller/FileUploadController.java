package com.enriquecimento.upload.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.enriquecimento.upload.service.FileUploadService;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService service;

    @PostMapping(value = "/upload", consumes = { "multipart/form-data" })
    @Operation(summary = "Faz upload de uma imagem com descrição")
    public ResponseEntity<?> uploadFile(
            @RequestPart("arquivo") MultipartFile arquivo,
            @RequestPart("nomeArquivo") String nomeArquivo,
            @RequestPart("descricao") String descricao) {

        // Verifica se o arquivo é uma imagem
        String contentType = arquivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity
                    .badRequest()
                    .body("O arquivo enviado não é uma imagem válida.");
        }

        try {
            return ResponseEntity.ok(this.service.saveFile(arquivo, nomeArquivo, descricao));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta o resultado do processamento de uma imagem pelo ID")
    public ResponseEntity<?> getImageResult(@PathVariable Long id) {
        try {
            var result = this.service.getFile(id);
            
            if (result.isPresent()) {
                return ResponseEntity.ok(result.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
