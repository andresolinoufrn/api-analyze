package br.ufrn.analyze.analyzer;

import br.ufrn.analyze.domain.constants.ItemType;
import br.ufrn.analyze.domain.constants.ResourceType;
import br.ufrn.analyze.domain.constants.ResourceUnit;
import br.ufrn.analyze.domain.constants.ThresholdViolation;
import br.ufrn.analyze.domain.dto.apimonitor.GroupedItem;
import br.ufrn.analyze.domain.dto.apimonitor.IndividualItem;
import br.ufrn.analyze.domain.dto.apimonitor.MonitorConfig;
import br.ufrn.analyze.domain.dto.apimonitor.MonitoredItemDTO;
import br.ufrn.analyze.domain.entity.AnalyzeConfig;
import br.ufrn.analyze.domain.entity.ChangeRequest;
import br.ufrn.analyze.repository.ChangeRequestDAO;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;

/**
 * First version of analyzer
 * <p>
 * Analyze host ( Virtual Machines ) and Cluster ( group of Virtual Machines )
 */
@Component
@Transactional
public class DEPRECATEDIndividualThresholdAnalyzer {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private Logger logger;
    @Autowired
    private ChangeRequestDAO changeRequestDAO;

    public void execute(AnalyzeConfig analyzeConfig) {
        collectMonitoringDate(analyzeConfig);
        scalingHeat();
        changeRequestDAO.flush();
        sendChangeRequest(analyzeConfig);
    }

    /**
     * Collect monitoring data and define whether action should be taken to increase (UP) or decrease ( DOWN ) resources
     * based on thresholds
     *
     * @param analyzeConfig
     */
    private void collectMonitoringDate(AnalyzeConfig analyzeConfig) {
        MonitorConfig monitorConfig = restTemplate.getForObject(analyzeConfig.getClusterNodesMonitorApiURL(), MonitorConfig.class);

        int totalGroupedItens = monitorConfig.getGroupedItems().size();
        int countGroup = 0;
        logger.info("[IndividualThresholdAnalyzer] Total grouped items to process: " + monitorConfig.getGroupedItems().size());
        for (GroupedItem groupedItem : monitorConfig.getGroupedItems()) {
            logger.info("[IndividualThresholdAnalyzer] \tProcess " + ++countGroup + " of " + totalGroupedItens + " grouped items");
            logger.info("[IndividualThresholdAnalyzer] \tGroup Name: " + groupedItem.getGroupName());
            logger.info("[IndividualThresholdAnalyzer] \tGroup Type: " + groupedItem.getGroupType());
            int totalIndividualItems = groupedItem.getIndividualItems().size();
            int countIndividualItems = 0;
            double totalCpuResource = 0.0;
            double totalUsedCpuResource = 0.0;
            double totalMemoryResource = 0.0;
            double totalUsedMemoryResource = 0.0;
            double scaleQuantity;

            if (groupedItem.getGroupType().compareTo(ItemType.CLUSTER) == 0) {
                //PROCESSAR ITEMS DO CLUSTER ( CONJUNTO DE VMs)
                for (IndividualItem individualItem : groupedItem.getIndividualItems()) {
                    logger.info("[IndividualThresholdAnalyzer]  Process " + ++countIndividualItems + " of " + totalIndividualItems + " individual items");
                    logger.info("[IndividualThresholdAnalyzer]    Item Name: " + individualItem.getItemName());
                    logger.info("[IndividualThresholdAnalyzer]    Item Type: " + individualItem.getItemType());
                    if (individualItem.getItemType().compareTo(ItemType.VM) == 0) {
                        String monitorURL;

                        //CPU DATA
                        monitorURL = analyzeConfig.getHostMonitorApiCpuUrl() + "/" + individualItem.getItemName();
                        MonitoredItemDTO monitoredItemDTOCPU = restTemplate.getForObject(monitorURL, MonitoredItemDTO.class);
                        logger.info("[IndividualThresholdAnalyzer]    Resource Type: " + monitoredItemDTOCPU.getResourceType());

                        scaleQuantity = suggestAmountOfResources(monitoredItemDTOCPU.getUsedResource(),
                                monitoredItemDTOCPU.getTotalResource(),
                                analyzeConfig.getHostCpuThresholdUp(),
                                analyzeConfig.getHostCpuThresholdDown());

                        createChangeRequest(analyzeConfig.getAnalysisNumber(),
                                individualItem.getItemType(),
                                individualItem.getItemName(),
                                monitoredItemDTOCPU.getResourceType(),
                                monitoredItemDTOCPU.getUsedResource(),
                                monitoredItemDTOCPU.getTotalResource(),
                                monitoredItemDTOCPU.getResourceUnit(),
                                analyzeConfig.getHostCpuThresholdUp(),
                                analyzeConfig.getHostCpuThresholdDown(),
                                scaleQuantity);

                        totalCpuResource = totalCpuResource + monitoredItemDTOCPU.getTotalResource();
                        totalUsedCpuResource = totalUsedCpuResource + (monitoredItemDTOCPU.getUsedResource() * monitoredItemDTOCPU.getTotalResource());

                        //MEMORY DATA
                        monitorURL = analyzeConfig.getHostMonitorApiMemoryUrl() + "/" + individualItem.getItemName();
                        MonitoredItemDTO monitoredItemDTOMemory = restTemplate.getForObject(monitorURL, MonitoredItemDTO.class);
                        logger.info("[IndividualThresholdAnalyzer]    Resource Type: " + monitoredItemDTOMemory.getResourceType());

                        scaleQuantity = suggestAmountOfResources(monitoredItemDTOMemory.getUsedResource(),
                                monitoredItemDTOMemory.getTotalResource(),
                                analyzeConfig.getHostMemoryThresholdUp(),
                                analyzeConfig.getHostMemoryThresholdDown());


                        createChangeRequest(analyzeConfig.getAnalysisNumber(),
                                individualItem.getItemType(),
                                individualItem.getItemName(),
                                monitoredItemDTOMemory.getResourceType(),
                                monitoredItemDTOMemory.getUsedResource(),
                                monitoredItemDTOMemory.getTotalResource(),
                                monitoredItemDTOMemory.getResourceUnit(),
                                analyzeConfig.getHostMemoryThresholdUp(),
                                analyzeConfig.getHostMemoryThresholdDown(),
                                scaleQuantity);

                        totalMemoryResource = totalMemoryResource + monitoredItemDTOMemory.getTotalResource();
                        totalUsedMemoryResource = totalUsedMemoryResource + (monitoredItemDTOMemory.getUsedResource() * monitoredItemDTOMemory.getTotalResource());
                    }else{
                        logger.error("[IndividualThresholdAnalyzer] Unsuported item type. Expected + " + ItemType.VM + " current " + individualItem.getItemType());
                    }
                }
                //CPU CLUSTER DATA
                logger.info("[IndividualThresholdAnalyzer]    Item Name: " + groupedItem.getGroupName());
                logger.info("[IndividualThresholdAnalyzer]    Item Type: " + groupedItem.getGroupType());

                logger.info("[IndividualThresholdAnalyzer]    Resource Type: " + ResourceType.CPU);

                scaleQuantity = suggestAmountOfResources(totalUsedCpuResource / totalCpuResource,
                        totalCpuResource,
                        analyzeConfig.getHostCpuThresholdUp(),
                        analyzeConfig.getHostCpuThresholdDown());

                createChangeRequest(analyzeConfig.getAnalysisNumber(),
                        groupedItem.getGroupType(),
                        groupedItem.getGroupName(),
                        ResourceType.CPU,
                        totalUsedCpuResource / totalCpuResource,
                        totalCpuResource,
                        ResourceUnit.CORES,
                        analyzeConfig.getHostCpuThresholdUp(),
                        analyzeConfig.getHostCpuThresholdDown(),
                        scaleQuantity);

                //MEMORY CLUSTER DATA
                logger.info("[IndividualThresholdAnalyzer]    Resource Type: " + ResourceType.MEMORY);

                scaleQuantity = suggestAmountOfResources(totalUsedMemoryResource / totalMemoryResource,
                        totalMemoryResource,
                        analyzeConfig.getHostMemoryThresholdUp(),
                        analyzeConfig.getHostMemoryThresholdDown());


                createChangeRequest(analyzeConfig.getAnalysisNumber(),
                        groupedItem.getGroupType(),
                        groupedItem.getGroupName(),
                        ResourceType.MEMORY,
                        totalUsedMemoryResource / totalMemoryResource,
                        totalMemoryResource,
                        ResourceUnit.GB,
                        analyzeConfig.getHostMemoryThresholdUp(),
                        analyzeConfig.getHostMemoryThresholdDown(),
                        scaleQuantity);


            }else if (groupedItem.getGroupType().compareTo(ItemType.DEPLOYMENT) == 0){
                //PROCESSAR ITENS DO DEPLOYMENT ( conjunto de PODs )
                for (IndividualItem individualItem : groupedItem.getIndividualItems()) {
                    logger.info("[IndividualThresholdAnalyzer]  Process " + ++countIndividualItems + " of " + totalIndividualItems + " individual items");
                    logger.info("[IndividualThresholdAnalyzer]    Item Name: " + individualItem.getItemName());
                    logger.info("[IndividualThresholdAnalyzer]    Item Type: " + individualItem.getItemType());
                    if (individualItem.getItemType().compareTo(ItemType.POD) == 0) {
                        String monitorURL;

                        //CPU DATA
                        monitorURL = analyzeConfig.getContainerMonitorApiCpuUrl() + "/" + individualItem.getItemName();
                        MonitoredItemDTO monitoredItemDTOCPU = restTemplate.getForObject(monitorURL, MonitoredItemDTO.class);
                        logger.info("[IndividualThresholdAnalyzer]    Resource Type: " + monitoredItemDTOCPU.getResourceType());

                        scaleQuantity = suggestAmountOfResources(monitoredItemDTOCPU.getUsedResource(),
                                monitoredItemDTOCPU.getTotalResource(),
                                analyzeConfig.getContainerCpuThresholdUp(),
                                analyzeConfig.getContainerCpuThresholdDown());

                        createChangeRequest(analyzeConfig.getAnalysisNumber(),
                                individualItem.getItemType(),
                                individualItem.getItemName(),
                                monitoredItemDTOCPU.getResourceType(),
                                monitoredItemDTOCPU.getUsedResource(),
                                monitoredItemDTOCPU.getTotalResource(),
                                monitoredItemDTOCPU.getResourceUnit(),
                                analyzeConfig.getContainerCpuThresholdUp(),
                                analyzeConfig.getContainerCpuThresholdDown(),
                                scaleQuantity);

                        totalCpuResource = totalCpuResource + monitoredItemDTOCPU.getTotalResource();
                        totalUsedCpuResource = totalUsedCpuResource + (monitoredItemDTOCPU.getUsedResource() * monitoredItemDTOCPU.getTotalResource());

                        //MEMORY DATA
                        monitorURL = analyzeConfig.getContainerMonitorApiMemoryUrl() + "/" + individualItem.getItemName();
                        MonitoredItemDTO monitoredItemDTOMemory = restTemplate.getForObject(monitorURL, MonitoredItemDTO.class);
                        logger.info("[IndividualThresholdAnalyzer]    Resource Type: " + monitoredItemDTOMemory.getResourceType());

                        scaleQuantity = suggestAmountOfResources(monitoredItemDTOMemory.getUsedResource(),
                                monitoredItemDTOMemory.getTotalResource(),
                                analyzeConfig.getContainerMemoryThresholdUp(),
                                analyzeConfig.getContainerMemoryThresholdDown());


                        createChangeRequest(analyzeConfig.getAnalysisNumber(),
                                individualItem.getItemType(),
                                individualItem.getItemName(),
                                monitoredItemDTOMemory.getResourceType(),
                                monitoredItemDTOMemory.getUsedResource(),
                                monitoredItemDTOMemory.getTotalResource(),
                                monitoredItemDTOMemory.getResourceUnit(),
                                analyzeConfig.getContainerMemoryThresholdUp(),
                                analyzeConfig.getContainerMemoryThresholdDown(),
                                scaleQuantity);

                        totalMemoryResource = totalMemoryResource + monitoredItemDTOMemory.getTotalResource();
                        totalUsedMemoryResource = totalUsedMemoryResource + (monitoredItemDTOMemory.getUsedResource() * monitoredItemDTOMemory.getTotalResource());
                    }else{
                        logger.error("[IndividualThresholdAnalyzer] Unsuported item type. Expected " + ItemType.POD + " current " + individualItem.getItemType());
                    }
                }
                //CPU CLUSTER DATA
                logger.info("[IndividualThresholdAnalyzer]    Item Name: " + groupedItem.getGroupName());
                logger.info("[IndividualThresholdAnalyzer]    Item Type: " + groupedItem.getGroupType());

                logger.info("[IndividualThresholdAnalyzer]    Resource Type: " + ResourceType.CPU);

                scaleQuantity = suggestAmountOfResources(totalUsedCpuResource / totalCpuResource,
                        totalCpuResource,
                        analyzeConfig.getContainerCpuThresholdUp(),
                        analyzeConfig.getContainerCpuThresholdDown());

                createChangeRequest(analyzeConfig.getAnalysisNumber(),
                        groupedItem.getGroupType(),
                        groupedItem.getGroupName(),
                        ResourceType.CPU,
                        totalUsedCpuResource / totalCpuResource,
                        totalCpuResource,
                        ResourceUnit.CORES,
                        analyzeConfig.getContainerCpuThresholdUp(),
                        analyzeConfig.getContainerCpuThresholdDown(),
                        scaleQuantity);

                //MEMORY CLUSTER DATA
                logger.info("[IndividualThresholdAnalyzer]    Resource Type: " + ResourceType.MEMORY);

                scaleQuantity = suggestAmountOfResources(totalUsedMemoryResource / totalMemoryResource,
                        totalMemoryResource,
                        analyzeConfig.getContainerMemoryThresholdUp(),
                        analyzeConfig.getContainerMemoryThresholdDown());


                createChangeRequest(analyzeConfig.getAnalysisNumber(),
                        groupedItem.getGroupType(),
                        groupedItem.getGroupName(),
                        ResourceType.MEMORY,
                        totalUsedMemoryResource / totalMemoryResource,
                        totalMemoryResource,
                        ResourceUnit.GB,
                        analyzeConfig.getContainerMemoryThresholdUp(),
                        analyzeConfig.getContainerMemoryThresholdDown(),
                        scaleQuantity);

            }
        }
    }


    /**
     * Calculate the amount of resources that should be added or removed according to the scaling action
     */
    private Double suggestAmountOfResources(Double usedResource, Double totalResource, Double thresholdUp, Double thresholdDown) {
        Double recommendedAmount = 0.0;

        if (thresholdUp <= thresholdDown) {
            logger.error("[IndividualThresholdAnalyzer]      Invalid configuration: thresholdUp <= thresholdDown ");
        } else {
            Double factor = 1.1; // TODO PARAMETRIZAR ESSE VALOR - 1.1 significa que vai aumentar 10% a cada tentantiva de iteração

            Double totalUsedResourceAbsolute = usedResource * totalResource;
            Double absoluteThresholdUp = thresholdUp * totalResource;
            Double absoluteThresholdDown = thresholdDown * totalResource;
            Double usedResourceTmp = usedResource;

            logger.info("[IndividualThresholdAnalyzer]      ThresholdUp: " + thresholdUp);
            logger.info("[IndividualThresholdAnalyzer]      ThresholdDown: " + thresholdDown);
            logger.info("[IndividualThresholdAnalyzer]      Used Resource ( % ) : " + usedResource);
            logger.info("[IndividualThresholdAnalyzer]      Used Resource (absolute) : " + totalUsedResourceAbsolute);
            logger.info("[IndividualThresholdAnalyzer]      Total Resource: " + totalResource);

            if (usedResource > thresholdUp) {  //amount is above thresholds
                logger.info("[IndividualThresholdAnalyzer]      upper threshold violation ");
                recommendedAmount = totalUsedResourceAbsolute - (absoluteThresholdUp);
                while (usedResourceTmp > thresholdUp) {
                    recommendedAmount = recommendedAmount * factor; //
                    logger.info("[IndividualThresholdAnalyzer]        suggested amount to add: " + recommendedAmount);
                    usedResourceTmp = totalUsedResourceAbsolute / (totalResource + recommendedAmount);
                    logger.info("[IndividualThresholdAnalyzer]        expected usage after addition: " + usedResourceTmp);
                }
            } else if (usedResource < thresholdDown) { //amount is below thresholds
                logger.info("[IndividualThresholdAnalyzer]      down threshold violation ");
                recommendedAmount = totalUsedResourceAbsolute - (absoluteThresholdDown);
                while (usedResourceTmp < thresholdDown) {

                    if ((-1 * (recommendedAmount * factor)) > totalResource) { // tenta removar mais recursos que o total existente. nao multiplica pelo fator e utiliza o valor anterior recomendado
                        logger.info("[IndividualThresholdAnalyzer]        maximum limit for removal reached: " + recommendedAmount);
                        break;
                    }
                    recommendedAmount = recommendedAmount * factor; //
                    logger.info("[IndividualThresholdAnalyzer]        suggested amount to remove: " + recommendedAmount);
                    usedResourceTmp = totalUsedResourceAbsolute / (totalResource + recommendedAmount);
                    logger.info("[IndividualThresholdAnalyzer]        expected usage after addition: " + usedResourceTmp);
                }
            } else { // amount is within the thresholds
                logger.info("[IndividualThresholdAnalyzer]      no threshold violation ");
            }
        }
        return recommendedAmount;
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

    private void sendChangeRequest(AnalyzeConfig analyzeConfig) {
        Long lastAnalysisNumber = changeRequestDAO.getLasAnalysisNumber();
        List<ChangeRequest> changeRequestList = changeRequestDAO.findAllByAnalysisNumber(lastAnalysisNumber);
        for (ChangeRequest changeRequest : changeRequestList) {
            if (Math.abs(changeRequest.getHeat()) == analyzeConfig.getHeatThreshold() ){
                logger.info("[IndividualThresholdAnalyzer] Send Change Request \n" + changeRequest);
                logger.info("[IndividualThresholdAnalyzer] Heat value reset");
                changeRequest.setHeat(0);
                changeRequest.setChangeRequestDate(new Date());
                changeRequestDAO.saveAndFlush(changeRequest);
            }
        }

    }

    private ChangeRequest createChangeRequest(
            Long analysisNumber,
            String itemType,
            String itemName,
            String resourceType,
            Double usedResource,
            Double totalResource,
            String resourceUnit,
            Double thresholdUp,
            Double thresholdDown,
            Double scaleQuantity
    ) {
        ChangeRequest changeRequest = new ChangeRequest();
        changeRequest.setAnalysisNumber(analysisNumber);
        changeRequest.setItemType(itemType);
        changeRequest.setItemName(itemName);
        changeRequest.setResourceType(resourceType);
        changeRequest.setUsedResource(usedResource);
        changeRequest.setTotalResource(totalResource);
        changeRequest.setResourceUnit(resourceUnit);
        changeRequest.setThresholdUp(thresholdUp);
        changeRequest.setThresholdDown(thresholdDown);
        if (usedResource > thresholdUp) {
            changeRequest.setThresholdViolation(ThresholdViolation.UP);
        } else if (usedResource < thresholdDown) {
            changeRequest.setThresholdViolation(ThresholdViolation.DOWN);
        } else {
            changeRequest.setThresholdViolation(ThresholdViolation.NONE);
        }
        changeRequest.setScaleQuantity(scaleQuantity);
        return changeRequestDAO.saveAndFlush(changeRequest);
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