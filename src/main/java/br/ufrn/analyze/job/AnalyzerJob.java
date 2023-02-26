package br.ufrn.analyze.job;

import br.ufrn.analyze.service.AnalyzerService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;


public class AnalyzerJob implements Job {

    @Autowired
    private AnalyzerService analyzerService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        analyzerService.execute();
    }

//    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
//        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
//        String param = dataMap.getString("param");
//        System.out.println(MessageFormat.format("{0}, Job: {1}; Param: {2}", new Date().toString(),
//                getClass(), param));
//    }
}
