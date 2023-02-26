package br.ufrn.analyze;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.client.RestTemplate;




@SpringBootApplication
@EnableJpaAuditing
public class ApiAnalyzeApplication {

    public static void main(String[] args) throws SchedulerException, InterruptedException {
        SpringApplication.run(ApiAnalyzeApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }


    @Bean
    public Logger getLogger(){
        return  LoggerFactory.getLogger(ApiAnalyzeApplication.class);
    }

//    @Bean
//    public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
//        return args -> {
//            MonitoredItemDTO monitoredItemDTO = restTemplate.getForObject(
//                    "http://localhost:8080/api-monitor/fakehost/cpu/positivo", MonitoredItemDTO.class);
//            System.out.println(monitoredItemDTO);
//
//        };
//    }

}
