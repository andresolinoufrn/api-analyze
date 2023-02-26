package br.ufrn.analyze.domain.entity;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class ChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @CreatedDate
    private Date createdDate;
    @LastModifiedDate
    private Date lastModifiedDate;

    private Long analysisNumber;    // Analysis iteration number

    // Monitored items data
    private String itemType;        // CLUSTER, VM, DEPLOYMENT or POD
    private String itemName;        // MonitoredItemDTO.itemName
    private String vmName;          // MonitoredItemDTO.itemName if the item is a virtual machine
    private String vmClusterName;   // Name of VM cluster
    private String podName;         // MonitoredItemDTO.itemName if the item is a POD
    private String deploymentName;  // MonitoredItemDTO.itemName if the item is a deployment
    private String operationName;   // MonitoredItemDTO.itemName if the item is a operation
    private String resourceType;    // MonitoredItemDTO.resourceType - CPU or MEMORY
    private Double totalResource;   // MonitoredItemDTO.totalResource - total available resource of item
    private Double usedResource;    // MonitoredItemDTO.usedResource - current resource usage
    private String resourceUnit;    // MonitoredItemDTO.resourceUNit - ex CORES for cpu or GB for memory

    // thresholds when item was collected
    private Double thresholdUp;
    private Double ThresholdDown;

    // scale data
    private String thresholdViolation; // UP or DOWN
    private Double scaleQuantity; // amount of resources to be added or removed

    // heat algorithm
    private Integer heat;

    // Change request date sent. Null if not sent.
    private Date changeRequestDate;


    //** apenas para auxiliar durante o experimento

    private String experimento;

    private String cenario;

    private Integer repeticao;

    //getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Long getAnalysisNumber() {
        return analysisNumber;
    }

    public void setAnalysisNumber(Long analysisNumber) {
        this.analysisNumber = analysisNumber;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public String getVmClusterName() {
        return vmClusterName;
    }

    public void setVmClusterName(String vmClusterName) {
        this.vmClusterName = vmClusterName;
    }

    public String getPodName() {
        return podName;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Double getTotalResource() {
        return totalResource;
    }

    public void setTotalResource(Double totalResource) {
        this.totalResource = totalResource;
    }

    public Double getUsedResource() {
        return usedResource;
    }

    public void setUsedResource(Double usedResource) {
        this.usedResource = usedResource;
    }

    public String getResourceUnit() {
        return resourceUnit;
    }

    public void setResourceUnit(String resourceUnit) {
        this.resourceUnit = resourceUnit;
    }

    public Double getThresholdUp() {
        return thresholdUp;
    }

    public void setThresholdUp(Double thresholdUp) {
        this.thresholdUp = thresholdUp;
    }

    public Double getThresholdDown() {
        return ThresholdDown;
    }

    public void setThresholdDown(Double thresholdDown) {
        ThresholdDown = thresholdDown;
    }

    public String getThresholdViolation() {
        return thresholdViolation;
    }

    public void setThresholdViolation(String thresholdViolation) {
        this.thresholdViolation = thresholdViolation;
    }

    public Double getScaleQuantity() {
        return scaleQuantity;
    }

    public void setScaleQuantity(Double scaleQuantity) {
        this.scaleQuantity = scaleQuantity;
    }

    public Integer getHeat() {
        return heat;
    }

    public void setHeat(Integer heat) {
        this.heat = heat;
    }

    public Date getChangeRequestDate() {
        return changeRequestDate;
    }

    public void setChangeRequestDate(Date changeRequestDate) {
        this.changeRequestDate = changeRequestDate;
    }

    public String getExperimento() {
        return experimento;
    }

    public void setExperimento(String experimento) {
        this.experimento = experimento;
    }

    public String getCenario() {
        return cenario;
    }

    public void setCenario(String cenario) {
        this.cenario = cenario;
    }

    public Integer getRepeticao() {
        return repeticao;
    }

    public void setRepeticao(Integer repeticao) {
        this.repeticao = repeticao;
    }

    @Override
    public String toString() {
        return "ChangeRequest{" +
                "id=" + id +
                ", createdDate=" + createdDate +
                ", lastModifiedDate=" + lastModifiedDate +
                ", analysisNumber=" + analysisNumber +
                ", itemType='" + itemType + '\'' +
                ", vmName='" + vmName + '\'' +
                ", vmClusterName='" + vmClusterName + '\'' +
                ", podName='" + podName + '\'' +
                ", deploymentName='" + deploymentName + '\'' +
                ", operationName='" + operationName + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", totalResource=" + totalResource +
                ", usedResource=" + usedResource +
                ", resourceUnit='" + resourceUnit + '\'' +
                ", thresholdUp=" + thresholdUp +
                ", ThresholdDown=" + ThresholdDown +
                ", thresholdViolation='" + thresholdViolation + '\'' +
                ", scaleQuantity=" + scaleQuantity +
                ", heat=" + heat +
                ", changeRequestDate=" + changeRequestDate +
                '}';
    }
}
