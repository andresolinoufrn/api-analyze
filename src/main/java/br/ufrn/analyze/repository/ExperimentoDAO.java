package br.ufrn.analyze.repository;

import br.ufrn.analyze.domain.entity.Experimento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExperimentoDAO extends JpaRepository<Experimento, Integer> {

    Optional<Experimento> findById(Long id);
}
