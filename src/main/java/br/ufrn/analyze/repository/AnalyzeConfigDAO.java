package br.ufrn.analyze.repository;

import br.ufrn.analyze.domain.entity.AnalyzeConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalyzeConfigDAO extends JpaRepository<AnalyzeConfig, Integer> {

    Optional<AnalyzeConfig> findById(Long id);
}
