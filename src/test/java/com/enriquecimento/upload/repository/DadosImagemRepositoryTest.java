package com.enriquecimento.upload.repository;

import com.enriquecimento.upload.entity.DadosImagem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
class DadosImagemRepositoryTest {

    @Autowired
    private DadosImagemRepository repository;

    @Test
    void deveSalvarERecuperarDadosImagem() {
        // Arrange
        DadosImagem dadosImagem = DadosImagem.builder()
                .nomeArquivo("teste.jpg")
                .descricao("Imagem de teste")
                .linkPublico("https://example.com/image.jpg")
                .conteudo("Descrição enriquecida pela IA")
                .build();

        // Act
        DadosImagem saved = repository.save(dadosImagem);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getNomeArquivo()).isEqualTo("teste.jpg");
        assertThat(saved.getDescricao()).isEqualTo("Imagem de teste");
        assertThat(saved.getLinkPublico()).isEqualTo("https://example.com/image.jpg");
        assertThat(saved.getConteudo()).isEqualTo("Descrição enriquecida pela IA");
    }

    @Test
    void deveAtualizarDadosImagem() {
        // Arrange
        DadosImagem dadosImagem = DadosImagem.builder()
                .nomeArquivo("original.jpg")
                .descricao("Descrição original")
                .linkPublico("https://example.com/original.jpg")
                .conteudo("")
                .build();

        DadosImagem saved = repository.save(dadosImagem);

        // Act
        saved.setConteudo("Nova descrição enriquecida pela IA");
        DadosImagem updated = repository.save(saved);

        // Assert
        assertThat(updated.getConteudo()).isEqualTo("Nova descrição enriquecida pela IA");
        assertThat(updated.getId()).isEqualTo(saved.getId());
    }

    @Test
    void deveEncontrarPorId() {
        // Arrange
        DadosImagem dadosImagem = DadosImagem.builder()
                .nomeArquivo("busca.jpg")
                .descricao("Imagem para busca")
                .linkPublico("https://example.com/busca.jpg")
                .conteudo("")
                .build();

        DadosImagem saved = repository.save(dadosImagem);

        // Act
        var found = repository.findById(saved.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getNomeArquivo()).isEqualTo("busca.jpg");
    }

    @Test
    void deveRetornarVazioParaIdInexistente() {
        // Act
        var found = repository.findById(999L);

        // Assert
        assertThat(found).isEmpty();
    }
}
