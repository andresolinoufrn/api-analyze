package br.ufrn.analyze.repository;

import br.ufrn.analyze.domain.entity.ChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChangeRequestDAO extends JpaRepository<ChangeRequest, Integer> {

    /**
     * Retorn the last analysis number
     * @return
     */
    @Query(value = "SELECT max(analysisNumber) FROM ChangeRequest ")
    public Long getLasAnalysisNumber();

    /**
     * returns the number of records before the last analysis.
     * @return
     */
    @Query(value =  "select count(id) from ChangeRequest where analysisNumber < (select max(analysisNumber) from ChangeRequest )")
    public Long countAnalysisBeforeLast();

    @Query(value = "update ChangeRequest set heat = 0")
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    public void resetHeat();

    public List<ChangeRequest> findAllByAnalysisNumber(Long analysisNumber);

    public List<ChangeRequest> findAllByAnalysisNumberAndItemType(Long analysisNumber, String itemType);

    public List<ChangeRequest> findAllByAnalysisNumberAndItemTypeAndResourceType(Long analysisNumber, String itemType, String resourceType);

    //public List<ChangeRequest> findAllByAnalysisNumberAndItemTypeAndResourceTypeAndMemberOf(Long analysisNumber, String itemType, String resourceType, String memberOf);

    public List<ChangeRequest> findAllByAnalysisNumberAndVmClusterNameAndVmName(Long analysisNumber, String vmClusterName, String vmName);

    public List<ChangeRequest> findAllByAnalysisNumberAndVmClusterNameAndVmNameIsNull(Long analysisNumber, String vmClusterName);

    public List<ChangeRequest> findAllByAnalysisNumberAndVmClusterNameAndVmNameIsNotNull(Long analysisNumber, String vmClusterName);


    public List<ChangeRequest> findAllByAnalysisNumberAndItemTypeAndResourceTypeAndVmClusterNameAndVmNameIsNull(Long analysisNumber, String itemType, String resourceType,String vmClusterName);

    public List<ChangeRequest> findAllByAnalysisNumberAndItemTypeAndResourceTypeAndVmClusterNameAndVmNameIsNotNull(Long analysisNumber, String itemType, String resourceType,String vmClusterName);

    public List<ChangeRequest> findAllByAnalysisNumberAndItemTypeAndResourceTypeAndDeploymentNameAndPodNameIsNotNull(Long analysisNumber, String itemType, String resourceType,String deploymentName);

}
