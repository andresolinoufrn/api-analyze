package br.ufrn.analyze.domain.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Essa classe foi criada apenas para auxiliar durante os experimentos. Pode ser removida ( e suas dependencias ).
 */
@Entity
public class Experimento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    String experimentoAtual;
    String cenarioAtual;

    Integer repeticao;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExperimentoAtual() {
        return experimentoAtual;
    }

    public void setExperimentoAtual(String experimentoAtual) {
        this.experimentoAtual = experimentoAtual;
    }

    public String getCenarioAtual() {
        return cenarioAtual;
    }

    public void setCenarioAtual(String cenarioAtual) {
        this.cenarioAtual = cenarioAtual;
    }

    public Integer getRepeticao() {
        return repeticao;
    }

    public void setRepeticao(Integer repeticao) {
        this.repeticao = repeticao;
    }
}
