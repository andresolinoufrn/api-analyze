package br.ufrn.analyze.service;

import br.ufrn.analyze.analyzer.DEPRECATEDIndividualThresholdAnalyzer;
import br.ufrn.analyze.analyzer.ThresholdAnalyzer;
import br.ufrn.analyze.domain.entity.AnalyzeConfig;
import br.ufrn.analyze.repository.AnalyzeConfigDAO;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AnalyzerService {

    @Autowired
    private Logger logger;

    @Autowired
    private AnalyzeConfigDAO analyzeConfigDAO;

//    @Autowired
//    private DEPRECATEDIndividualThresholdAnalyzer DEPRECATEDIndividualThresholdAnalyzer;

    @Autowired
    private ThresholdAnalyzer thresholdAnalyzer;

    public void execute(){
        logger.info("[Analyzer] started");
        Optional<AnalyzeConfig> analyzeConfigOptional = analyzeConfigDAO.findById(Long.valueOf(1));
        if (!analyzeConfigOptional.isEmpty()){
            AnalyzeConfig analyzeConfig = analyzeConfigOptional.get();
            analyzeConfig.setAnalysisNumber(analyzeConfig.getAnalysisNumber()+1);
            //DEPRECATEDIndividualThresholdAnalyzer.execute(analyzeConfig);
            thresholdAnalyzer.execute(analyzeConfig);
            analyzeConfigDAO.save(analyzeConfig);
        }else{
            //TODO retornar mensagem que no existe configuraço e disparar excecao
            logger.error("Configuration not found");
        }
    }

}
