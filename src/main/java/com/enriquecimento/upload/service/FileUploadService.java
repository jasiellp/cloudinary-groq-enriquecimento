package com.enriquecimento.upload.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import com.cloudinary.Transformation;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.enriquecimento.upload.entity.DadosImagem;

import com.enriquecimento.upload.repository.DadosImagemRepository;


import java.util.Optional;
import java.util.logging.Level;

import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import java.io.FileOutputStream;
import java.io.File;
import java.util.Map;


@Log
@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final DadosImagemRepository dadosImagemRepository;
    private final AmqpService amqpService;
    
    @Value("${cloudinary.url}")
    private String cloudinaryUrl;
    

    public DadosImagem saveFile(MultipartFile file, String nomeArquivo, String descricao) throws Exception {
        
                
        File tempFile = File.createTempFile("imagem_", "_" + nomeArquivo);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }

        String url = this.upload(nomeArquivo, tempFile);
        
        DadosImagem imagem = DadosImagem.builder()
        .nomeArquivo(nomeArquivo)
        .descricao(descricao)
        .conteudo("")
        .linkPublico(url)
        .build();

        DadosImagem savedImagem = this.dadosImagemRepository.save(imagem);
        
        // Envia mensagem para a fila AMQP para processamento
        try {
            this.amqpService.sendImageProcessingMessage(
                savedImagem.getId().toString(),
                url,
                descricao
            );
        } catch (Exception e) {
            log.warning("Falha ao enviar mensagem para fila AMQP: " + e.getMessage());
        }

        return savedImagem;
    }

    public Optional<DadosImagem> getFile(Long id) {
        return this.dadosImagemRepository.findById(id);
    }

    private String upload(String nomeImagem,  File arquivoImagem) {
        // Set your Cloudinary credentials
        Cloudinary cloudinary = new Cloudinary(cloudinaryUrl);
        cloudinary.config.secure = true;
        log.info(cloudinary.config.cloudName);

        try {

            log.info("Iniciando upload");

            // Upload the image
            Map params1 = ObjectUtils.asMap(
                "use_filename", true,
                "unique_filename", false,
                "overwrite", true
            );
            Map uploadResult = cloudinary.uploader().upload(arquivoImagem, params1);
            String publicId = uploadResult.get("public_id").toString();
            log.info(uploadResult.toString());
            // Get the asset details
            Map params2 = ObjectUtils.asMap(
                    "quality_analysis", true
            );
            Map uploadResponse = cloudinary.api().resource(publicId, params2);
            log.info(uploadResponse.toString());

            // Create the image tag with the transformed image and log it to the console
            
            String url = cloudinary.url().transformation(new Transformation()
                    .crop("pad")
                    .width(300)
                    .height(400)
                    .background("auto:predominant"))
                    .imageTag(publicId);
            return url;
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage());
            return null; // Return null in case of error
        }
    }
}
