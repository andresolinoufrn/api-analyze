package br.ufrn.analyze.service;

import org.springframework.stereotype.Service;



@Service
public class PlatformAnalyzeService {

//    @Autowired
//    private RestTemplate restTemplate;
//    @Autowired
//    private AnalyzeConfigDAO analyzeConfigDAO;
//    @Autowired
//    private ChangeSuggestionDAO changeSuggestionDAO;
//    @Autowired
//    private Logger logger;

//    public void analyzer() {
//        logger.info("Platform Level - Analyzer started");
//        Optional<AnalyzeConfig> analyzeConfigOptional = analyzeConfigDAO.findById(Long.valueOf(1));
//        if (!analyzeConfigOptional.isEmpty()){
//            AnalyzeConfig analyzeConfig = analyzeConfigOptional.get();
//            operationAnalyzer(analyzeConfig);
//
//        }else{
//            //TODO retornar mensagem que no existe configuraço
//            logger.error("Configuration not found");
//        }
//    }
//
//    private void operationAnalyzer(AnalyzeConfig analyzeConfig){
//        logger.info("Platform Level - operation time analyze started");
//        logger.info("Platform Level - operation time threshold Up: " + analyzeConfig.getOperationTimeThresholdUp());
//        MonitoredPlatformOperationDTO[] monitoredPlataformOperationDTOS = restTemplate.getForObject(
//                analyzeConfig.getPlatformMonitorApiUrl(), MonitoredPlatformOperationDTO[].class);
//
//        for (MonitoredPlatformOperationDTO monitoredPlataformOperationDTO : monitoredPlataformOperationDTOS){
//            if (monitoredPlataformOperationDTO.getOperationTime() > analyzeConfig.getOperationTimeThresholdUp()){
//                logger.info("Platform Level - operation " + monitoredPlataformOperationDTO.getOperationName() + " " +
//                        "execution time greater than the limit ( " + monitoredPlataformOperationDTO.getOperationTime() +
//                        " " + monitoredPlataformOperationDTO.getUnitTime() + " )");
//                // Aqui ha uma diferenca na analise. A analise da operacao  eh realizada e ja deve ser feito algum calculo
//                // para indicar qual recurso ( pod ou deployment ) devera ser aumentado. No caso de operacao interna, sera
//                //sempre para up. Nao ha um threshold para tempo de operacao, nao faz sentido diminuir recursos se a operacao
//                //esta rapida, deixar a diminuicao de recursos para o monitoramento de CPU e memoria
//                // TODO verificar se mais de um pod do deploy esta com o tempo de operaçacao alto. Se varios pods estiverem com o tempo de operacao alto,
//                // fazer o up do deployment, caso seja apenas um pod com tempo alto, fazer a sugestao de up apenas para o pod
//                Random random = new Random(); // uma variavel por enquanto que nao faco o calculo correto para saber se e o deploy ou o pod
//                if (random.nextBoolean()){
//                    createChangeSuggestion("PLATFORM","OPERATION", monitoredPlataformOperationDTO.getDeploymentName(), "UP");
//                }else{
//                    createChangeSuggestion("PLATFORM","OPERATION", monitoredPlataformOperationDTO.getPodName(), "UP");
//                }
//
//            }
//        }
//        logger.info("Platform Level - Total analyzed operations: " + monitoredPlataformOperationDTOS.length);
//    }


//    private double calculateAverageOperationTime(MonitoredPlatformOperationDTO[] monitoredPlataformOperationDTOS) {
//        int count = 0;
//        double value = 0.0;
//        for (MonitoredPlatformOperationDTO item:monitoredPlataformOperationDTOS){
//            value+=item.getOperationTime();
//            count++;
//        }
//        value/=count;
//        return  value;
//    }

//    private ChangeSuggestion createChangeSuggestion(String monitoredItem, String itemType, String resourceName, String scaleAction){
//        ChangeSuggestion changeSuggestion = new ChangeSuggestion();
//        changeSuggestion.setMonitoredItem(monitoredItem);
//        changeSuggestion.setItemType(itemType);
//        changeSuggestion.setResourceName(resourceName);
//        changeSuggestion.setThresholdViolation(scaleAction);
//        return changeSuggestionDAO.save(changeSuggestion);
//    }

}
