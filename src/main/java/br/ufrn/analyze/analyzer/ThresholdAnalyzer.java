package br.ufrn.analyze.analyzer;

import br.ufrn.analyze.domain.constants.ItemType;
import br.ufrn.analyze.domain.constants.ResourceType;
import br.ufrn.analyze.domain.constants.ResourceUnit;
import br.ufrn.analyze.domain.constants.ThresholdViolation;
import br.ufrn.analyze.domain.dto.apimonitor.MonitoredItemDTO;
import br.ufrn.analyze.domain.entity.AnalyzeConfig;
import br.ufrn.analyze.domain.entity.ChangeRequest;
import br.ufrn.analyze.domain.entity.Experimento;
import br.ufrn.analyze.repository.ChangeRequestDAO;
import br.ufrn.analyze.repository.ExperimentoDAO;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Second version of analyzer
 */
@Component
@Transactional
public class ThresholdAnalyzer {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private Logger logger;

    @Autowired
    private ChangeRequestDAO changeRequestDAO;

    @Autowired
    private ExperimentoDAO experimentoDAO;

    public void execute(AnalyzeConfig analyzeConfig) {
        collectData(analyzeConfig);
    }

    public void collectData(AnalyzeConfig analyzeConfig) {
        //List<String> clusterNodes = restTemplate.getForObject("http://localhost:8080/api-monitor/k8s/workerNodesFromCluster", ArrayList.class);

        //cluster tem apenas nos
        List<String> clusterNodes = restTemplate.getForObject(analyzeConfig.getClusterNodesMonitorApiURL(), ArrayList.class);

        clusterAnalyze(analyzeConfig, ResourceType.CPU, clusterNodes);
        clusterAnalyze(analyzeConfig, ResourceType.MEMORY, clusterNodes);

        //no cluster tenho varios deployments e cada deployment tem varios pods
        String namespace = analyzeConfig.getNamespaceToAnalise();
        List<String> deploymentNames = restTemplate.getForObject(analyzeConfig.getDeploymentsFromNamespaceUrl() + "/" + namespace, ArrayList.class);

        for (String deploymentName : deploymentNames) {
            List<String> matchLabels = restTemplate.getForObject(analyzeConfig.getMatchLabelsFromNamespaceAndDeploymentUrl() + "/" + namespace + "/" + deploymentName, ArrayList.class);
            for (String matchLabel : matchLabels) {
                List<String> podList = restTemplate.getForObject(analyzeConfig.getPodsFromNamespaceAndMatchLabelsUrl() + "/" + namespace + "/" + matchLabel, ArrayList.class);
                logger.info("[ThresholdAnalyzer] Analyzer namespace " + namespace + " mathlabel " + matchLabel);
                deploymentAnalyze(analyzeConfig, ResourceType.CPU, deploymentName, podList);
                deploymentAnalyze(analyzeConfig, ResourceType.MEMORY, deploymentName, podList);
                platformAnalyze(analyzeConfig, deploymentName, podList);
            }
        }

        scalingHeat();

        sendChangeRequest(analyzeConfig, ItemType.CLUSTER, ResourceType.CPU);
        sendChangeRequest(analyzeConfig, ItemType.CLUSTER, ResourceType.MEMORY);

        sendChangeRequest(analyzeConfig, ItemType.DEPLOYMENT, ResourceType.CPU);
        sendChangeRequest(analyzeConfig, ItemType.DEPLOYMENT, ResourceType.MEMORY);

        sendChangeRequest(analyzeConfig, ItemType.PLATFORM, ResourceType.OPERATION_TIME);
    }


    /**
     * Analyze VM ( cluster and individual VMs)
     *
     * @param analyzeConfig
     * @param resourceType
     * @param items
     */
    private void clusterAnalyze(AnalyzeConfig analyzeConfig, String resourceType, List<String> items) {
        logger.info("[ThresholdAnalyzer] Cluster analysis. " + resourceType + ". Total nodes to analyze: " + items.size());
        String monitorURL;
        double thresholdUp = 0.0;
        double thresholdDown = 0.0;
        String unit = "";
        if (resourceType.compareTo(ResourceType.CPU) == 0) {
            monitorURL = analyzeConfig.getHostMonitorApiCpuUrl();
            thresholdUp = analyzeConfig.getHostCpuThresholdUp();
            thresholdDown = analyzeConfig.getHostCpuThresholdDown();
            unit = ResourceUnit.CORES;
        } else if (resourceType.compareTo(ResourceType.MEMORY) == 0) {
            monitorURL = analyzeConfig.getHostMonitorApiMemoryUrl();
            thresholdUp = analyzeConfig.getHostMemoryThresholdUp();
            thresholdDown = analyzeConfig.getHostMemoryThresholdDown();
            unit = ResourceUnit.GB;
        } else {
            logger.error("[ThresholdAnalyzer] Unsuported resource type. Expected + " + ResourceType.CPU + " or " + ResourceType.MEMORY);
            throw new RuntimeException("[ThresholdAnalyzer] Unsuported resource type. Expected + \" + ResourceType.CPU + \" or \" + ResourceType.MEMORY");
        }

        double totalResource = 0.0;
        double totalUsedResource = 0.0;
        double scaleQuantity;

        // Individual data
        for (String item : items) {
            MonitoredItemDTO monitoredItemDTO = restTemplate.getForObject(monitorURL + "/" + item + "?historyInterval=" + analyzeConfig.getHistoryInterval(), MonitoredItemDTO.class);

            scaleQuantity = suggestAmountOfResources(monitoredItemDTO.getUsedResource(),
                    monitoredItemDTO.getTotalResource(),
                    analyzeConfig.getHostCpuThresholdUp(),
                    analyzeConfig.getHostCpuThresholdDown());

            createChangeRequest(analyzeConfig.getAnalysisNumber(),
                    ItemType.VM,
                    item,
                    item,
                    "CLUSTER",
                    null,
                    null,
                    null,
                    monitoredItemDTO.getResourceType(),
                    monitoredItemDTO.getUsedResource(),
                    monitoredItemDTO.getTotalResource(),
                    monitoredItemDTO.getResourceUnit(),
                    thresholdUp,
                    thresholdDown,
                    scaleQuantity);

            totalResource = totalResource + monitoredItemDTO.getTotalResource();
            totalUsedResource = totalUsedResource + (monitoredItemDTO.getUsedResource() * monitoredItemDTO.getTotalResource());
        }
        // Cluster data
        scaleQuantity = suggestAmountOfResources(totalUsedResource / totalResource,
                totalResource,
                thresholdUp,
                thresholdDown);

        createChangeRequest(analyzeConfig.getAnalysisNumber(),
                ItemType.CLUSTER,
                "CLUSTER",
                null,
                "CLUSTER",
                null,
                null,
                null,
                resourceType,
                totalUsedResource / totalResource,
                totalResource,
                unit,
                thresholdUp,
                thresholdDown,
                scaleQuantity);
    }

    /**
     * Analyze Deployment ( Deployment and individual PODs)
     *
     * @param analyzeConfig
     * @param resourceType
     * @param items
     */
    private void deploymentAnalyze(AnalyzeConfig analyzeConfig, String resourceType, String deploymentName, List<String> items) {
        logger.info("[ThresholdAnalyzer] Deployment " + deploymentName + ". " + resourceType + ". Total pods to analyze: " + items.size());
        String monitorURL;
        double thresholdUp = 0.0;
        double thresholdDown = 0.0;
        String unit = "";
        if (resourceType.compareTo(ResourceType.CPU) == 0) {
            monitorURL = analyzeConfig.getContainerMonitorApiCpuUrl();
            thresholdUp = analyzeConfig.getContainerCpuThresholdUp();
            thresholdDown = analyzeConfig.getContainerCpuThresholdDown();
            unit = ResourceUnit.NANOCORES;
        } else if (resourceType.compareTo(ResourceType.MEMORY) == 0) {
            monitorURL = analyzeConfig.getContainerMonitorApiMemoryUrl();
            thresholdUp = analyzeConfig.getContainerMemoryThresholdUp();
            thresholdDown = analyzeConfig.getContainerMemoryThresholdDown();
            unit = ResourceUnit.BYTE;
        } else {
            logger.error("[ThresholdAnalyzer] Unsuported resource type. Expected + " + ResourceType.CPU + " or " + ResourceType.MEMORY);
            throw new RuntimeException("[ThresholdAnalyzer] Unsuported resource type. Expected + \" + ResourceType.CPU + \" or \" + ResourceType.MEMORY");
        }

        double totalDeploymentResource = 0.0;
        double totalDeploymentUsedResource = 0.0;
        double scaleQuantity = 0.0;

        // Individual data
        for (String item : items) {
            MonitoredItemDTO monitoredItemDTO = restTemplate.getForObject(monitorURL + "/" + item+ "?historyInterval=" + analyzeConfig.getHistoryInterval(), MonitoredItemDTO.class);
            scaleQuantity = suggestAmountOfResources(monitoredItemDTO.getUsedResource(),
                    monitoredItemDTO.getTotalResource(),
                    analyzeConfig.getContainerCpuThresholdUp(),
                    analyzeConfig.getContainerCpuThresholdDown());

            createChangeRequest(analyzeConfig.getAnalysisNumber(),
                    ItemType.POD,
                    item,
                    null,
                    null,
                    item,
                    deploymentName,
                    null,
                    monitoredItemDTO.getResourceType(),
                    monitoredItemDTO.getUsedResource(),
                    monitoredItemDTO.getTotalResource(),
                    monitoredItemDTO.getResourceUnit(),
                    thresholdUp,
                    thresholdDown,
                    scaleQuantity);

            totalDeploymentResource = totalDeploymentResource + monitoredItemDTO.getTotalResource();
            totalDeploymentUsedResource = totalDeploymentUsedResource + (monitoredItemDTO.getUsedResource() * monitoredItemDTO.getTotalResource());
        }

        double totalDeploymentUsedResourcePct = totalDeploymentUsedResource / totalDeploymentResource;
        if (Double.isInfinite(totalDeploymentUsedResourcePct) || Double.isNaN(totalDeploymentUsedResourcePct)) {
            totalDeploymentUsedResourcePct = 0.0;
        }
        scaleQuantity = suggestAmountOfResources(totalDeploymentUsedResourcePct,
                totalDeploymentResource,
                thresholdUp,
                thresholdDown);

        createChangeRequest(analyzeConfig.getAnalysisNumber(),
                ItemType.DEPLOYMENT,
                deploymentName,
                null,
                null,
                null,
                deploymentName,
                null,
                resourceType,
                totalDeploymentUsedResourcePct,
                totalDeploymentResource,
                unit,
                thresholdUp,
                thresholdDown,
                scaleQuantity);
    }


    private void platformAnalyze(AnalyzeConfig analyzeConfig, String deploymentName, List<String> pods) {
        logger.info("[ThresholdAnalyzer] Platform  analyze");
        String monitorURL = analyzeConfig.getPlatformMonitorApiUrl();
        double thresholdUp = analyzeConfig.getOperationTimeThresholdUp();

        for (String pod : pods) {
            MonitoredItemDTO[] monitoredItemDTOList = restTemplate.getForObject(monitorURL + "/" + pod+ "?historyInterval=" + analyzeConfig.getHistoryInterval(), MonitoredItemDTO[].class);

            for (MonitoredItemDTO monitoredItemDTO : monitoredItemDTOList) {
                //System.out.println(monitoredItemDTO.getItemName() + " : " + monitoredItemDTO.getUsedResource());
                createChangeRequest(analyzeConfig.getAnalysisNumber(),
                        ItemType.PLATFORM,
                        monitoredItemDTO.getItemName(),
                        null,
                        null,
                        pod,
                        deploymentName,
                        monitoredItemDTO.getItemName(),
                        monitoredItemDTO.getResourceType(),
                        monitoredItemDTO.getUsedResource(),
                        monitoredItemDTO.getTotalResource(),
                        monitoredItemDTO.getResourceUnit(),
                        thresholdUp,
                        0.0,
                        0.0
                );
            }
        }
    }


    /**
     * Calculate the amount of resources that should be added or removed according to the scaling action
     */
    private Double suggestAmountOfResources(Double usedResource, Double totalResource, Double thresholdUp, Double thresholdDown) {
        if (totalResource == 0) {
            // se não for possível calcular o valor total de recursos, o api-monitor configura o valor em 0.
            // lembre que o total de recursos é calculado pelo percentual de uso e uso efetivo, quando o percentual
            // de uso é retornado 0, o valor de recursos total vai para infinito ( divisão por numero muito pequeno)
            return 0.0;
        }

        Double recommendedAmount = 0.0;

        if (thresholdUp <= thresholdDown) {
            logger.error("[IndividualThresholdAnalyzer]      Invalid configuration: thresholdUp <= thresholdDown ");
        } else {
            Double factor = 1.1; // TODO PARAMETRIZAR ESSE VALOR - 1.1 significa que vai aumentar 10% a cada tentantiva de iteração

            Double totalUsedResourceAbsolute = usedResource * totalResource;
            Double absoluteThresholdUp = thresholdUp * totalResource;
            Double absoluteThresholdDown = thresholdDown * totalResource;
            Double usedResourceTmp = usedResource;

//            logger.info("[IndividualThresholdAnalyzer]      ThresholdUp: " + thresholdUp);
//            logger.info("[IndividualThresholdAnalyzer]      ThresholdDown: " + thresholdDown);
//            logger.info("[IndividualThresholdAnalyzer]      Used Resource ( % ) : " + usedResource);
//            logger.info("[IndividualThresholdAnalyzer]      Used Resource (absolute) : " + totalUsedResourceAbsolute);
//            logger.info("[IndividualThresholdAnalyzer]      Total Resource: " + totalResource);

            if (usedResource > thresholdUp) {  //amount is above thresholds
                //logger.info("[IndividualThresholdAnalyzer]      upper threshold violation ");
                recommendedAmount = totalUsedResourceAbsolute - (absoluteThresholdUp);
                while (usedResourceTmp > thresholdUp) {
                    recommendedAmount = recommendedAmount * factor; //
                    //logger.info("[IndividualThresholdAnalyzer]        suggested amount to add: " + recommendedAmount);
                    usedResourceTmp = totalUsedResourceAbsolute / (totalResource + recommendedAmount);
                    //logger.info("[IndividualThresholdAnalyzer]        expected usage after addition: " + usedResourceTmp);
                }
            } else if (usedResource < thresholdDown) { //amount is below thresholds
                //logger.info("[IndividualThresholdAnalyzer]      down threshold violation ");
                recommendedAmount = totalUsedResourceAbsolute - (absoluteThresholdDown);
                while (usedResourceTmp < thresholdDown) {
                    if ((-1 * (recommendedAmount * factor)) > totalResource) { // tenta removar mais recursos que o total existente. nao multiplica pelo fator e utiliza o valor anterior recomendado
                        //logger.info("[IndividualThresholdAnalyzer]        maximum limit for removal reached: " + recommendedAmount);
                        break;
                    }
                    recommendedAmount = recommendedAmount * factor; //
                    //logger.info("[IndividualThresholdAnalyzer]        suggested amount to remove: " + recommendedAmount);
                    usedResourceTmp = totalUsedResourceAbsolute / (totalResource + recommendedAmount);
                    //logger.info("[IndividualThresholdAnalyzer]        expected usage after addition: " + usedResourceTmp);
                }
            } else { // amount is within the thresholds
                //logger.info("[IndividualThresholdAnalyzer]      no threshold violation ");
            }
        }
        return recommendedAmount;
    }

    private ChangeRequest createChangeRequest(
            Long analysisNumber,
            String itemType,
            String itemName,
            String vmName,
            String vmClusterName,
            String podName,
            String deploymentName,
            String operationName,
            String resourceType,
            Double usedResource,
            Double totalResource,
            String resourceUnit,
            Double thresholdUp,
            Double thresholdDown,
            Double scaleQuantity
    ) {
        if (usedResource == null || usedResource.isInfinite() || usedResource.isNaN()) {
            usedResource = 0.0;
        }
        if (scaleQuantity == null || scaleQuantity.isInfinite() || scaleQuantity.isNaN()) {
            scaleQuantity = 0.0;
        }
        if (totalResource == null || totalResource.isInfinite() || totalResource.isNaN()) {
            totalResource = 0.0;
        }

        ChangeRequest changeRequest = new ChangeRequest();
        changeRequest.setAnalysisNumber(analysisNumber);
        changeRequest.setItemType(itemType);
        changeRequest.setItemName(itemName);
        changeRequest.setVmName(vmName);
        changeRequest.setVmClusterName(vmClusterName);
        changeRequest.setPodName(podName);
        changeRequest.setDeploymentName(deploymentName);
        changeRequest.setOperationName(operationName);
        changeRequest.setResourceType(resourceType);
        changeRequest.setUsedResource(usedResource);
        changeRequest.setTotalResource(totalResource);
        changeRequest.setResourceUnit(resourceUnit);
        changeRequest.setThresholdUp(thresholdUp);
        changeRequest.setThresholdDown(thresholdDown);
        if (usedResource == 0) {
            changeRequest.setThresholdViolation(ThresholdViolation.UNDEFINED);
        } else if (usedResource < thresholdDown) {
            changeRequest.setThresholdViolation(ThresholdViolation.DOWN);
        } else if (usedResource > thresholdUp) {
            changeRequest.setThresholdViolation(ThresholdViolation.UP);
        } else {
            changeRequest.setThresholdViolation(ThresholdViolation.NONE);
        }
        changeRequest.setScaleQuantity(scaleQuantity);
        changeRequest.setHeat(0);
        //essa parte é apenas para o experimento
        Optional<Experimento> experimentoOptional = experimentoDAO.findById(Long.valueOf(1));
        if (!experimentoOptional.isEmpty()){
            Experimento experimento = experimentoOptional.get();
            changeRequest.setExperimento(experimento.getExperimentoAtual());
            changeRequest.setCenario(experimento.getCenarioAtual());
            changeRequest.setRepeticao(experimento.getRepeticao());
        }else{
            logger.error("Configuration not found");
        }
        return changeRequestDAO.saveAndFlush(changeRequest);
    }

    /**
     * Checks the "window" to decide whether to submit the ChangeRequest to the planning phase
     * A violation of upper thresholds, indicating a saturation of a container cluster, will result in increasing the
     * heat factor (lines 1–4), while a violation of lower thresholds, i.e., underutilized clusters, will result in
     * decreasing heat (lines 5–8). - https://drive.google.com/file/d/1ejXhZ2mFv02ZZtHqFkxHHj2H_1ebaKGz/view
     */
    private void scalingHeat() {
        Long lastAnalysisNumber = changeRequestDAO.getLasAnalysisNumber();
        if (changeRequestDAO.countAnalysisBeforeLast() == 0) { // existe apenas uma rodade de analise salva em banco
            changeRequestDAO.resetHeat();
        } else {
            int heat;
            List<ChangeRequest> changeRequestList = changeRequestDAO.findAllByAnalysisNumber(lastAnalysisNumber - 1);
            List<ChangeRequest> changeRequesLastList = changeRequestDAO.findAllByAnalysisNumber(lastAnalysisNumber);

            for (ChangeRequest changeRequest : changeRequestList) {
                for (ChangeRequest changeRequestLast : changeRequesLastList) {
                    if (changeRequest.getItemName().compareTo(changeRequestLast.getItemName()) == 0 && //pega o mesmo item da iteracao anterior e iteracao atual
                            changeRequest.getItemType().compareTo(changeRequestLast.getItemType()) == 0 &&
                            changeRequest.getResourceType().compareTo(changeRequestLast.getResourceType()) == 0) {
                        if (changeRequestLast.getThresholdViolation().compareTo(ThresholdViolation.UP) == 0) {
                            if (changeRequest.getHeat() <= 0) {
                                changeRequestLast.setHeat(1);
                            } else {
                                changeRequestLast.setHeat(changeRequest.getHeat() + 1);
                            }
                        } else if (changeRequestLast.getThresholdViolation().compareTo(ThresholdViolation.DOWN) == 0) {
                            if (changeRequest.getHeat() >= 0) {
                                changeRequestLast.setHeat(-1);
                            } else {
                                changeRequestLast.setHeat(changeRequest.getHeat() - 1);
                            }
                        } else if (changeRequestLast.getThresholdViolation().compareTo(ThresholdViolation.NONE) == 0) {
                            if (changeRequest.getHeat() > 0) {
                                changeRequestLast.setHeat(changeRequest.getHeat() - 1);
                            } else if (changeRequest.getHeat() < 0) {
                                changeRequestLast.setHeat(changeRequest.getHeat() + 1);
                            } else if (changeRequest.getHeat() == 0) {
                                changeRequestLast.setHeat(0);
                            }
                        }
                        changeRequestDAO.saveAndFlush(changeRequestLast);
                    }
                }
            }
        }
    }


    //** TODO - FAZER UM findAllByAnalysis
    private void sendChangeRequest(AnalyzeConfig analyzeConfig, String itemType, String resourceType) {
        Long lastAnalysisNumber = changeRequestDAO.getLasAnalysisNumber();

        //CLUSTER ANALYSIS -  CPU  AND MEMORY ANALYSIS
        if (itemType.compareTo(ItemType.CLUSTER) == 0) {
            List<ChangeRequest> changeRequestClusterList = changeRequestDAO.findAllByAnalysisNumberAndItemTypeAndResourceType(lastAnalysisNumber, itemType, resourceType);
            for (ChangeRequest changeRequestCluster : changeRequestClusterList) {
                if (Math.abs(changeRequestCluster.getHeat()) >= analyzeConfig.getHeatThreshold()) { // Mapa de calor do cluster atingido
                    changeRequestCluster.setHeat(0);
                    changeRequestCluster.setChangeRequestDate(new Date());
                    changeRequestDAO.saveAndFlush(changeRequestCluster);
                    //Se zerar o heat do cluster, zero o heat de todos as VMs que fazem parte do cluster
                    List<ChangeRequest> changeRequestVmList = changeRequestDAO.findAllByAnalysisNumberAndItemTypeAndResourceTypeAndVmClusterNameAndVmNameIsNotNull(
                            lastAnalysisNumber,
                            ItemType.VM,
                            resourceType,
                            changeRequestCluster.getVmClusterName());
                    for (ChangeRequest changeRequestVm : changeRequestVmList) {
                        changeRequestVm.setHeat(0); // zera o mapa de calor das vms que fazem parte do cluster
                        changeRequestDAO.saveAndFlush(changeRequestVm);
                    }
                } else {
                    List<ChangeRequest> changeRequestVmList = changeRequestDAO.findAllByAnalysisNumberAndItemTypeAndResourceTypeAndVmClusterNameAndVmNameIsNotNull(
                            lastAnalysisNumber,
                            ItemType.VM,
                            resourceType,
                            changeRequestCluster.getVmClusterName());
                    for (ChangeRequest changeRequestVm : changeRequestVmList) {
                        if (Math.abs(changeRequestVm.getHeat()) == analyzeConfig.getHeatThreshold()) {
                            changeRequestVm.setHeat(0);
                            changeRequestVm.setChangeRequestDate(new Date());
                            changeRequestDAO.saveAndFlush(changeRequestVm);
                            //Se zerar o heat de alguma VM, zero também o heat do cluster
                            changeRequestCluster.setHeat(0);
                            changeRequestDAO.saveAndFlush(changeRequestCluster);
                        }
                    }
                }
            }
        } else if (itemType.compareTo(ItemType.DEPLOYMENT) == 0) {
            //DEPLOYMENT ANALYSIS -  CPU  AND MEMORY ANALYSIS
            List<ChangeRequest> changeRequestDeploymentList = changeRequestDAO.findAllByAnalysisNumberAndItemTypeAndResourceType(lastAnalysisNumber, itemType, resourceType);
            for (ChangeRequest changeRequestDeployment : changeRequestDeploymentList) {
                if (Math.abs(changeRequestDeployment.getHeat()) >= analyzeConfig.getHeatThreshold()) { // Mapa de calor do cluster atingido
                    changeRequestDeployment.setHeat(0);
                    changeRequestDeployment.setChangeRequestDate(new Date());
                    changeRequestDAO.saveAndFlush(changeRequestDeployment);
                    //Se zerar o heat do cluster, zero o heat de todos os PODs que fazem parte do Deployment
                    List<ChangeRequest> changeRequestPodList = changeRequestDAO.findAllByAnalysisNumberAndItemTypeAndResourceTypeAndDeploymentNameAndPodNameIsNotNull(
                            lastAnalysisNumber,
                            ItemType.POD,
                            resourceType,
                            changeRequestDeployment.getDeploymentName());
                    for (ChangeRequest changeRequestPod : changeRequestPodList) {
                        changeRequestPod.setHeat(0); // zera o mapa de calor dos PODs que fazem parte do deployment
                        changeRequestDAO.saveAndFlush(changeRequestPod);
                    }
                } else {
                    List<ChangeRequest> changeRequestPodList = changeRequestDAO.findAllByAnalysisNumberAndItemTypeAndResourceTypeAndDeploymentNameAndPodNameIsNotNull(
                            lastAnalysisNumber,
                            ItemType.POD,
                            resourceType,
                            changeRequestDeployment.getDeploymentName());
                    for (ChangeRequest changeRequestPod : changeRequestPodList) {
                        if (Math.abs(changeRequestPod.getHeat()) == analyzeConfig.getHeatThreshold()) {
                            changeRequestPod.setHeat(0);
                            changeRequestPod.setChangeRequestDate(new Date());
                            changeRequestDAO.saveAndFlush(changeRequestPod);
                            //Se zerar o heat de alguma VM, zero também o heat do cluster
                            changeRequestDeployment.setHeat(0);
                            changeRequestDAO.saveAndFlush(changeRequestDeployment);
                        }
                    }
                }
            }
        } else if (itemType.compareTo(ItemType.PLATFORM) == 0) {
            //PLATFORM ANALYSIS -  OPERATION TIME
            List<ChangeRequest> changeRequestPlatformList = changeRequestDAO.findAllByAnalysisNumberAndItemTypeAndResourceType(lastAnalysisNumber, itemType, resourceType);
            for (ChangeRequest changeRequestPlatform : changeRequestPlatformList) {
                if (Math.abs(changeRequestPlatform.getHeat()) >= analyzeConfig.getHeatThreshold()) { // Mapa de calor da operacao atingido
                    changeRequestPlatform.setHeat(0);
                    changeRequestPlatform.setChangeRequestDate(new Date());
                    changeRequestDAO.saveAndFlush(changeRequestPlatform);
                }
            }
        } else {
            logger.error("Item type not supported");
        }
    }
}

// RASCUNHOS - IDEIAS

// rompido limite superior
// o analisador deve verificar se a prioridade sera scale horizntal ou vertical. Caso seja horizontal, fazer o scale horizontal. Verificar
// o tamanho necessariuo da nova VM
// se for vertical, verificar o host que esta mais sobrecarregado, e aumentar os recursos dele de forma que ele fique abaixo do thersold


//rompido limite inferior
//se a prioridade for horizontal, verificiar se ao remover o menor host o sistema nao ficara sobrecarregado. Caso no fique, remove esse host, se ficar
// nao remove o host e diz nao ser possvel
// se for vertical, verifica o quanto pode ser removido para nao diminuir e ja ficar sobrecarregado