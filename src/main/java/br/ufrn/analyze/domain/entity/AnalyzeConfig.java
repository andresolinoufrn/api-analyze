package br.ufrn.analyze.domain.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class AnalyzeConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Configuraçoes Gerais

    // Frequencia, em segundos, de quando o analisador ira buscar dados de monitoramento
    private Integer updateFrequency;

    // Quantidade de tempo, em segundos, dos dados que serao considerados para a analise, os ultimos segundos
    // exemplo: 30 signigica que ira buscar os registros mais recentes 30 segundos
    private Integer historyInterval;

    // Numero da analise, cada analise tem o seu numero e as requisicoes de mudança sao geradas com esse numero
    private Long analysisNumber;

    private Integer heatThreshold; // valor que ao ser atingido faz com que a analise envie a ChangeRequest para o planejaemnto


    // CONFIGURACOES NIVEL HOST

    // URL do API de monitoramento de onde serão recueprado os nós do cluster
    private String clusterNodesMonitorApiURL;

    // URL de Monitoramento da CPU - nivel de host
    private String hostMonitorApiCpuUrl;

    // Limite superior de uso de CPU - acima disso mais recursos devem ser adicionados
    private double hostCpuThresholdUp;

    // Limite inferior de uso de CPU - abaixo disso recursos podem ser removidos
    private double hostCpuThresholdDown;

    // URL de Monitoramento da memoria - nivel de host
    private String hostMonitorApiMemoryUrl;

    private double hostMemoryThresholdUp;

    private double hostMemoryThresholdDown;


    // CONFIGURACOES NIVEL CONTAINER

    // Nome do namespace onde ocorrerá a analise
    private String namespaceToAnalise;

    // URL de Monitoramento da CPU - nivel de container
    private String containerMonitorApiCpuUrl;

    // URL que retorna os desploymentes de um namespace
    private String deploymentsFromNamespaceUrl;

    // URL que retornar o math label de um determinado namespace e Deployment
    private String matchLabelsFromNamespaceAndDeploymentUrl;

    // URL que retorna os pods de um namespace e label
    private String podsFromNamespaceAndMatchLabelsUrl;

    // Limite superior de uso de CPU - acima disso mais recursos devem ser adicionados
    private double containerCpuThresholdUp;

    // Limite inferior de uso de CPU - abaixo disso recursos podem ser removidos
    private double containerCpuThresholdDown;

    // URL de Monitoramento da memoria - nivel de host
    private String containerMonitorApiMemoryUrl;

    private double containerMemoryThresholdUp;

    private double containerMemoryThresholdDown;

    // Configuraoes nivel plataforma

    private String platformMonitorApiUrl;

    private double operationTimeThresholdUp;

    //Getter and Setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUpdateFrequency() {
        return updateFrequency;
    }

    public void setUpdateFrequency(Integer updateFrequency) {
        this.updateFrequency = updateFrequency;
    }

    public Integer getHistoryInterval() {
        return historyInterval;
    }

    public void setHistoryInterval(Integer historyInterval) {
        this.historyInterval = historyInterval;
    }

    public Long getAnalysisNumber() {
        return analysisNumber;
    }

    public void setAnalysisNumber(Long analysisNumber) {
        this.analysisNumber = analysisNumber;
    }

    public Integer getHeatThreshold() {
        return heatThreshold;
    }

    public void setHeatThreshold(Integer heatThreshold) {
        this.heatThreshold = heatThreshold;
    }

    public String getClusterNodesMonitorApiURL() {
        return clusterNodesMonitorApiURL;
    }

    public void setClusterNodesMonitorApiURL(String clusterNodesMonitorApiURL) {
        this.clusterNodesMonitorApiURL = clusterNodesMonitorApiURL;
    }

    public String getHostMonitorApiCpuUrl() {
        return hostMonitorApiCpuUrl;
    }

    public void setHostMonitorApiCpuUrl(String hostMonitorApiCpuUrl) {
        this.hostMonitorApiCpuUrl = hostMonitorApiCpuUrl;
    }

    public double getHostCpuThresholdUp() {
        return hostCpuThresholdUp;
    }

    public void setHostCpuThresholdUp(double hostCpuThresholdUp) {
        this.hostCpuThresholdUp = hostCpuThresholdUp;
    }

    public double getHostCpuThresholdDown() {
        return hostCpuThresholdDown;
    }

    public void setHostCpuThresholdDown(double hostCpuThresholdDown) {
        this.hostCpuThresholdDown = hostCpuThresholdDown;
    }

    public String getHostMonitorApiMemoryUrl() {
        return hostMonitorApiMemoryUrl;
    }

    public void setHostMonitorApiMemoryUrl(String hostMonitorApiMemoryUrl) {
        this.hostMonitorApiMemoryUrl = hostMonitorApiMemoryUrl;
    }

    public double getHostMemoryThresholdUp() {
        return hostMemoryThresholdUp;
    }

    public void setHostMemoryThresholdUp(double hostMemoryThresholdUp) {
        this.hostMemoryThresholdUp = hostMemoryThresholdUp;
    }

    public double getHostMemoryThresholdDown() {
        return hostMemoryThresholdDown;
    }

    public void setHostMemoryThresholdDown(double hostMemoryThresholdDown) {
        this.hostMemoryThresholdDown = hostMemoryThresholdDown;
    }

    public String getNamespaceToAnalise() {
        return namespaceToAnalise;
    }

    public void setNamespaceToAnalise(String namespaceToAnalise) {
        this.namespaceToAnalise = namespaceToAnalise;
    }

    public String getContainerMonitorApiCpuUrl() {
        return containerMonitorApiCpuUrl;
    }

    public void setContainerMonitorApiCpuUrl(String containerMonitorApiCpuUrl) {
        this.containerMonitorApiCpuUrl = containerMonitorApiCpuUrl;
    }

    public String getDeploymentsFromNamespaceUrl() {
        return deploymentsFromNamespaceUrl;
    }

    public void setDeploymentsFromNamespaceUrl(String deploymentsFromNamespaceUrl) {
        this.deploymentsFromNamespaceUrl = deploymentsFromNamespaceUrl;
    }

    public String getMatchLabelsFromNamespaceAndDeploymentUrl() {
        return matchLabelsFromNamespaceAndDeploymentUrl;
    }

    public void setMatchLabelsFromNamespaceAndDeploymentUrl(String matchLabelsFromNamespaceAndDeploymentUrl) {
        this.matchLabelsFromNamespaceAndDeploymentUrl = matchLabelsFromNamespaceAndDeploymentUrl;
    }

    public String getPodsFromNamespaceAndMatchLabelsUrl() {
        return podsFromNamespaceAndMatchLabelsUrl;
    }

    public void setPodsFromNamespaceAndMatchLabelsUrl(String podsFromNamespaceAndMatchLabelsUrl) {
        this.podsFromNamespaceAndMatchLabelsUrl = podsFromNamespaceAndMatchLabelsUrl;
    }

    public double getContainerCpuThresholdUp() {
        return containerCpuThresholdUp;
    }

    public void setContainerCpuThresholdUp(double containerCpuThresholdUp) {
        this.containerCpuThresholdUp = containerCpuThresholdUp;
    }

    public double getContainerCpuThresholdDown() {
        return containerCpuThresholdDown;
    }

    public void setContainerCpuThresholdDown(double containerCpuThresholdDown) {
        this.containerCpuThresholdDown = containerCpuThresholdDown;
    }

    public String getContainerMonitorApiMemoryUrl() {
        return containerMonitorApiMemoryUrl;
    }

    public void setContainerMonitorApiMemoryUrl(String containerMonitorApiMemoryUrl) {
        this.containerMonitorApiMemoryUrl = containerMonitorApiMemoryUrl;
    }

    public double getContainerMemoryThresholdUp() {
        return containerMemoryThresholdUp;
    }

    public void setContainerMemoryThresholdUp(double containerMemoryThresholdUp) {
        this.containerMemoryThresholdUp = containerMemoryThresholdUp;
    }

    public double getContainerMemoryThresholdDown() {
        return containerMemoryThresholdDown;
    }

    public void setContainerMemoryThresholdDown(double containerMemoryThresholdDown) {
        this.containerMemoryThresholdDown = containerMemoryThresholdDown;
    }

    public String getPlatformMonitorApiUrl() {
        return platformMonitorApiUrl;
    }

    public void setPlatformMonitorApiUrl(String platformMonitorApiUrl) {
        this.platformMonitorApiUrl = platformMonitorApiUrl;
    }

    public double getOperationTimeThresholdUp() {
        return operationTimeThresholdUp;
    }

    public void setOperationTimeThresholdUp(double operationTimeThresholdUp) {
        this.operationTimeThresholdUp = operationTimeThresholdUp;
    }
}
