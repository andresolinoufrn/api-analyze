package br.ufrn.analyze.controller;


import br.ufrn.analyze.service.*;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/analyzer")
public class AnalyzeController {

    @Autowired
    private AnalyzerJobService analyzerJobService;

    @Autowired
    private PlatformAnalyzeService platformAnalyzeService;

    @Autowired
    private AnalyzerService analyzerService;

    @GetMapping("/start")
    @ResponseBody
    public String start(){
        try {
            analyzerJobService.startAnalyzer();
        }catch (ObjectAlreadyExistsException objectAlreadyExistsException){
            return "Analisador ja iniciado";
        } catch (SchedulerException | IOException e) {
            throw new RuntimeException(e);
        }
        return "Analisador iniciado";
    }

    @GetMapping("/stop")
    @ResponseBody
    public String stop(){
        try {
            analyzerJobService.stopAnalyzer();
        } catch (SchedulerException | IOException e) {
            throw new RuntimeException(e);
        }
        return "Analisador parado";
    }

    @GetMapping("/doAnalysis")
    @ResponseBody
    public String doAnalysis(){
        //suggesterService.execute();
        //analyzerService.execute();
        return "Analise realizada";
    }

    @GetMapping("/doAnalysisNovo")
    @ResponseBody
    public String doAnalysisNovo(){
        analyzerService.execute();
        return "Analise nova realizada";
    }

}
