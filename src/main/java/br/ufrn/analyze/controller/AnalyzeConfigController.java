package br.ufrn.analyze.controller;

import br.ufrn.analyze.domain.entity.AnalyzeConfig;
import br.ufrn.analyze.domain.entity.Experimento;
import br.ufrn.analyze.repository.AnalyzeConfigDAO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/api/analyzeconfig")
public class AnalyzeConfigController {

    private AnalyzeConfigDAO analyzeConfigDAO;

    public AnalyzeConfigController(AnalyzeConfigDAO analyzeConfigDAO) {
        this.analyzeConfigDAO = analyzeConfigDAO;
    }

    @GetMapping("/ola/{nome}")
    @ResponseBody
    public String ola(@PathVariable(name = "nome") String nome){
        return String.format("Ola %s!", nome);
    }

    @GetMapping("{id}")
    public AnalyzeConfig getAnalyzeConfigById( @PathVariable Long id ){
        return analyzeConfigDAO
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Configuração não encontrada"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnalyzeConfig save(@RequestBody AnalyzeConfig analyzeConfig ){
        return analyzeConfigDAO.save(analyzeConfig);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete( @PathVariable Long id ){
        analyzeConfigDAO.findById(id)
                .map( config -> {
                    analyzeConfigDAO.delete(config );
                    return config;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Configuracao não encontrada") );

    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update( @PathVariable Long id,
                        @RequestBody AnalyzeConfig config ){
        analyzeConfigDAO
                .findById(id)
                .map( configExistente -> {
                    config.setId(configExistente.getId());
                    analyzeConfigDAO.save(config);
                    return configExistente;
                }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Configuracao não encontrado") );
    }



    @PutMapping("/update/{id}")
    public void update(@PathVariable Long id,
                       @RequestParam(name="parameter", required = true) String parameter,
                       @RequestParam(name="value", required = true) String value) {


        Optional<AnalyzeConfig> analyzeConfigOptional = analyzeConfigDAO.findById(Long.valueOf(id));
        if (analyzeConfigOptional.isEmpty()){
            throw  new ResponseStatusException(HttpStatus.NOT_FOUND, "Configuracao não encontrada");
        }else{
            AnalyzeConfig analyzeConfig = analyzeConfigOptional.get();
            if (parameter.compareTo("updateFrequency") == 0 ){
                analyzeConfig.setUpdateFrequency(Integer.valueOf(value));
                analyzeConfigDAO.save(analyzeConfig);
            }else if (parameter.compareTo("historyInterval") == 0 ){
                analyzeConfig.setHistoryInterval(Integer.valueOf(value));
                analyzeConfigDAO.save(analyzeConfig);
            }else if (parameter.compareTo("heatThreshold") == 0 ){
                analyzeConfig.setHeatThreshold(Integer.valueOf(value));
                analyzeConfigDAO.save(analyzeConfig);
            }else {
                throw  new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parametro invalido");
            }
        }

    }


}
