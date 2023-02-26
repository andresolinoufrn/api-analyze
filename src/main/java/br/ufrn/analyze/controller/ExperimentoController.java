package br.ufrn.analyze.controller;

import br.ufrn.analyze.domain.entity.Experimento;
import br.ufrn.analyze.repository.ExperimentoDAO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/experimento")
public class ExperimentoController {

    private final ExperimentoDAO experimentoDAO;

    public ExperimentoController(ExperimentoDAO experimentoDAO) {
        this.experimentoDAO = experimentoDAO;
    }

    @GetMapping("{id}")
    public Experimento getById(@PathVariable Long id ){
        return experimentoDAO
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Configuração não encontrada"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Experimento save(@RequestBody Experimento experimento ){
        return experimentoDAO.save(experimento);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete( @PathVariable Long id ){
        experimentoDAO.findById(id)
                .map( experimento -> {
                    experimentoDAO.delete(experimento);
                    return experimento;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Configuracao não encontrada") );

    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update( @PathVariable Long id,
                        @RequestBody Experimento experimento ){
        experimentoDAO
                .findById(id)
                .map( experimentoExistente -> {
                    experimento.setId(experimentoExistente.getId());
                    experimentoDAO.save(experimento);
                    return experimentoExistente;
                }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Configuracao não encontrado") );
    }


}
