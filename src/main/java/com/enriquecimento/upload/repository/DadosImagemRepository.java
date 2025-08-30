package com.enriquecimento.upload.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.enriquecimento.upload.entity.DadosImagem;

public interface DadosImagemRepository extends JpaRepository<DadosImagem, Long> {
}
