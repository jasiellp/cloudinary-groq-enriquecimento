package com.enriquecimento.upload.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TBL_Dados_Image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DadosImagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeArquivo;

    @Column(length = 1000)
    private String linkPublico;
    
    @Column(length = 1000)
    private String descricao;

    @Column(length = 4000)
    private String conteudo;
}
