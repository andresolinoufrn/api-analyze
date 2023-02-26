package br.ufrn.analyze.service;

import br.ufrn.analyze.config.QuartzConfig;
import br.ufrn.analyze.domain.entity.AnalyzeConfig;
import br.ufrn.analyze.job.AnalyzerJob;
import br.ufrn.analyze.repository.AnalyzeConfigDAO;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class AnalyzerJobService {

    @Autowired
    private AnalyzeConfigDAO analyzeConfigDAO;
    @Autowired
    private QuartzConfig quartzConfig;

    private JobDetail jobDetail = JobBuilder.newJob(AnalyzerJob.class)
            .withIdentity("analyzeJob")
            .withDescription("Analyzer Job")
            //.usingJobData("param", "valor do parametro") // add a parameter
            .build();

    public void startAnalyzer() throws SchedulerException, IOException {
        Optional<AnalyzeConfig> analyzeConfig = analyzeConfigDAO.findById(Long.valueOf(1));
        if (!analyzeConfig.isEmpty()){
            Trigger periodicTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("periodicTrigger", "analyzeGroup")
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(analyzeConfig.get().getUpdateFrequency())
                            .repeatForever())
                    .startNow()
                    .build();


            //scheduler = schedulerFactory.getScheduler();
            Scheduler scheduler = quartzConfig.schedulerFactoryBean().getScheduler();

            scheduler.start();
            scheduler.scheduleJob(jobDetail, periodicTrigger);
        }else{
            //TODO disparar mensagem que no existe configuraço
        }



    }

    public void stopAnalyzer() throws SchedulerException, IOException {
        //scheduler = schedulerFactory.getScheduler();
        Scheduler scheduler = quartzConfig.schedulerFactoryBean().getScheduler();
        scheduler.deleteJob(jobDetail.getKey());
    }

}
